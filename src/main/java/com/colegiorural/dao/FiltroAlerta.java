package com.colegiorural.dao;

public record FiltroAlerta(String estado, String tipo, String q) {
    public static FiltroAlerta sinFiltro() { return new FiltroAlerta("", "", ""); }
}
