package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Factura;

import java.util.List;
import java.util.Optional;

public interface FacturaRepositoryPort {

    Factura guardar(Factura factura);

    Optional<Factura> buscarPorId(Long id);

    Optional<Factura> buscarPorReservaId(Long reservaId);

    List<Factura> listarTodas();

    List<Factura> listarPorCliente(Long clienteId);

    List<Factura> listarPorProveedor(Long proveedorId);

    /** Siguiente número consecutivo de factura (secuencia de base de datos). */
    long siguienteConsecutivo();
}
