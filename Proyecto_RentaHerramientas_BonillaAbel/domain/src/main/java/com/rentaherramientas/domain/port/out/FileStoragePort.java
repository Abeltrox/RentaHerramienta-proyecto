package com.rentaherramientas.domain.port.out;

/** Puerto de salida: almacenamiento de imágenes de herramientas. */
public interface FileStoragePort {

    /** Guarda la imagen y devuelve la URL pública relativa (por ejemplo /uploads/abc.jpg). */
    String guardarImagen(String contentType, byte[] contenido);

    void eliminar(String url);
}
