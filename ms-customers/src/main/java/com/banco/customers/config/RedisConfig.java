package com.banco.customers.config;

import com.banco.customers.dto.CustomerResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configura el ReactiveRedisTemplate usado para cachear las
 * respuestas de clientes consultados frecuentemente (findById),
 * reduciendo la carga sobre MongoDB en operaciones de solo lectura.
 */
@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, CustomerResponse> customerRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<CustomerResponse> serializer =
                new Jackson2JsonRedisSerializer<>(CustomerResponse.class);

        RedisSerializationContext<String, CustomerResponse> context = RedisSerializationContext
                .<String, CustomerResponse>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
