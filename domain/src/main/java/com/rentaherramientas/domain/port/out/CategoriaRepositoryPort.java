package com.rentaherramientas.domain.port.out;

import com.rentaherramientas.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositoryPort {

    Categoria guardar(Categoria categoria);

    Optional<Categoria> buscarPorId(Long id);

    List<Categoria> listar();

    boolean existePorNombre(String nombre);

    boolean existePorNombreYIdDistinto(String nombre, Long id);

    void eliminar(Long id);
}
