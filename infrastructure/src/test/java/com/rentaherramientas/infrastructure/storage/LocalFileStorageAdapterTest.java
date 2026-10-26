package com.rentaherramientas.infrastructure.storage;

import com.rentaherramientas.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageAdapterTest {

    @TempDir Path carpeta;

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};

    @Test
    @DisplayName("Guarda una imagen JPEG real y la puede eliminar")
    void guardaYElimina() throws IOException {
        LocalFileStorageAdapter almacen = new LocalFileStorageAdapter(carpeta.toString());

        String url = almacen.guardarImagen("image/jpeg", JPEG);

        assertThat(url).startsWith("/uploads/").endsWith(".jpg");
        Path archivo = carpeta.resolve(url.substring("/uploads/".length()));
        assertThat(Files.exists(archivo)).isTrue();
        almacen.eliminar(url);
        assertThat(Files.exists(archivo)).isFalse();
    }

    @Test
    @DisplayName("Rechaza un archivo que dice ser imagen pero no lo es")
    void rechazaFalsaImagen() {
        LocalFileStorageAdapter almacen = new LocalFileStorageAdapter(carpeta.toString());
        byte[] texto = "<script>alert(1)</script>".getBytes();

        assertThatThrownBy(() -> almacen.guardarImagen("image/png", texto)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> almacen.guardarImagen("text/html", texto)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("No elimina archivos fuera de la carpeta de imágenes (path traversal)")
    void noSaleDeLaCarpeta() throws IOException {
        Path interna = Files.createDirectory(carpeta.resolve("uploads"));
        Path fuera = Files.writeString(carpeta.resolve("secreto.txt"), "no borrar");
        LocalFileStorageAdapter almacen = new LocalFileStorageAdapter(interna.toString());

        almacen.eliminar("/uploads/../secreto.txt");

        assertThat(Files.exists(fuera)).isTrue();
    }
}
