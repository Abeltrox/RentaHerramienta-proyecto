package com.rentaherramientas.domain.value;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    @DisplayName("Normaliza espacios y mayúsculas")
    void normaliza() {
        assertThat(Email.of("  Laura@Correo.COM ").getValor()).isEqualTo("laura@correo.com");
    }

    @Test
    @DisplayName("Rechaza vacío, nulo y formatos inválidos")
    void rechazaInvalidos() {
        assertThatThrownBy(() -> Email.of(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Email.of("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Email.of("sin-arroba.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Email.of("a@b")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Dos correos equivalentes son iguales")
    void igualdad() {
        assertThat(Email.of("A@b.com")).isEqualTo(Email.of("a@b.com"));
    }
}
