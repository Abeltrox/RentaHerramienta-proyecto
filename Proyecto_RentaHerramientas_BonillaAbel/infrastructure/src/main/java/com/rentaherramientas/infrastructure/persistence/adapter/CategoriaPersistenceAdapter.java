package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.Categoria;
import com.rentaherramientas.domain.port.out.CategoriaRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.CategoriaEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.CategoriaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoriaPersistenceAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository jpa;

    @Override
    public Categoria guardar(Categoria c) {
        CategoriaEntity e = c.getId() == null ? new CategoriaEntity() : jpa.findById(c.getId()).orElseGet(CategoriaEntity::new);
        e.setNombre(c.getNombre());
        e.setDescripcion(c.getDescripcion());
        return PersistenceMappers.categoria(jpa.save(e));
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::categoria);
    }

    @Override
    public List<Categoria> listar() {
        return jpa.findAllByOrderByNombreAsc().stream().map(PersistenceMappers::categoria).toList();
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return jpa.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public boolean existePorNombreYIdDistinto(String nombre, Long id) {
        return jpa.existsByNombreIgnoreCaseAndIdNot(nombre, id);
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
    }
}
