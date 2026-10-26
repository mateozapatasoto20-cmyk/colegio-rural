package com.colegiorural.servicio;

import com.colegiorural.config.AppProperties;
import com.colegiorural.dao.CursoDao;
import com.colegiorural.dao.DocenteDao;
import com.colegiorural.dominio.Curso;
import com.colegiorural.dominio.CursoConDocente;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CursoService {
    private final CursoDao dao;
    private final DocenteDao docentes;
    private final LogService log;
    private final AppProperties app;

    public CursoService(CursoDao dao, DocenteDao docentes, LogService log, AppProperties app) {
        this.dao = dao; this.docentes = docentes; this.log = log; this.app = app;
    }

    public List<CursoConDocente> listar() { return dao.listarConDocente(); }

    public void crear(String nombre, String grado, String seccion, String periodo) {
        String n = nombre == null ? "" : nombre.trim(), p = periodo == null ? "" : periodo.trim();
        if (n.isEmpty() || p.isEmpty()) { throw new ValidacionException("Completa el nombre del curso y el periodo."); }
        if (!app.grados().contains(grado) || !app.secciones().contains(seccion)) { throw new ValidacionException("Elige un grado y una sección válidos."); }
        dao.crear(new Curso(0, n, grado, seccion, p));
        log.registrar("Creó curso " + n);
    }

    public void asignar(long cursoId, long docenteId) {
        Curso c = dao.buscarPorId(cursoId).orElseThrow(() -> new ValidacionException("El curso no existe."));
        if (docenteId > 0 && docentes.buscarPorId(docenteId).isEmpty()) { throw new ValidacionException("El docente no existe."); }
        dao.asignarDocente(cursoId, docenteId, c.periodo());
        log.registrar("Asignó docente al curso #" + cursoId);
    }

    public void eliminar(long id) { dao.eliminar(id); log.registrar("Eliminó curso #" + id); }
}
