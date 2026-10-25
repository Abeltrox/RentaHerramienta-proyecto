package com.rentaherramientas.application.port.in;

import com.rentaherramientas.domain.model.Categoria;

import java.util.List;

public interface CategoriaUseCase {

    List<Categoria> listar();

    Categoria crear(CategoriaComando comando);

    Categoria actualizar(Long id, CategoriaComando comando);

    void eliminar(Long id);

    record CategoriaComando(String nombre, String descripcion) {
    }
}
