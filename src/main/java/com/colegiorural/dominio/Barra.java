package com.colegiorural.dominio;

/** Una barra de un gráfico. clase: ok, crit, warn o neutro (define el color). */
public record Barra(String etiqueta, double valor, String clase) {
}
