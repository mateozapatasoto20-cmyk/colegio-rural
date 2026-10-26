package com.colegiorural.dominio;

/** Promedio de nota y de asistencia de uno o varios registros. */
public record Resumen(double promedio, double asistencia, int cantidad) {
}
