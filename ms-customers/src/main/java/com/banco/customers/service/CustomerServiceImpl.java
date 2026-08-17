package com.banco.customers.service;

import com.banco.customers.dto.CustomerRequest;
import com.banco.customers.dto.CustomerResponse;
import com.banco.customers.exception.CustomerNotFoundException;
import com.banco.customers.exception.DuplicateDocumentException;
import com.banco.customers.model.Customer;
import com.banco.customers.repository.CustomerRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementacion del servicio de clientes.
 *
 * La logica de validacion se compone usando RxJava3 (Single/Maybe) y
 * luego se adapta de vuelta a Reactor (Mono/Flux) porque WebFlux y
 * Spring Data Reactive trabajan de forma nativa con Reactor. Esto
 * satisface el requerimiento de usar reactividad con RxJava sin
 * perder la integracion con el resto del stack de Spring.
 *
 * Cache-aside con Redis (Parte III): findById primero consulta Redis;
 * si no esta, cae a MongoDB y guarda el resultado con TTL de 5 minutos.
 * Cualquier escritura (create/update/delete/updateProfile) invalida
 * la entrada correspondiente para evitar servir datos obsoletos.
 *
 * Inyeccion de dependencias por constructor (sin @Autowired en campos).
 */
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String CACHE_PREFIX = "customer:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ReactiveRedisTemplate<String, CustomerResponse> redisTemplate;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper,
                                ReactiveRedisTemplate<String, CustomerResponse> redisTemplate) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<CustomerResponse> create(CustomerRequest request) {
        Single<Boolean> existsRx = RxJava3Adapter.monoToSingle(
                customerRepository.existsByDocumentNumber(request.getDocumentNumber()));

        Single<Customer> savedRx = existsRx.flatMap(exists -> {
            if (Boolean.TRUE.equals(exists)) {
                return Single.error(new DuplicateDocumentException(request.getDocumentNumber()));
            }
            Customer toSave = customerMapper.toEntity(request);
            return RxJava3Adapter.monoToSingle(customerRepository.save(toSave));
        });

        return RxJava3Adapter.singleToMono(savedRx)
                .doOnNext(c -> log.info("Cliente creado con id={}", c.getId()))
                .map(customerMapper::toResponse);
    }

    @Override
    public Flux<CustomerResponse> findAll() {
        return customerRepository.findAll()
                .map(customerMapper::toResponse);
    }

    @Override
    public Mono<CustomerResponse> findById(String id) {
        String cacheKey = CACHE_PREFIX + id;
        return redisTemplate.opsForValue().get(cacheKey)
                .doOnNext(cached -> log.debug("Cache HIT para cliente id={}", id))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache MISS para cliente id={}, consultando MongoDB", id);
                    Maybe<Customer> found = RxJava3Adapter.monoToMaybe(customerRepository.findById(id));
                    return RxJava3Adapter.maybeToMono(found)
                            .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                            .map(customerMapper::toResponse)
                            .flatMap(response -> redisTemplate.opsForValue()
                                    .set(cacheKey, response, CACHE_TTL)
                                    .thenReturn(response));
                }));
    }

    @Override
    public Mono<CustomerResponse> update(String id, CustomerRequest request) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                .flatMap(existing -> {
                    existing.setDocumentNumber(request.getDocumentNumber());
                    existing.setCustomerType(request.getCustomerType());
                    existing.setFullName(request.getFullName());
                    existing.setEmail(request.getEmail());
                    existing.setPhoneNumber(request.getPhoneNumber());
                    existing.setAddress(request.getAddress());
                    return customerRepository.save(existing);
                })
                .doOnNext(c -> log.info("Cliente actualizado id={}", c.getId()))
                .flatMap(c -> redisTemplate.opsForValue().delete(CACHE_PREFIX + id).thenReturn(c))
                .map(customerMapper::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                .flatMap(customerRepository::delete)
                .then(redisTemplate.opsForValue().delete(CACHE_PREFIX + id))
                .doOnSuccess(v -> log.info("Cliente eliminado id={}", id))
                .then();
    }

    @Override
    public Mono<CustomerResponse> updateProfile(String id, com.banco.customers.model.CustomerProfile profile) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(id)))
                .flatMap(existing -> {
                    existing.setProfile(profile);
                    return customerRepository.save(existing);
                })
                .doOnNext(c -> log.info("Perfil actualizado a {} para cliente id={}", profile, id))
                .flatMap(c -> redisTemplate.opsForValue().delete(CACHE_PREFIX + id).thenReturn(c))
                .map(customerMapper::toResponse);
    }
}
