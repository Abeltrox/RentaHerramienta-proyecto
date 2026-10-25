package com.rentaherramientas.infrastructure.storage;

import com.rentaherramientas.domain.exception.ReglaNegocioException;
import com.rentaherramientas.domain.port.out.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Guarda las imágenes en disco (carpeta configurada en app.storage.upload-dir) con nombre aleatorio. */
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private static final String PREFIJO_URL = "/uploads/";

    private final Path directorio;

    public LocalFileStorageAdapter(@Value("${app.storage.upload-dir}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardarImagen(String contentType, byte[] contenido) {
        String extension = extensionSegura(contentType, contenido);
        String nombre = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(directorio);
            Files.write(directorio.resolve(nombre), contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
        return PREFIJO_URL + nombre;
    }

    @Override
    public void eliminar(String url) {
        if (url == null || !url.startsWith(PREFIJO_URL)) {
            return;
        }
        // resolve + normalize + startsWith evita salirse de la carpeta (path traversal)
        Path archivo = directorio.resolve(url.substring(PREFIJO_URL.length())).normalize();
        if (!archivo.startsWith(directorio)) {
            return;
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignorado) {
            // un archivo huérfano no debe romper la operación de negocio
        }
    }

    /** Verifica que el contenido realmente sea una imagen (no basta con confiar en el Content-Type del cliente). */
    private String extensionSegura(String contentType, byte[] b) {
        boolean jpeg = b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        boolean png = b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
        boolean webp = b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        return switch (contentType) {
            case "image/jpeg" -> exigir(jpeg, "jpg");
            case "image/png" -> exigir(png, "png");
            case "image/webp" -> exigir(webp, "webp");
            default -> throw new ReglaNegocioException("Formato no permitido. Usa JPG, PNG o WEBP");
        };
    }

    private String exigir(boolean valido, String extension) {
        if (!valido) {
            throw new ReglaNegocioException("El archivo no es una imagen válida");
        }
        return extension;
    }
}
