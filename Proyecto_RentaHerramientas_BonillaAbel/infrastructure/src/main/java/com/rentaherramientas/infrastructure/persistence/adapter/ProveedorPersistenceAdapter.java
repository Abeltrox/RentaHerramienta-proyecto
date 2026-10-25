package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.Proveedor;
import com.rentaherramientas.domain.port.out.ProveedorRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.ProveedorEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.ProveedorJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProveedorPersistenceAdapter implements ProveedorRepositoryPort {

    private final ProveedorJpaRepository jpa;
    private final UsuarioJpaRepository usuarios;

    @Override
    public Proveedor guardar(Proveedor p) {
        ProveedorEntity e = jpa.findById(p.getId()).orElseGet(ProveedorEntity::new);
        e.setId(p.getId());
        // La asociación es de solo lectura (comparte PK), pero debe quedar poblada para mapear el resultado.
        e.setUsuario(usuarios.getReferenceById(p.getId()));
        e.setEmpresa(p.getEmpresa());
        e.setNit(p.getNit());
        e.setDireccion(p.getDireccion());
        e.setCiudad(p.getCiudad());
        return PersistenceMappers.proveedor(jpa.save(e));
    }

    @Override
    public Optional<Proveedor> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::proveedor);
    }

    @Override
    public List<Proveedor> listar() {
        return jpa.findAllByOrderByEmpresaAsc().stream().map(PersistenceMappers::proveedor).toList();
    }

    @Override
    public boolean existePorNit(String nit) {
        return jpa.existsByNit(nit);
    }

    @Override
    public boolean existePorNitYIdDistinto(String nit, Long id) {
        return jpa.existsByNitAndIdNot(nit, id);
    }
}
