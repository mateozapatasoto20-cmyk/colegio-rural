package com.colegiorural.servicio;

import com.colegiorural.config.AppProperties;
import com.colegiorural.dao.ConfigDao;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Reglas de negocio vigentes: lo guardado en la tabla configuracion tiene prioridad sobre application.properties. */
@Service
public class ReglasVigentes {

    public record Reglas(double notaMinima, double asistenciaMinima, int faltasSeguidas) { }

    private final ConfigDao dao;
    private final AppProperties base;
    private volatile Reglas cache;

    public ReglasVigentes(ConfigDao dao, AppProperties base) { this.dao = dao; this.base = base; }

    public Reglas actual() {
        Reglas r = cache;
        if (r == null) {
            Map<String, String> bd;
            try { bd = dao.todos(); } catch (RuntimeException e) { bd = Map.of(); }   // sin tabla: valores del archivo
            r = new Reglas(numero(bd.get("nota_minima"), base.notaMinima()), numero(bd.get("asistencia_minima"), base.asistenciaMinima()),
                    (int) numero(bd.get("faltas_seguidas"), base.faltasSeguidas()));
            cache = r;
        }
        return r;
    }

    public void invalidar() { cache = null; }

    // Para usar en las pantallas: ${@reglasVigentes.notaMinima()}
    public double notaMinima() { return actual().notaMinima(); }
    public double asistenciaMinima() { return actual().asistenciaMinima(); }
    public int faltasSeguidas() { return actual().faltasSeguidas(); }

    private static double numero(String valor, double porDefecto) {
        try { return valor == null ? porDefecto : Double.parseDouble(valor); } catch (NumberFormatException e) { return porDefecto; }
    }
}
