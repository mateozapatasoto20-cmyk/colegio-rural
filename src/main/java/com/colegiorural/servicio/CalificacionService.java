package com.colegiorural.servicio;

import com.colegiorural.dao.CalificacionDao;
import com.colegiorural.dao.CursoDao;
import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dao.FiltroCalificacion;
import com.colegiorural.dominio.Calificacion;
import com.colegiorural.dominio.CalificacionFila;
import com.colegiorural.dominio.Estudiante;
import com.colegiorural.dominio.Pagina;
import com.colegiorural.regla.Formato;
import com.colegiorural.regla.ReglaAlerta;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/** Ingreso de notas: valida que los datos sean correctos, guarda y deja que las reglas generen las alertas. */
@Service
public class CalificacionService {
    private final CalificacionDao dao;
    private final EstudianteDao estudiantes;
    private final CursoDao cursos;
    private final AlertaService alertas;
    private final LogService log;
    private final ReglasVigentes reglas;

    public CalificacionService(CalificacionDao dao, EstudianteDao estudiantes, CursoDao cursos, AlertaService alertas, LogService log, ReglasVigentes reglas) {
        this.dao = dao; this.estudiantes = estudiantes; this.cursos = cursos; this.alertas = alertas; this.log = log; this.reglas = reglas;
    }

    /** Registra nota y asistencia. Devuelve cuántas alertas se crearon. Si algo no es correcto, lanza ValidacionException y no guarda nada. */
    public int registrar(long estudianteId, String curso, Double nota, Double asistencia) {
        String c = StringUtils.trimToEmpty(curso);
        Estudiante est = estudiantes.buscarPorId(estudianteId).filter(Estudiante::activo)
                .orElseThrow(() -> new ValidacionException("Elige un estudiante activo."));
        if (c.isEmpty() || c.length() > 60) { throw new ValidacionException("Indica el curso."); }
        if (nota == null || nota < 0 || nota > 20) { throw new ValidacionException("La nota debe estar entre 0 y 20."); }
        if (asistencia == null || asistencia < 0 || asistencia > 100) { throw new ValidacionException("La asistencia debe estar entre 0 y 100."); }

        dao.crear(new Calificacion(0, est.id(), c, nota, asistencia));
        int n = alertas.evaluar(est.id(), new ReglaAlerta.Evaluacion(c, nota, asistencia, null));
        log.registrar("Registró nota " + Formato.numero(nota) + " en " + c + " (estudiante #" + est.id() + ")");
        return n;
    }

    public void eliminar(long id) { dao.eliminar(id); log.registrar("Eliminó calificación #" + id); }

    public Pagina<CalificacionFila> listar(String q, String curso, boolean soloRiesgo, int pagina, int porPagina) {
        var r = reglas.actual();
        var f = new FiltroCalificacion(q, curso, soloRiesgo, r.notaMinima(), r.asistenciaMinima());
        return new Pagina<>(dao.listar(f, porPagina, (pagina - 1) * porPagina), dao.contar(f));
    }

    /** Cursos para elegir: los creados en "Cursos" más los que ya tienen notas, sin repetir. */
    public List<String> nombresDeCursos() {
        TreeSet<String> s = new TreeSet<>(cursos.nombresDistintos());
        s.addAll(dao.cursosRegistrados());
        return List.copyOf(s);
    }
}
