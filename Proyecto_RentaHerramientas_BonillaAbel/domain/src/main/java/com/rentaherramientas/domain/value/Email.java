package com.rentaherramientas.domain.value;

import java.util.Locale;
import java.util.regex.Pattern;

/** Objeto de valor: correo electrónico normalizado (sin espacios y en minúsculas). */
public final class Email {

    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String valor;

    private Email(String valor) {
        this.valor = valor;
    }

    public static Email of(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        String normalizado = texto.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("Formato de correo inválido: " + texto);
        }
        return new Email(normalizado);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Email otro && valor.equals(otro.valor));
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    @Override
    public String toString() {
        return valor;
    }
}
