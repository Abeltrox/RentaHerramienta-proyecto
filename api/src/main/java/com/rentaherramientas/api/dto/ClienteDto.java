package com.rentaherramientas.api.dto;

import com.rentaherramientas.application.port.in.ClienteUseCase.ClienteComando;
import com.rentaherramientas.domain.model.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ClienteDto {

    private ClienteDto() {
    }

    public record Request(
            @Schema(example = "Laura Gómez") @NotBlank @Size(max = 120) String nombre,
            @Schema(example = "laura@correo.com") @NotBlank @Email @Size(max = 160) String email,
            @Schema(description = "Obligatoria al crear; opcional al actualizar", example = "Cliente123*") @Size(min = 8, max = 72) String password,
            @Schema(example = "3001234567") @Size(max = 30) String telefono,
            @Schema(example = "1098765432") @NotBlank @Size(max = 30) String documento,
            @Schema(example = "Calle 45 # 12-30") @Size(max = 200) String direccion,
            @Schema(example = "Bucaramanga") @Size(max = 80) String ciudad) {

        public ClienteComando aComando() {
            return new ClienteComando(nombre, email, password, telefono, documento, direccion, ciudad);
        }
    }

    public record Response(Long id, String nombre, String email, String telefono, boolean activo,
                           String documento, String direccion, String ciudad) {
        public static Response de(Cliente c) {
            return new Response(c.getId(), c.getUsuario().getNombre(), c.getUsuario().getEmail(), c.getUsuario().getTelefono(),
                    c.getUsuario().isActivo(), c.getDocumento(), c.getDireccion(), c.getCiudad());
        }
    }
}
