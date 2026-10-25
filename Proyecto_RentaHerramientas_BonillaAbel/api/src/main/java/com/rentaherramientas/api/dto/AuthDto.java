package com.rentaherramientas.api.dto;

import com.rentaherramientas.application.port.in.AuthUseCase.AuthResultado;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(
            @Schema(example = "admin@rentaherramientas.com") @NotBlank @Email String email,
            @Schema(example = "Admin123*") @NotBlank String password) {
    }

    public record RegistroRequest(
            @Schema(example = "Laura Gómez") @NotBlank @Size(max = 120) String nombre,
            @Schema(example = "laura@correo.com") @NotBlank @Email @Size(max = 160) String email,
            @Schema(example = "Cliente123*") @NotBlank @Size(min = 8, max = 72) String password,
            @Schema(example = "3001234567") @Size(max = 30) String telefono,
            @Schema(example = "1098765432") @NotBlank @Size(max = 30) String documento,
            @Schema(example = "Calle 45 # 12-30") @Size(max = 200) String direccion,
            @Schema(example = "Bucaramanga") @Size(max = 80) String ciudad) {
    }

    public record UsuarioResponse(Long id, String nombre, String email, String telefono, Rol rol, boolean activo) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(), u.getRol(), u.isActivo());
        }
    }

    public record AuthResponse(String token, String tipo, long expiraEnSegundos, UsuarioResponse usuario) {
        public static AuthResponse de(AuthResultado r) {
            return new AuthResponse(r.token(), "Bearer", r.expiraEnSegundos(), UsuarioResponse.de(r.usuario()));
        }
    }
}
