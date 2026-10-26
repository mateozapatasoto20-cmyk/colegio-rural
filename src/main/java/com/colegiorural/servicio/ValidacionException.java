package com.colegiorural.servicio;

/** Error de datos del usuario (nota fuera de rango, DNI repetido...). El mensaje se muestra tal cual en pantalla. */
public class ValidacionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) { super(mensaje); }
}
