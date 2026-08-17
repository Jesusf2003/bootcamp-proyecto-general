package com.banco.customers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entidad de negocio Cliente. Representa tanto clientes personales
 * como empresariales dentro de la misma coleccion, discriminados
 * por el atributo {@link CustomerType}.
 *
 * Se utiliza el patron "database per service": esta coleccion solo
 * es accedida por ms-customers.
 */
@Document(collection = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    private String id;

    /** Numero de documento de identidad (DNI) o RUC para empresas. */
    private String documentNumber;

    private CustomerType customerType;

    /** Nombre completo (persona) o razon social (empresa). */
    private String fullName;

    private String email;

    private String phoneNumber;

    private String address;

    /** Perfil especial (Parte II): STANDARD, VIP (personal) o PYME (empresarial). */
    @Builder.Default
    private CustomerProfile profile = CustomerProfile.STANDARD;

    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
