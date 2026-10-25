package com.rentaherramientas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Empresa o persona que publica equipos para alquilar. Su id es el mismo del usuario. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {
    private Long id;
    private Usuario usuario;
    private String empresa;
    private String nit;
    private String direccion;
    private String ciudad;
}
