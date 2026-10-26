package com.colegiorural.servicio;

import com.colegiorural.dao.CalificacionDao;
import com.colegiorural.dao.ConfigDao;
import com.colegiorural.regla.Formato;
import com.colegiorural.regla.ReglaAlerta;
import org.springframework.stereotype.Service;

/** Reglas de negocio editables (mínimos aprobatorios) y recálculo de alertas con las reglas vigentes. */
@Service
public class ReglasService {
    private final ConfigDao dao;
    private final CalificacionDao califs;
    private final AlertaService alertas;
    private final ReglasVigentes vigentes;
    private final LogService log;

    public ReglasService(ConfigDao dao, CalificacionDao califs, AlertaService alertas, ReglasVigentes vigentes, LogService log) {
        this.dao = dao; this.califs = califs; this.alertas = alertas; this.vigentes = vigentes; this.log = log;
    }

    public void guardar(Double notaMinima, Double asistenciaMinima, Integer faltasSeguidas) {
        if (notaMinima == null || notaMinima < 1 || notaMinima > 20) { throw new ValidacionException("La nota mínima debe estar entre 1 y 20."); }
        if (asistenciaMinima == null || asistenciaMinima < 1 || asistenciaMinima > 100) { throw new ValidacionException("La asistencia mínima debe estar entre 1 y 100."); }
        if (faltasSeguidas == null || faltasSeguidas < 2 || faltasSeguidas > 10) { throw new ValidacionException("Las faltas seguidas deben ser un número entre 2 y 10."); }
        dao.guardar("nota_minima", Formato.numero(notaMinima));
        dao.guardar("asistencia_minima", Formato.numero(asistenciaMinima));
        dao.guardar("faltas_seguidas", String.valueOf(faltasSeguidas));
        vigentes.invalidar();
        log.registrar("Cambió las reglas: nota mínima " + Formato.numero(notaMinima) + ", asistencia mínima " + Formato.numero(asistenciaMinima) + "%, faltas seguidas " + faltasSeguidas);
    }

    /** Revisa todas las notas de estudiantes activos con las reglas actuales. Devuelve las alertas nuevas. */
    public int recalcular() {
        int n = 0;
        for (var c : califs.todasDeActivos()) { n += alertas.evaluar(c.estudianteId(), new ReglaAlerta.Evaluacion(c.curso(), c.nota(), c.asistencia(), null)); }
        log.registrar("Recalculó alertas (" + n + " nuevas)");
        return n;
    }
}
