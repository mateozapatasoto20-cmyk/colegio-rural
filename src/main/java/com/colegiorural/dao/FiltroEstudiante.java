package com.colegiorural.dao;

/** estado: "" (todos), "1" activos, "0" inactivos. orden: apellidos, apellidos_desc, nombres, grado, reciente. */
public record FiltroEstudiante(String q, String grado, String estado, String orden) {
    public static FiltroEstudiante todos() { return new FiltroEstudiante("", "", "", "apellidos"); }
}
