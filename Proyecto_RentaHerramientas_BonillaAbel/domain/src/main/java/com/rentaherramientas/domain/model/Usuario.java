package com.rentaherramientas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Credenciales y datos básicos de acceso. Proveedor y Cliente extienden su información. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private String password; // siempre almacenada como hash BCrypt
    private String telefono;
    private Rol rol;
    private boolean activo;
    private LocalDateTime fechaCreacion;
}
