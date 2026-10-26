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

/** Perfil de cliente. Comparte la llave primaria con {@link UsuarioEntity}. */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class ClienteEntity {

    @Id
    private Long id;

    // Misma columna que la PK (solo lectura). Se usa @ManyToOne y no @OneToOne para que Hibernate
    // no intente resolver la asociación con el id aún nulo al hacer merge() de un perfil nuevo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false, unique = true, length = 30)
    private String documento;

    @Column(length = 200)
    private String direccion;

    @Column(length = 80)
    private String ciudad;
}
