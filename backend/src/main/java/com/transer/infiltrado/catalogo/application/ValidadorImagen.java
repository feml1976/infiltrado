package com.transer.infiltrado.catalogo.application;

import com.transer.infiltrado.catalogo.domain.exception.ImagenInvalidaException;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ValidadorImagen {

    static final int MAX_BYTES = 200 * 1024; // 200 KB en binario decodificado

    /**
     * Valida y decodifica una cadena Base64 de imagen.
     * Acepta prefijo data URI (data:image/png;base64,...) — lo elimina antes de decodificar.
     * Lanza {@link ImagenInvalidaException} si el tamaño o el formato no son válidos.
     *
     * @return bytes decodificados (no se almacenan; la cadena original se guarda en BD)
     */
    public byte[] decodificarYValidar(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new ImagenInvalidaException("Se requiere imagen Base64 para tipo IMAGEN");
        }

        String datos = base64.contains(",")
                ? base64.substring(base64.indexOf(',') + 1).trim()
                : base64.trim();

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(datos);
        } catch (IllegalArgumentException e) {
            throw new ImagenInvalidaException("La cadena Base64 no es válida");
        }

        if (bytes.length > MAX_BYTES) {
            throw new ImagenInvalidaException(
                    "La imagen supera el límite de 200 KB (binario: " + bytes.length + " bytes)");
        }

        if (!esPng(bytes) && !esJpg(bytes) && !esWebp(bytes)) {
            throw new ImagenInvalidaException("Formato no admitido. Se aceptan PNG, JPG y WEBP");
        }

        return bytes;
    }

    private static boolean esPng(byte[] b) {
        return b.length >= 8
                && (b[0] & 0xFF) == 0x89 && (b[1] & 0xFF) == 0x50
                && (b[2] & 0xFF) == 0x4E && (b[3] & 0xFF) == 0x47
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A
                && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A;
    }

    private static boolean esJpg(byte[] b) {
        return b.length >= 3
                && (b[0] & 0xFF) == 0xFF
                && (b[1] & 0xFF) == 0xD8
                && (b[2] & 0xFF) == 0xFF;
    }

    private static boolean esWebp(byte[] b) {
        // Cabecera: RIFF....WEBP
        return b.length >= 12
                && (b[0] & 0xFF) == 0x52 && (b[1] & 0xFF) == 0x49
                && (b[2] & 0xFF) == 0x46 && (b[3] & 0xFF) == 0x46
                && (b[8] & 0xFF) == 0x57 && (b[9] & 0xFF) == 0x45
                && (b[10] & 0xFF) == 0x42 && (b[11] & 0xFF) == 0x50;
    }
}
