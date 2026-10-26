package com.colegiorural.dominio;

public record Estudiante(long id, String nombres, String apellidos, String grado, String seccion, String dni, boolean activo) {
    public String nombreCompleto() { return nombres + " " + apellidos; }
    public String apellidosNombres() { return apellidos + ", " + nombres; }
    public String iniciales() { return ("" + nombres.charAt(0) + apellidos.charAt(0)).toUpperCase(); }
    public String gradoSeccion() { return grado + " " + seccion; }
}
