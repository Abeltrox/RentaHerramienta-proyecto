package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Proveedor;

import java.util.List;

/** Caso de uso: gestión de proveedores (CRUD). */
public interface ProveedorUseCase {

    Proveedor crear(ProveedorComando comando);

    Proveedor actualizar(Long id, ProveedorComando comando, Actor actor);

    /** Eliminación lógica: desactiva al usuario y retira sus herramientas del catálogo. */
    void eliminar(Long id);

    List<Proveedor> listar();

    Proveedor obtener(Long id, Actor actor);

    /** {@code password} es obligatoria al crear y opcional al actualizar. */
    record ProveedorComando(String nombre, String email, String password, String telefono,
                            String empresa, String nit, String direccion, String ciudad) {
    }
}
