package com.banco.auth.service;

import com.banco.auth.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacen en memoria de usuarios de demostracion. En un entorno real
 * esto seria una coleccion Mongo propia de ms-auth con alta de
 * usuarios ligada al alta de clientes en ms-customers.
 */
@Component
public class UserStore {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserStore() {
        // Usuario administrador de la plataforma
        users.put("admin", User.builder()
                .username("admin")
                .passwordHash(encoder.encode("admin123"))
                .customerId(null)
                .role("ADMIN")
                .build());

        // Usuario cliente de demostracion (ajustar customerId al id real que
        // devuelva ms-customers al crear un cliente durante las pruebas).
        users.put("cliente1", User.builder()
                .username("cliente1")
                .passwordHash(encoder.encode("cliente123"))
                .customerId("REEMPLAZAR_CON_ID_REAL")
                .role("CLIENT")
                .build());
    }

    public User findByUsername(String username) {
        return users.get(username);
    }

    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
