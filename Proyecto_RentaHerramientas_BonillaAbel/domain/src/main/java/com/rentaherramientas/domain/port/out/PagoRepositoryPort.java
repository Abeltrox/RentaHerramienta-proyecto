package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoRepositoryPort {

    Pago guardar(Pago pago);

    Optional<Pago> buscarPorId(Long id);

    Optional<Pago> buscarPorReservaId(Long reservaId);

    List<Pago> listarTodos();

    List<Pago> listarPorCliente(Long clienteId);

    List<Pago> listarPorProveedor(Long proveedorId);
}
