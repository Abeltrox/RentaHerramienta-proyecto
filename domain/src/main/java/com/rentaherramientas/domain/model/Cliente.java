package com.rentaherramientas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Persona o empresa que alquila equipos. Su id es el mismo del usuario. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private Long id;
    private Usuario usuario;
    private String documento;
    private String direccion;
    private String ciudad;
}
