package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.MetodoPago;

import java.math.BigDecimal;

/** Puerto de salida: pasarela de pagos. Hoy es simulada; mañana puede ser PayU, Wompi, Stripe... */
public interface PaymentGatewayPort {

    ResultadoPasarela procesar(SolicitudPago solicitud);

    record SolicitudPago(BigDecimal monto, MetodoPago metodo, String ultimosDigitos, String descripcion) {
    }

    record ResultadoPasarela(boolean aprobado, String referencia, String mensaje) {
    }
}
