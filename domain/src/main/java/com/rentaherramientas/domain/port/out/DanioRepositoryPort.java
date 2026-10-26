package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.ReporteDanio;

import java.util.List;
import java.util.Optional;

public interface DanioRepositoryPort {

    ReporteDanio guardar(ReporteDanio reporte);

    Optional<ReporteDanio> buscarPorId(Long id);

    List<ReporteDanio> listarTodos();

    List<ReporteDanio> listarPorProveedor(Long proveedorId);
}
