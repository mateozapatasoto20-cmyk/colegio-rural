package com.colegiorural.servicio;

import com.colegiorural.dao.DocenteDao;
import com.colegiorural.dominio.Docente;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DocenteService {
    public record FilaDocente(Docente docente, int cursos) { }

    private final DocenteDao dao;
    private final LogService log;

    public DocenteService(DocenteDao dao, LogService log) { this.dao = dao; this.log = log; }

    public List<FilaDocente> listar(String q) {
        Map<Long, Integer> asignados = dao.cursosAsignados();
        return dao.listar(q).stream().map(d -> new FilaDocente(d, asignados.getOrDefault(d.id(), 0))).toList();
    }

    public void crear(String nombres, String apellidos, String especialidad) {
        String n = limpio(nombres), a = limpio(apellidos), e = limpio(especialidad);
        if (n.isEmpty() || a.isEmpty() || e.isEmpty()) { throw new ValidacionException("Completa nombres, apellidos y especialidad."); }
        dao.crear(new Docente(0, n, a, e));
        log.registrar("Registró docente " + a);
    }

    public void eliminar(long id) { dao.eliminar(id); log.registrar("Eliminó docente #" + id); }
    public List<Docente> todos() { return dao.listar(""); }

    private static String limpio(String s) { return s == null ? "" : s.trim(); }
}
