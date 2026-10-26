package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.Cliente;
import com.rentaherramientas.domain.port.out.ClienteRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.ClienteEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.ClienteJpaRepository;
import com.rentaherramientas.infrastructure.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientePersistenceAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpa;
    private final UsuarioJpaRepository usuarios;

    @Override
    public Cliente guardar(Cliente c) {
        ClienteEntity e = jpa.findById(c.getId()).orElseGet(ClienteEntity::new);
        e.setId(c.getId());
        e.setUsuario(usuarios.getReferenceById(c.getId()));
        e.setDocumento(c.getDocumento());
        e.setDireccion(c.getDireccion());
        e.setCiudad(c.getCiudad());
        return PersistenceMappers.cliente(jpa.save(e));
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::cliente);
    }

    @Override
    public List<Cliente> listar() {
        return jpa.findAllByOrderByIdAsc().stream().map(PersistenceMappers::cliente).toList();
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return jpa.existsByDocumento(documento);
    }

    @Override
    public boolean existePorDocumentoYIdDistinto(String documento, Long id) {
        return jpa.existsByDocumentoAndIdNot(documento, id);
    }
}
