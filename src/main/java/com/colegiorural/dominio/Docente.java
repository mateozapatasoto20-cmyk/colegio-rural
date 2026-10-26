package com.colegiorural.dominio;

public record Docente(long id, String nombres, String apellidos, String especialidad) {
    public String nombreCompleto() { return nombres + " " + apellidos; }
    public String apellidosNombres() { return apellidos + ", " + nombres; }
    public String iniciales() { return ("" + nombres.charAt(0) + apellidos.charAt(0)).toUpperCase(); }
}
