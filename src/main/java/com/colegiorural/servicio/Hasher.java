package com.colegiorural.servicio;

/** Cifrado de contraseñas. La implementación real usa BCrypt (BcryptHasher). */
public interface Hasher {
    String cifrar(String clave);
    boolean coincide(String clave, String cifrada);
}
