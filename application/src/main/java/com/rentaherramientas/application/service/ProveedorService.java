package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.ProveedorUseCase;
import com.rentaherramientas.domain.exception.ConflictoException;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.EstadoHerramienta;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Proveedor;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.model.Usuario;
import com.rentaherramientas.domain.port.out.HerramientaRepositoryPort;
import com.rentaherramientas.domain.port.out.PasswordHasherPort;
import com.rentaherramientas.domain.port.out.ProveedorRepositoryPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import com.rentaherramientas.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProveedorService implements ProveedorUseCase {

    private final ProveedorRepositoryPort proveedores;
    private final UsuarioRepositoryPort usuarios;
    private final HerramientaRepositoryPort herramientas;
    private final ReservaRepositoryPort reservas;
    private final PasswordHasherPort hasher;

    @Override
    public Proveedor crear(ProveedorComando c) {
        String email = c.email().trim().toLowerCase();
        if (usuarios.existePorEmail(email)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        if (proveedores.existePorNit(c.nit().trim())) {
            throw new ConflictoException("El NIT ya está registrado");
        }
        if (c.password() == null || c.password().length() < 8) {
            throw new ReglaNegocioException("La contraseña es obligatoria y debe tener mínimo 8 caracteres");
        }
        Usuario usuario = usuarios.guardar(Usuario.builder()
                .nombre(c.nombre().trim())
                .email(email)
                .password(hasher.encriptar(c.password()))
                .telefono(c.telefono())
                .rol(Rol.PROVEEDOR)
                .activo(true)
                .build());
        return proveedores.guardar(Proveedor.builder()
                .id(usuario.getId())
                .usuario(usuario)
                .empresa(c.empresa().trim())
                .nit(c.nit().trim())
                .direccion(c.direccion())
                .ciudad(c.ciudad())
                .build());
    }

    @Override
    public Proveedor actualizar(Long id, ProveedorComando c, Actor actor) {
        if (!actor.puedeGestionar(id)) {
            throw new OperacionNoPermitidaException("Solo puedes modificar tu propio perfil");
        }
        Proveedor proveedor = cargar(id);
        Usuario usuario = proveedor.getUsuario();
        String email = c.email().trim().toLowerCase();
        if (usuarios.existePorEmailYIdDistinto(email, usuario.getId())) {
            throw new ConflictoException("El correo ya está registrado");
        }
        if (proveedores.existePorNitYIdDistinto(c.nit().trim(), id)) {
            throw new ConflictoException("El NIT ya está registrado");
        }
        usuario.setNombre(c.nombre().trim());
        usuario.setEmail(email);
        usuario.setTelefono(c.telefono());
        if (c.password() != null && !c.password().isBlank()) {
            if (c.password().length() < 8) {
                throw new ReglaNegocioException("La contraseña debe tener mínimo 8 caracteres");
            }
            usuario.setPassword(hasher.encriptar(c.password()));
        }
        usuarios.guardar(usuario);
        proveedor.setEmpresa(c.empresa().trim());
        proveedor.setNit(c.nit().trim());
        proveedor.setDireccion(c.direccion());
        proveedor.setCiudad(c.ciudad());
        return proveedores.guardar(proveedor);
    }

    @Override
    public void eliminar(Long id) {
        Proveedor proveedor = cargar(id);
        boolean conReservasActivas = reservas.listarPorProveedor(id).stream()
                .anyMatch(r -> EstadoReserva.bloqueantes().contains(r.getEstado()));
        if (conReservasActivas) {
            throw new ReglaNegocioException("El proveedor tiene reservas activas; ciérralas antes de eliminarlo");
        }
        // Eliminación lógica: se conserva el historial de alquileres y facturas.
        herramientas.listarPorProveedor(id).forEach(h -> {
            h.setEstado(EstadoHerramienta.INACTIVA);
            herramientas.guardar(h);
        });
        proveedor.getUsuario().setActivo(false);
        usuarios.guardar(proveedor.getUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listar() {
        return proveedores.listar();
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor obtener(Long id, Actor actor) {
        if (!actor.puedeGestionar(id)) {
            throw new OperacionNoPermitidaException("No puedes consultar este proveedor");
        }
        return cargar(id);
    }

    private Proveedor cargar(Long id) {
        return proveedores.buscarPorId(id).orElseThrow(() -> RecursoNoEncontradoException.de("Proveedor", id));
    }
}
