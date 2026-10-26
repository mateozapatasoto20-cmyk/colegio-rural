package com.colegiorural.memoria;

import com.colegiorural.config.AppProperties;
import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.regla.ReglaAlerta;
import com.colegiorural.regla.ReglaAsistenciaBaja;
import com.colegiorural.regla.ReglaFaltasSeguidas;
import com.colegiorural.regla.ReglaNotaBaja;
import com.colegiorural.servicio.AlertaService;
import com.colegiorural.servicio.AsistenciaService;
import com.colegiorural.servicio.CalificacionService;
import com.colegiorural.servicio.EstudianteService;
import com.colegiorural.servicio.LogService;
import com.colegiorural.servicio.ReglasService;
import com.colegiorural.servicio.ReglasVigentes;
import com.colegiorural.servicio.RespaldoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Arma los servicios a mano con DAO en memoria: sin Spring y sin base de datos.
 *  Esto es posible porque cada servicio recibe INTERFACES por el constructor (inversión de dependencias). */
public class Escenario {
    public final AppProperties app = new AppProperties(8, "2026-I", List.of("1ro Secundaria", "2do Secundaria", "6to Primaria"), List.of("A", "B", "C"), 11, 65, 3);
    public final MemoriaEstudianteDao estudiantesDao = new MemoriaEstudianteDao();
    public final MemoriaCursoDao cursosDao = new MemoriaCursoDao();
    public final MemoriaCalificacionDao calificacionesDao = new MemoriaCalificacionDao(estudiantesDao);
    public final MemoriaAlertaDao alertasDao = new MemoriaAlertaDao(estudiantesDao);
    public final MemoriaAsistenciaDao asistenciaDao = new MemoriaAsistenciaDao(cursosDao);
    public final MemoriaVarios.Config configDao = new MemoriaVarios.Config();
    public final MemoriaVarios.Logs logsDao = new MemoriaVarios.Logs();
    public final MemoriaVarios.Respaldo respaldoDao = new MemoriaVarios.Respaldo();

    public final List<ReglaAlerta> reglas = new ArrayList<>();
    public final ReglasVigentes vigentes = new ReglasVigentes(configDao, app);
    public final LogService log = new LogService(logsDao, () -> Optional.of(new SesionUsuario(1, "admin", "Admin", "admin", null)));
    public final AlertaService alertas;
    public final CalificacionService calificaciones;
    public final AsistenciaService asistencia;
    public final EstudianteService estudiantes;
    public final ReglasService reglasService;
    public final RespaldoService respaldo;

    public Escenario() {
        reglas.add(new ReglaNotaBaja(vigentes));
        reglas.add(new ReglaAsistenciaBaja(vigentes));
        reglas.add(new ReglaFaltasSeguidas(vigentes));
        alertas = new AlertaService(alertasDao, estudiantesDao, log, reglas);
        calificaciones = new CalificacionService(calificacionesDao, estudiantesDao, cursosDao, alertas, log, vigentes);
        asistencia = new AsistenciaService(asistenciaDao, cursosDao, estudiantesDao, alertas, log, vigentes);
        estudiantes = new EstudianteService(estudiantesDao, calificacionesDao, alertasDao, log, app);
        reglasService = new ReglasService(configDao, calificacionesDao, alertas, vigentes, log);
        respaldo = new RespaldoService(respaldoDao, log);
    }
}
