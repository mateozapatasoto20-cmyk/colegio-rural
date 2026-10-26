package com.colegiorural.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Valores de application.properties (prefijo "colegio"). */
@ConfigurationProperties(prefix = "colegio")
public record AppProperties(int porPagina, String periodo, List<String> grados, List<String> secciones,
                            double notaMinima, double asistenciaMinima, int faltasSeguidas) {
}
