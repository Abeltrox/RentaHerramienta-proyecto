package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.CategoriaUseCase;
import com.rentaherramientas.domain.exception.ConflictoException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Categoria;
import com.rentaherramientas.domain.port.out.CategoriaRepositoryPort;
import com.rentaherramientas.domain.port.out.HerramientaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService implements CategoriaUseCase {

    private final CategoriaRepositoryPort categorias;
    private final HerramientaRepositoryPort herramientas;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categorias.listar();
    }

    @Override
    public Categoria crear(CategoriaComando c) {
        String nombre = c.nombre().trim();
        if (categorias.existePorNombre(nombre)) {
            throw new ConflictoException("Ya existe una categoría con ese nombre");
        }
        return categorias.guardar(Categoria.builder().nombre(nombre).descripcion(c.descripcion()).build());
    }

    @Override
    public Categoria actualizar(Long id, CategoriaComando c) {
        Categoria categoria = categorias.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Categoría", id));
        String nombre = c.nombre().trim();
        if (categorias.existePorNombreYIdDistinto(nombre, id)) {
            throw new ConflictoException("Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(c.descripcion());
        return categorias.guardar(categoria);
    }

    @Override
    public void eliminar(Long id) {
        categorias.buscarPorId(id).orElseThrow(() -> RecursoNoEncontradoException.de("Categoría", id));
        if (herramientas.existePorCategoria(id)) {
            throw new ReglaNegocioException("No se puede eliminar: hay herramientas que usan esta categoría");
        }
        categorias.eliminar(id);
    }
}
