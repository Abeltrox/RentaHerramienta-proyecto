package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.FiltroHerramienta;
import com.rentaherramientas.domain.model.Herramienta;

import java.util.List;
import java.util.Optional;

public interface HerramientaRepositoryPort {

    Herramienta guardar(Herramienta herramienta);

    Optional<Herramienta> buscarPorId(Long id);

    /** Igual que buscarPorId pero bloquea la fila (evita dos reservas simultáneas sobre el mismo equipo). */
    Optional<Herramienta> buscarPorIdParaActualizar(Long id);

    /** Excluye siempre las herramientas INACTIVAS (eliminadas lógicamente). */
    List<Herramienta> buscar(FiltroHerramienta filtro);

    List<Herramienta> listarPorProveedor(Long proveedorId);

    boolean existePorCategoria(Long categoriaId);
}
