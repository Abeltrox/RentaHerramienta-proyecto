package com.rentaherramientas.api.dto;

import com.rentaherramientas.domain.model.EstadoDanio;
import com.rentaherramientas.domain.model.ReporteDanio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DanioDto {

    private DanioDto() {
    }

    public record CrearRequest(
            @Schema(example = "1") @NotNull Long reservaId,
            @Schema(example = "El disco de corte llegó fisurado") @NotBlank @Size(max = 500) String descripcion,
            @Schema(example = "80000") @PositiveOrZero BigDecimal costoEstimado) {
    }

    public record EstadoRequest(@Schema(example = "RESUELTO") @NotNull EstadoDanio estado) {
    }

    public record Response(Long id, Long reservaId, Long herramientaId, String herramienta, String descripcion,
                           BigDecimal costoEstimado, EstadoDanio estado, LocalDateTime fechaReporte, LocalDateTime fechaResolucion) {
        public static Response de(ReporteDanio d) {
            return new Response(d.getId(), d.getReservaId(), d.getHerramientaId(), d.getHerramientaNombre(), d.getDescripcion(),
                    d.getCostoEstimado(), d.getEstado(), d.getFechaReporte(), d.getFechaResolucion());
        }
    }
}
