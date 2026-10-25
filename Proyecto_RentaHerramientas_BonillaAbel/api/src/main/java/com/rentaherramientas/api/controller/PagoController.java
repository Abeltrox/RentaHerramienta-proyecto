package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.PagoDto.PagarRequest;
import com.rentaherramientas.api.dto.PagoDto.Response;
import com.rentaherramientas.api.dto.PagoDto.ResultadoResponse;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.PagoUseCase.PagarComando;
import com.rentaherramientas.application.port.in.PagoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Pago en línea (pasarela simulada) y su historial")
public class PagoController {

    private final PagoUseCase pagos;

    @Operation(summary = "Pagar una reserva aprobada (CLIENTE)",
            description = "Cobra subtotal + IVA 19 %, marca la reserva como PAGADA y emite la factura automáticamente. "
                    + "En el sandbox, `ultimosDigitos: \"0000\"` simula un pago rechazado.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public ResultadoResponse pagar(@Valid @RequestBody PagarRequest r) {
        return ResultadoResponse.de(pagos.pagar(new PagarComando(r.reservaId(), r.metodo(), r.ultimosDigitos()), ActorResolver.actual()));
    }

    @Operation(summary = "Historial de pagos según el rol",
            description = "ADMIN: todos · PROVEEDOR: los de sus herramientas · CLIENTE: los suyos.")
    @GetMapping
    public List<Response> listar() {
        return pagos.listar(ActorResolver.actual()).stream().map(Response::de).toList();
    }

    @Operation(summary = "Detalle de un pago")
    @GetMapping("/{id}")
    public Response obtener(@PathVariable Long id) {
        return Response.de(pagos.obtener(id, ActorResolver.actual()));
    }
}
