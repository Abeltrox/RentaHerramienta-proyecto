package com.rentaherramientas.api.dto;

import com.rentaherramientas.application.port.in.CategoriaUseCase.CategoriaComando;
import com.rentaherramientas.domain.model.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoriaDto {

    private CategoriaDto() {
    }

    public record Request(
            @Schema(example = "Taladros y Rotomartillos") @NotBlank @Size(max = 80) String nombre,
            @Schema(example = "Herramientas para perforar concreto y metal") @Size(max = 300) String descripcion) {

        public CategoriaComando aComando() {
            return new CategoriaComando(nombre, descripcion);
        }
    }

    public record Response(Long id, String nombre, String descripcion) {
        public static Response de(Categoria c) {
            return new Response(c.getId(), c.getNombre(), c.getDescripcion());
        }
    }
}
