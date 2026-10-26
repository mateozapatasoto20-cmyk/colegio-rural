package com.colegiorural.dominio;

/** Usuario más el nombre del estudiante vinculado (si es alumno). */
public record UsuarioDetalle(Usuario usuario, String estudianteNombre) {
}
