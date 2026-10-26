package com.colegiorural.dominio;

import java.util.List;

public record Pagina<T>(List<T> items, int total) {
}
