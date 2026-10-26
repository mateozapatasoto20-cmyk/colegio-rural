package com.colegiorural.dominio;

public record Usuario(long id, String username, String password, String rol, String nombre, boolean activo, Long estudianteId) {
}
