package com.rentaherramientas.platform.security;

import com.rentaherramientas.api.security.AuthenticatedUser;
import com.rentaherramientas.domain.model.Rol;
import com.rentaherramientas.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = Base64.getEncoder().encodeToString(new byte[64]);
    private final Usuario usuario = Usuario.builder().id(7L).email("a@b.com").rol(Rol.PROVEEDOR).activo(true).build();

    @Test
    @DisplayName("Un token generado se valida y devuelve id, correo y rol")
    void generaYValida() {
        JwtService jwt = new JwtService(SECRETO, 30);

        Optional<AuthenticatedUser> u = jwt.validar(jwt.generar(usuario));

        assertThat(u).isPresent();
        assertThat(u.get().id()).isEqualTo(7L);
        assertThat(u.get().rol()).isEqualTo(Rol.PROVEEDOR);
        assertThat(u.get().email()).isEqualTo("a@b.com");
    }

    @Test
    @DisplayName("Un token vencido no es válido")
    void tokenVencido() {
        JwtService jwt = new JwtService(SECRETO, -1);
        assertThat(jwt.validar(jwt.generar(usuario))).isEmpty();
    }

    @Test
    @DisplayName("Un token manipulado o firmado con otra clave no es válido")
    void tokenManipulado() {
        JwtService jwt = new JwtService(SECRETO, 30);
        String token = jwt.generar(usuario);
        byte[] otraClave = new byte[64];
        Arrays.fill(otraClave, (byte) 1);
        JwtService otro = new JwtService(Base64.getEncoder().encodeToString(otraClave), 30);

        assertThat(jwt.validar(token + "x")).isEmpty();
        assertThat(otro.validar(token)).isEmpty();
        assertThat(jwt.validar("no-es-un-jwt")).isEmpty();
    }

    @Test
    @DisplayName("Exige un secreto de al menos 256 bits")
    void secretoCorto() {
        String corto = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new JwtService(corto, 30)).isInstanceOf(IllegalStateException.class);
    }
}
