package com.colegiorural.dominio;

import java.time.LocalDateTime;

public record Alerta(long id, long estudianteId, String mensaje, String tipo, String estado, LocalDateTime creada) {
    public boolean activa() { return "activa".equals(estado); }
}
