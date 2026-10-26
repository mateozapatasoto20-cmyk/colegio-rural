package com.colegiorural.memoria;

import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dao.FiltroEstudiante;
import com.colegiorural.dominio.Estudiante;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** DAO falso en memoria: cumple la misma interfaz que el de JDBC. Prueba el principio de sustitución (L) y permite probar sin base de datos. */
public class MemoriaEstudianteDao implements EstudianteDao {
    public final Map<Long, Estudiante> datos = new LinkedHashMap<>();
    private long sig = 1;

    public MemoriaEstudianteDao() {
        crear(new Estudiante(0, "Ana", "Torres Vega", "1ro Secundaria", "A", "71234501", true));
        crear(new Estudiante(0, "Luis", "Huamán Rojas", "1ro Secundaria", "A", "71234502", true));
        crear(new Estudiante(0, "María", "Salas Cruz", "2do Secundaria", "B", "71234503", true));
        crear(new Estudiante(0, "Rosa", "Díaz Mendoza", "6to Primaria", "B", "71234505", true));
        cambiarEstado(4, false);
    }

    private boolean pasa(Estudiante e, FiltroEstudiante f) {
        String q = f.q().toLowerCase();
        return (q.isEmpty() || e.nombres().toLowerCase().contains(q) || e.apellidos().toLowerCase().contains(q) || e.dni().contains(q))
                && (f.grado().isEmpty() || f.grado().equals(e.grado()))
                && (f.estado().isEmpty() || (e.activo() ? "1" : "0").equals(f.estado()));
    }

    @Override public Optional<Estudiante> buscarPorId(long id) { return Optional.ofNullable(datos.get(id)); }
    @Override public Optional<Estudiante> buscarPorDni(String dni) { return datos.values().stream().filter(e -> e.dni().equals(dni)).findFirst(); }

    @Override
    public List<Estudiante> listar(FiltroEstudiante f, int limite, int offset) {
        return datos.values().stream().filter(e -> pasa(e, f)).sorted(Comparator.comparing(Estudiante::apellidos)).skip(offset).limit(limite).toList();
    }

    @Override public int contar(FiltroEstudiante f) { return (int) datos.values().stream().filter(e -> pasa(e, f)).count(); }

    @Override public long crear(Estudiante e) { long id = sig++; datos.put(id, new Estudiante(id, e.nombres(), e.apellidos(), e.grado(), e.seccion(), e.dni(), true)); return id; }
    @Override public void actualizar(Estudiante e) { Estudiante v = datos.get(e.id()); datos.put(e.id(), new Estudiante(e.id(), e.nombres(), e.apellidos(), e.grado(), e.seccion(), e.dni(), v.activo())); }
    @Override public void cambiarEstado(long id, boolean activo) { Estudiante e = datos.get(id); datos.put(id, new Estudiante(id, e.nombres(), e.apellidos(), e.grado(), e.seccion(), e.dni(), activo)); }
    @Override public void eliminar(long id) { datos.remove(id); }
    @Override public Map<String, Integer> contarPorGrado() { Map<String, Integer> m = new TreeMap<>(); datos.values().forEach(e -> m.merge(e.grado(), 1, Integer::sum)); return m; }
    @Override public int contarActivos() { return (int) datos.values().stream().filter(Estudiante::activo).count(); }
    @Override public List<Estudiante> activosDeGrupo(String g, String s) { return new ArrayList<>(datos.values().stream().filter(e -> e.activo() && e.grado().equals(g) && e.seccion().equals(s)).toList()); }
}
