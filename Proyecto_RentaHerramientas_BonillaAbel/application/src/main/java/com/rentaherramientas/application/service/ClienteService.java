package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.ClienteUseCase;
import com.rentaherramientas.domain.exception.ConflictoException;
import com.rentaherramientas.domain.exception.OperacionNoPermitidaException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.model.Actor;
import com.rentaherramientas.domain.model.Cliente;
import com.rentaherramientas.domain.model.EstadoReserva;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.model.Usuario;
import com.rentaherramientas.domain.port.out.ClienteRepositoryPort;
import com.rentaherramientas.domain.port.out.PasswordHasherPort;
import com.rentaherramientas.domain.port.out.ReservaRepositoryPort;
import com.rentaherramientas.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService implements ClienteUseCase {

    private final ClienteRepositoryPort clientes;
    private final UsuarioRepositoryPort usuarios;
    private final ReservaRepositoryPort reservas;
    private final PasswordHasherPort hasher;

    @Override
    public Cliente crear(ClienteComando c) {
        String email = c.email().trim().toLowerCase();
        if (usuarios.existePorEmail(email)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        if (clientes.existePorDocumento(c.documento().trim())) {
            throw new ConflictoException("El documento ya está registrado");
        }
        if (c.password() == null || c.password().length() < 8) {
            throw new ReglaNegocioException("La contraseña es obligatoria y debe tener mínimo 8 caracteres");
        }
        Usuario usuario = usuarios.guardar(Usuario.builder()
                .nombre(c.nombre().trim())
                .email(email)
                .password(hasher.encriptar(c.password()))
                .telefono(c.telefono())
                .rol(Rol.CLIENTE)
                .activo(true)
                .build());
        return clientes.guardar(Cliente.builder()
                .id(usuario.getId())
                .usuario(usuario)
                .documento(c.documento().trim())
                .direccion(c.direccion())
                .ciudad(c.ciudad())
                .build());
    }

    @Override
    public Cliente actualizar(Long id, ClienteComando c, Actor actor) {
        if (!actor.puedeGestionar(id)) {
            throw new OperacionNoPermitidaException("Solo puedes modificar tu propio perfil");
        }
        Cliente cliente = cargar(id);
        Usuario usuario = cliente.getUsuario();
        String email = c.email().trim().toLowerCase();
        if (usuarios.existePorEmailYIdDistinto(email, usuario.getId())) {
            throw new ConflictoException("El correo ya está registrado");
        }
        if (clientes.existePorDocumentoYIdDistinto(c.documento().trim(), id)) {
            throw new ConflictoException("El documento ya está registrado");
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
        cliente.setDocumento(c.documento().trim());
        cliente.setDireccion(c.direccion());
        cliente.setCiudad(c.ciudad());
        return clientes.guardar(cliente);
    }

    @Override
    public void eliminar(Long id) {
        Cliente cliente = cargar(id);
        boolean conReservasActivas = reservas.listarPorCliente(id).stream()
                .anyMatch(r -> EstadoReserva.bloqueantes().contains(r.getEstado()));
        if (conReservasActivas) {
            throw new ReglaNegocioException("El cliente tiene reservas activas; ciérralas antes de eliminarlo");
        }
        cliente.getUsuario().setActivo(false); // eliminación lógica
        usuarios.guardar(cliente.getUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clientes.listar();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtener(Long id, Actor actor) {
        if (!actor.puedeGestionar(id)) {
            throw new OperacionNoPermitidaException("No puedes consultar este cliente");
        }
        return cargar(id);
    }

    private Cliente cargar(Long id) {
        return clientes.buscarPorId(id).orElseThrow(() -> RecursoNoEncontradoException.de("Cliente", id));
    }
}
