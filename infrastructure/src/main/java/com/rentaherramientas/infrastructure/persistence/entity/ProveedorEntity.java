package com.rentaherramientas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Perfil de proveedor. Comparte la llave primaria con {@link UsuarioEntity} (id asignado = id del usuario).
 * La asociación {@code usuario} es de solo lectura: el id se asigna a mano.
 */
@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
public class ProveedorEntity {

    @Id
    private Long id;

    // Misma columna que la PK (solo lectura). Se usa @ManyToOne y no @OneToOne para que Hibernate
    // no intente resolver la asociación con el id aún nulo al hacer merge() de un perfil nuevo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false, length = 150)
    private String empresa;

    @Column(nullable = false, unique = true, length = 30)
    private String nit;

    @Column(length = 200)
    private String direccion;

    @Column(length = 80)
    private String ciudad;
}
