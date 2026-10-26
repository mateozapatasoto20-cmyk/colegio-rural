package com.colegiorural.dominio;

public record Calificacion(long id, long estudianteId, String curso, double nota, double asistencia) {
}
