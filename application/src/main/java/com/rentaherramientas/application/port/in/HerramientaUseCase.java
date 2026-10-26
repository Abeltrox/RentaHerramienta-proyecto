package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.FiltroHerramienta;
import com.rentaherramientas.domain.model.Herramienta;

import java.math.BigDecimal;
import java.util.List;

/** Caso de uso: inventario de herramientas y exploración del catálogo. */
public interface HerramientaUseCase {

    Herramienta crear(HerramientaComando comando, Actor actor);

    Herramienta actualizar(Long id, HerramientaComando comando, Actor actor);

    /** Eliminación lógica. Falla si la herramienta tiene reservas activas. */
    void eliminar(Long id, Actor actor);

    Herramienta obtener(Long id);

    /** Catálogo (búsqueda con filtros). */
    List<Herramienta> buscar(FiltroHerramienta filtro);

    /** Inventario gestionable: todo para el admin, solo lo propio para el proveedor. */
    List<Herramienta> listarGestionables(Actor actor);

    Herramienta subirImagen(Long id, String contentType, byte[] contenido, Actor actor);

    /**
     * @param proveedorId solo lo usa el administrador (indica a qué proveedor pertenece el equipo);
     *                    para un proveedor se toma siempre su propio id.
     */
    record HerramientaComando(Long proveedorId, Long categoriaId, String nombre, String descripcion,
                              String marca, BigDecimal precioDia, EstadoHerramienta estado,
                              boolean reservaAutomatica) {
    }
}
