package com.rentaherramientas.api.controller;

import com.rentaherramientas.api.dto.AuthDto.AuthResponse;
import com.rentaherramientas.api.dto.AuthDto.LoginRequest;
import com.rentaherramientas.api.dto.AuthDto.RegistroRequest;
import com.rentaherramientas.api.dto.AuthDto.UsuarioResponse;
import com.rentaherramientas.api.security.ActorResolver;
import com.rentaherramientas.application.port.in.AuthUseCase.RegistroClienteComando;
import com.rentaherramientas.application.port.in.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro de clientes e inicio de sesión con JWT")
public class AuthController {

    private final AuthUseCase auth;

    @Operation(summary = "Registrar un cliente (público)", description = "Crea la cuenta con rol CLIENTE y devuelve el token JWT.")
    @SecurityRequirements
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registro(@Valid @RequestBody RegistroRequest r) {
        return AuthResponse.de(auth.registrarCliente(new RegistroClienteComando(
                r.nombre(), r.email(), r.password(), r.telefono(), r.documento(), r.direccion(), r.ciudad())));
    }

    @Operation(summary = "Iniciar sesión", description = "Devuelve el JWT que se envía luego en `Authorization: Bearer <token>`.")
    @SecurityRequirements
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return AuthResponse.de(auth.login(r.email(), r.password()));
    }

    @Operation(summary = "Datos del usuario autenticado")
    @GetMapping("/me")
    public UsuarioResponse me() {
        return UsuarioResponse.de(auth.perfil(ActorResolver.actual().usuarioId()));
    }
}
