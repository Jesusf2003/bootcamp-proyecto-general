package com.banco.customers;

import com.banco.customers.dto.CustomerRequest;
import com.banco.customers.dto.CustomerResponse;
import com.banco.customers.model.Customer;
import com.banco.customers.model.CustomerType;
import com.banco.customers.repository.CustomerRepository;
import com.banco.customers.service.CustomerMapper;
import com.banco.customers.service.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de clientes usando mocks del
 * repositorio y StepVerifier para validar los flujos reactivos.
 * El template de Redis se mockea para que el cache-aside no
 * dependa de un servidor Redis real durante las pruebas unitarias.
 */
class CustomerServiceImplTest {

    private CustomerRepository customerRepository;
    private CustomerServiceImpl customerService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        customerRepository = Mockito.mock(CustomerRepository.class);
        ReactiveRedisTemplate<String, CustomerResponse> redisTemplate = Mockito.mock(ReactiveRedisTemplate.class);
        ReactiveValueOperations<String, CustomerResponse> valueOps = Mockito.mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        when(valueOps.set(anyString(), any(), any())).thenReturn(Mono.just(true));
        when(valueOps.delete(anyString())).thenReturn(Mono.just(true));

        customerService = new CustomerServiceImpl(customerRepository, new CustomerMapper(), redisTemplate);
    }

    @Test
    void shouldCreateCustomerWhenDocumentDoesNotExist() {
        CustomerRequest request = CustomerRequest.builder()
                .documentNumber("45678912")
                .customerType(CustomerType.PERSONAL)
                .fullName("Juan Perez")
                .email("juan.perez@mail.com")
                .build();

        Customer saved = Customer.builder()
                .id("1")
                .documentNumber("45678912")
                .customerType(CustomerType.PERSONAL)
                .fullName("Juan Perez")
                .email("juan.perez@mail.com")
                .active(true)
                .build();

        when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(customerService.create(request))
                .expectNextMatches(response -> response.getId().equals("1")
                        && response.getDocumentNumber().equals("45678912"))
                .verifyComplete();
    }

    @Test
    void shouldRejectCustomerWhenDocumentAlreadyExists() {
        CustomerRequest request = CustomerRequest.builder()
                .documentNumber("45678912")
                .customerType(CustomerType.PERSONAL)
                .fullName("Juan Perez")
                .email("juan.perez@mail.com")
                .build();

        when(customerRepository.existsByDocumentNumber(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(customerService.create(request))
                .expectError(com.banco.customers.exception.DuplicateDocumentException.class)
                .verify();
    }
}
