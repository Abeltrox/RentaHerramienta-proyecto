package com.rentaherramientas.application.service;

import com.rentaherramientas.application.port.in.AuthUseCase;
import com.rentaherramientas.domain.exception.AutenticacionException;
import com.rentaherramientas.domain.exception.ConflictoException;
import com.rentaherramientas.domain.exception.RecursoNoEncontradoException;
import com.rentaherramientas.domain.model.Cliente;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.model.Usuario;
import com.rentaherramientas.domain.port.out.ClienteRepositoryPort;
import com.rentaherramientas.domain.port.out.PasswordHasherPort;
import com.rentaherramientas.domain.port.out.TokenProviderPort;
import com.rentaherramientas.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements AuthUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final ClienteRepositoryPort clientes;
    private final PasswordHasherPort hasher;
    private final TokenProviderPort tokens;

    @Override
    public AuthResultado registrarCliente(RegistroClienteComando c) {
        String email = c.email().trim().toLowerCase();
        if (usuarios.existePorEmail(email)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        if (clientes.existePorDocumento(c.documento().trim())) {
            throw new ConflictoException("El documento ya está registrado");
        }
        Usuario usuario = usuarios.guardar(Usuario.builder()
                .nombre(c.nombre().trim())
                .email(email)
                .password(hasher.encriptar(c.password()))
                .telefono(c.telefono())
                .rol(Rol.CLIENTE)
                .activo(true)
                .build());
        clientes.guardar(Cliente.builder()
                .id(usuario.getId())
                .usuario(usuario)
                .documento(c.documento().trim())
                .direccion(c.direccion())
                .ciudad(c.ciudad())
                .build());
        return new AuthResultado(tokens.generar(usuario), tokens.expiracionSegundos(), usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResultado login(String email, String password) {
        // Mensaje genérico a propósito: no revela si el correo existe o no.
        Usuario usuario = usuarios.buscarPorEmail(email.trim().toLowerCase())
                .filter(Usuario::isActivo)
                .filter(u -> hasher.coincide(password, u.getPassword()))
                .orElseThrow(() -> new AutenticacionException("Correo o contraseña incorrectos"));
        return new AuthResultado(tokens.generar(usuario), tokens.expiracionSegundos(), usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario perfil(Long usuarioId) {
        return usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Usuario", usuarioId));
    }
}
