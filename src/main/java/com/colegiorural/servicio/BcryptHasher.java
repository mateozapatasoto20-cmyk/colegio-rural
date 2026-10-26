package com.colegiorural.servicio;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Librería: BCryptPasswordEncoder. Lee también los hashes $2y$ que ya existen en la base de datos del sistema anterior. */
@Component
public class BcryptHasher implements Hasher {
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    @Override public String cifrar(String clave) { return codificador.encode(clave); }
    @Override public boolean coincide(String clave, String cifrada) { return cifrada != null && codificador.matches(clave, cifrada); }
}
