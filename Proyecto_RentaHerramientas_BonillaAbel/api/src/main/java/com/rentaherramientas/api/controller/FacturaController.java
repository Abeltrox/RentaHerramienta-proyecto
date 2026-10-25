package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.FacturaDto.Response;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.FacturaUseCase;
import com.rentaherramientas.domain.model.Factura;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@Tag(name = "Facturas", description = "Facturación automática y descarga en PDF")
public class FacturaController {

    private final FacturaUseCase facturas;

    @Operation(summary = "Listar facturas según el rol")
    @GetMapping
    public List<Response> listar() {
        return facturas.listar(ActorResolver.actual()).stream().map(Response::de).toList();
    }

    @Operation(summary = "Detalle de una factura")
    @GetMapping("/{id:\\d+}")
    public Response obtener(@PathVariable Long id) {
        return Response.de(facturas.obtener(id, ActorResolver.actual()));
    }

    @Operation(summary = "Factura de una reserva")
    @GetMapping("/reserva/{reservaId}")
    public Response porReserva(@PathVariable Long reservaId) {
        return Response.de(facturas.obtenerPorReserva(reservaId, ActorResolver.actual()));
    }

    @Operation(summary = "Emitir la factura de una reserva pagada (PROVEEDOR dueño o ADMIN)",
            description = "Idempotente: si ya existe devuelve la misma factura. Normalmente se emite sola al pagar.")
    @PostMapping("/reserva/{reservaId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PROVEEDOR')")
    public Response generar(@PathVariable Long reservaId) {
        return Response.de(facturas.generar(reservaId, ActorResolver.actual()));
    }

    @Operation(summary = "Descargar factura en PDF")
    @GetMapping("/{id:\\d+}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        Factura factura = facturas.obtener(id, ActorResolver.actual());
        byte[] contenido = facturas.descargarPdf(id, ActorResolver.actual());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(factura.getNumero() + ".pdf").build().toString())
                .body(contenido);
    }
}
