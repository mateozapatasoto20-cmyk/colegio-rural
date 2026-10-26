package com.colegiorural.dominio;

/** Estudiante con alertas activas y cuántas tiene. */
public record EnRiesgo(Estudiante estudiante, int alertas) {
}
