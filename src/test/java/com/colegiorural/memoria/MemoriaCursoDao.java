package com.colegiorural.memoria;

import com.colegiorural.dao.CursoDao;
import com.colegiorural.dominio.Curso;
import com.colegiorural.dominio.CursoConDocente;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemoriaCursoDao implements CursoDao {
    public final Map<Long, Curso> datos = new LinkedHashMap<>();
    private long sig = 1;

    public MemoriaCursoDao() {
        crear(new Curso(0, "Matemática", "1ro Secundaria", "A", "2026-I"));
        crear(new Curso(0, "Comunicación", "1ro Secundaria", "A", "2026-I"));
    }

    @Override public List<CursoConDocente> listarConDocente() { return datos.values().stream().map(c -> new CursoConDocente(c, null, null)).toList(); }
    @Override public Optional<Curso> buscarPorId(long id) { return Optional.ofNullable(datos.get(id)); }
    @Override public long crear(Curso c) { long id = sig++; datos.put(id, new Curso(id, c.nombre(), c.grado(), c.seccion(), c.periodo())); return id; }
    @Override public void eliminar(long id) { datos.remove(id); }
    @Override public void asignarDocente(long c, long d, String p) { }
    @Override public int contar() { return datos.size(); }
    @Override public List<String> nombresDistintos() { return datos.values().stream().map(Curso::nombre).distinct().sorted().toList(); }
}
