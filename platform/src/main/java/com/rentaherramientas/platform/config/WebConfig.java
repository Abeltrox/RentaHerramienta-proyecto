package com.rentaherramientas.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/** Publica las imágenes subidas en /uploads/** y activa las tareas programadas. */
@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.upload-dir}")
    private String directorioUploads;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = Path.of(directorioUploads).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(ubicacion.endsWith("/") ? ubicacion : ubicacion + "/");
    }
}
