package com.colegiorural.dominio;

import java.time.LocalDateTime;

/** Una línea de la bitácora (quién hizo qué y cuándo). */
public record Registro(long id, String usuario, String accion, LocalDateTime creado) {
}
