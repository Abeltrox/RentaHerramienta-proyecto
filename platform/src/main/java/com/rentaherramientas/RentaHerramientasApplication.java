package com.rentaherramientas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 * Tomcat embebido se levanta automáticamente en el puerto configurado (server.port).
 */
@SpringBootApplication
public class RentaHerramientasApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentaHerramientasApplication.class, args);
    }
}
