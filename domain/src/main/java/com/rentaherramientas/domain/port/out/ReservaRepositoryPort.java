package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Reserva;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepositoryPort {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(Long id);

    List<Reserva> listarTodas();

    List<Reserva> listarPorCliente(Long clienteId);

    List<Reserva> listarPorProveedor(Long proveedorId);

    List<Reserva> listarPorEstado(EstadoReserva estado);

    /**
     * ¿Existe otra reserva activa (pendiente, aprobada, pagada o entregada) que se cruce con el rango?
     *
     * @param excluirReservaId reserva a ignorar (use -1 si no aplica)
     */
    boolean existeSolapamiento(Long herramientaId, LocalDate inicio, LocalDate fin, Long excluirReservaId);

    boolean herramientaTieneReservasActivas(Long herramientaId);
}
