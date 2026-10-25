package com.rentaherramientas.domain.port.out;

/** Puerto de salida: cifrado de contraseñas (la implementación usa BCrypt). */
public interface PasswordHasherPort {

    String encriptar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String hash);
}
