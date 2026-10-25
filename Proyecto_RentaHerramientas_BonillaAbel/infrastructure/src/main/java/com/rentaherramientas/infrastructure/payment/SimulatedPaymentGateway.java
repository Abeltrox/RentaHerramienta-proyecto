package com.rentaherramientas.infrastructure.payment;

import com.rentaherramientas.domain.port.out.PaymentGatewayPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Pasarela de pagos SIMULADA (sandbox) para el proyecto académico. No mueve dinero real.
 * Regla de prueba: un pago con tarjeta cuyos últimos 4 dígitos sean "0000" es rechazado;
 * todo lo demás se aprueba. Para producción basta con crear otro adaptador (Wompi, PayU, Stripe...)
 * que implemente {@link PaymentGatewayPort}; el dominio no cambia.
 */
@Component
public class SimulatedPaymentGateway implements PaymentGatewayPort {

    @Override
    public ResultadoPasarela procesar(SolicitudPago solicitud) {
        if ("0000".equals(solicitud.ultimosDigitos())) {
            return new ResultadoPasarela(false, null, "Fondos insuficientes (tarjeta de prueba 0000)");
        }
        String referencia = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return new ResultadoPasarela(true, referencia, "Aprobado");
    }
}
