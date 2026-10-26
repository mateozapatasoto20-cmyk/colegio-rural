package com.colegiorural.dominio;

import java.time.LocalDateTime;

public record Anuncio(long id, String titulo, String mensaje, String audiencia, String autor, LocalDateTime creado) {
}
