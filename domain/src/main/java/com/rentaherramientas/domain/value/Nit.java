package com.rentaherramientas.domain.value;

import java.util.regex.Pattern;

/**
 * Objeto de valor: NIT colombiano con formato {@code NUMERO-DV}. El dígito de verificación (DV)
 * se valida con el algoritmo oficial de la DIAN (pesos 3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71).
 */
public final class Nit {

    private static final Pattern FORMATO = Pattern.compile("^\\d{1,15}-\\d$");
    private static final int[] PESOS = {3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71};

    private final String valor;

    private Nit(String valor) {
        this.valor = valor;
    }

    public static Nit of(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El NIT no puede estar vacío");
        }
        String limpio = texto.trim();
        if (!FORMATO.matcher(limpio).matches()) {
            throw new IllegalArgumentException("Formato de NIT inválido (se espera NUMERO-DV): " + texto);
        }
        String[] partes = limpio.split("-");
        if (calcularDigitoVerificacion(partes[0]) != Integer.parseInt(partes[1])) {
            throw new IllegalArgumentException("El dígito de verificación del NIT no es correcto: " + texto);
        }
        return new Nit(limpio);
    }

    public static boolean esValido(String texto) {
        try {
            of(texto);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static int calcularDigitoVerificacion(String numero) {
        int suma = 0;
        for (int i = 0; i < numero.length(); i++) {
            int digito = numero.charAt(numero.length() - 1 - i) - '0';
            suma += digito * PESOS[i];
        }
        int residuo = suma % 11;
        return residuo > 1 ? 11 - residuo : residuo;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Nit otro && valor.equals(otro.valor));
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
