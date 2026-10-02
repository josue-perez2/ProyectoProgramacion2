package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Clave {

    private Clave() {
    }

    public static boolean coincide(String hashRecibido, String hashAlmacenado) {
        if (hashRecibido == null || hashAlmacenado == null || hashAlmacenado.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                hashRecibido.trim().getBytes(StandardCharsets.UTF_8),
                hashAlmacenado.trim().getBytes(StandardCharsets.UTF_8));
    }
}
