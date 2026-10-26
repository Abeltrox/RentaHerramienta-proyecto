package com.rentaherramientas.platform.scheduler;

import com.rentaherramientas.application.port.in.ReservaUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Adaptador de entrada programado: vence reservas sin atender o sin pagar (historial de pagos y vencimientos). */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaVencimientoScheduler {

    private final ReservaUseCase reservas;

    @Scheduled(fixedDelayString = "${app.reservas.revision-vencimiento-ms}", initialDelayString = "${app.reservas.revision-vencimiento-ms}")
    public void revisarVencimientos() {
        int vencidas = reservas.expirarVencidas();
        if (vencidas > 0) {
            log.info("Reservas vencidas automáticamente: {}", vencidas);
        }
    }
}
