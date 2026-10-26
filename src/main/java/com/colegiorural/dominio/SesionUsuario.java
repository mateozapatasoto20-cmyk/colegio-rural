package com.colegiorural.dominio;

/** Datos del usuario que inició sesión (se guardan en la sesión HTTP). */
public record SesionUsuario(long id, String username, String nombre, String rol, Long estudianteId) implements java.io.Serializable {
    public String inicial() { return nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase(); }
}
