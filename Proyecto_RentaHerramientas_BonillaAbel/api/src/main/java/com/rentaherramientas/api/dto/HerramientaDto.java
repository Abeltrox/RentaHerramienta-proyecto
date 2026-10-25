package com.rentaherramientas.api.dto;

import com.rentaherramientas.application.port.in.HerramientaUseCase.HerramientaComando;
import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.Herramienta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class HerramientaDto {

    private HerramientaDto() {
    }

    public record Request(
            @Schema(description = "Solo lo usa el administrador; el proveedor siempre crea a su nombre") Long proveedorId,
            @Schema(example = "1") @NotNull Long categoriaId,
            @Schema(example = "Rotomartillo SDS Plus 800W") @NotBlank @Size(max = 150) String nombre,
            @Schema(example = "Rotomartillo con 3 modos, incluye brocas") @Size(max = 1000) String descripcion,
            @Schema(example = "Bosch") @Size(max = 80) String marca,
            @Schema(example = "45000") @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal precioDia,
            @Schema(example = "DISPONIBLE") EstadoHerramienta estado,
            @Schema(description = "true = las reservas se aprueban solas", example = "false") boolean reservaAutomatica) {

        public HerramientaComando aComando() {
            return new HerramientaComando(proveedorId, categoriaId, nombre, descripcion, marca, precioDia, estado, reservaAutomatica);
        }
    }

    public record Response(Long id, Long proveedorId, String proveedor, Long categoriaId, String categoria,
                           String nombre, String descripcion, String marca, BigDecimal precioDia,
                           EstadoHerramienta estado, String imagenUrl, boolean reservaAutomatica) {
        public static Response de(Herramienta h) {
            return new Response(h.getId(), h.getProveedorId(), h.getProveedorNombre(), h.getCategoriaId(), h.getCategoriaNombre(),
                    h.getNombre(), h.getDescripcion(), h.getMarca(), h.getPrecioDia(), h.getEstado(), h.getImagenUrl(),
                    h.isReservaAutomatica());
        }
    }
}
