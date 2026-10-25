package com.rentaherramientas.platform.security;

import com.rentaherramientas.domain.port.out.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador de salida: cifrado de contraseñas con BCrypt. */
@Component
@RequiredArgsConstructor
public class BcryptPasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder encoder;

    @Override
    public String encriptar(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String hash) {
        return encoder.matches(contrasenaPlana, hash);
    }
}
