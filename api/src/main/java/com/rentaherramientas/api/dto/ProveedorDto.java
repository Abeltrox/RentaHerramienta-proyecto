package com.rentaherramientas.api.dto;

import com.rentaherramientas.application.port.in.ProveedorUseCase.ProveedorComando;
import com.rentaherramientas.domain.model.Proveedor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ProveedorDto {

    private ProveedorDto() {
    }

    public record Request(
            @Schema(example = "Carlos Ruiz") @NotBlank @Size(max = 120) String nombre,
            @Schema(example = "ventas@maquirent.com") @NotBlank @Email @Size(max = 160) String email,
            @Schema(description = "Obligatoria al crear; opcional al actualizar", example = "Proveedor123*") @Size(min = 8, max = 72) String password,
            @Schema(example = "3157894561") @Size(max = 30) String telefono,
            @Schema(example = "MaquiRent S.A.S.") @NotBlank @Size(max = 150) String empresa,
            @Schema(example = "900123456-7") @NotBlank @Size(max = 30) String nit,
            @Schema(example = "Carrera 27 # 50-10") @Size(max = 200) String direccion,
            @Schema(example = "Bucaramanga") @Size(max = 80) String ciudad) {

        public ProveedorComando aComando() {
            return new ProveedorComando(nombre, email, password, telefono, empresa, nit, direccion, ciudad);
        }
    }

    public record Response(Long id, String nombre, String email, String telefono, boolean activo,
                           String empresa, String nit, String direccion, String ciudad) {
        public static Response de(Proveedor p) {
            return new Response(p.getId(), p.getUsuario().getNombre(), p.getUsuario().getEmail(), p.getUsuario().getTelefono(),
                    p.getUsuario().isActivo(), p.getEmpresa(), p.getNit(), p.getDireccion(), p.getCiudad());
        }
    }
}
