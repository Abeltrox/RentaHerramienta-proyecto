package com.rentaherramientas.infrastructure.persistence.adapter;

import com.rentaherramientas.domain.model.Usuario;
import com.rentaherramientas.domain.port.out.UsuarioRepositoryPort;
import com.rentaherramientas.infrastructure.persistence.entity.UsuarioEntity;
import com.rentaherramientas.infrastructure.persistence.mapper.PersistenceMappers;
import com.rentaherramientas.infrastructure.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository jpa;

    @Override
    public Usuario guardar(Usuario u) {
        UsuarioEntity e = u.getId() == null ? new UsuarioEntity() : jpa.findById(u.getId()).orElseGet(UsuarioEntity::new);
        e.setNombre(u.getNombre());
        e.setEmail(u.getEmail());
        e.setPassword(u.getPassword());
        e.setTelefono(u.getTelefono());
        e.setRol(u.getRol());
        e.setActivo(u.isActivo());
        return PersistenceMappers.usuario(jpa.save(e));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenceMappers::usuario);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpa.findByEmailIgnoreCase(email).map(PersistenceMappers::usuario);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpa.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existePorEmailYIdDistinto(String email, Long id) {
        return jpa.existsByEmailIgnoreCaseAndIdNot(email, id);
    }
}
