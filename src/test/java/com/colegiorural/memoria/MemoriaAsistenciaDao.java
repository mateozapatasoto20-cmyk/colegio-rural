package com.colegiorural.memoria;

import com.colegiorural.dao.AsistenciaDao;
import com.colegiorural.dominio.EstadoAsistencia;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class MemoriaAsistenciaDao implements AsistenciaDao {
    private record Reg(long estudianteId, long cursoId, LocalDate fecha, EstadoAsistencia estado) { }
    private final List<Reg> datos = new ArrayList<>();
    private final MemoriaCursoDao cursos;

    public MemoriaAsistenciaDao(MemoriaCursoDao cursos) { this.cursos = cursos; }

    private static Map<EstadoAsistencia, Integer> ceros() { Map<EstadoAsistencia, Integer> m = new EnumMap<>(EstadoAsistencia.class); for (var e : EstadoAsistencia.values()) { m.put(e, 0); } return m; }

    @Override public Map<Long, EstadoAsistencia> porCursoFecha(long c, LocalDate f) { Map<Long, EstadoAsistencia> m = new HashMap<>(); datos.stream().filter(r -> r.cursoId == c && r.fecha.equals(f)).forEach(r -> m.put(r.estudianteId, r.estado)); return m; }
    @Override public void guardarLote(long c, LocalDate f, Map<Long, EstadoAsistencia> e) { e.forEach((id, st) -> { datos.removeIf(r -> r.estudianteId == id && r.cursoId == c && r.fecha.equals(f)); datos.add(new Reg(id, c, f, st)); }); }
    @Override public List<EstadoAsistencia> ultimosEstados(long e, long c, int l) { return datos.stream().filter(r -> r.estudianteId == e && r.cursoId == c).sorted(Comparator.comparing((Reg r) -> r.fecha).reversed()).limit(l).map(r -> r.estado).toList(); }
    @Override public Map<EstadoAsistencia, Integer> conteo(long e, Long c) { Map<EstadoAsistencia, Integer> m = ceros(); datos.stream().filter(r -> r.estudianteId == e && (c == null || r.cursoId == c)).forEach(r -> m.merge(r.estado, 1, Integer::sum)); return m; }
    @Override public Map<Long, Map<EstadoAsistencia, Integer>> conteoPorCurso(long c) { Map<Long, Map<EstadoAsistencia, Integer>> m = new HashMap<>(); datos.stream().filter(r -> r.cursoId == c).forEach(r -> m.computeIfAbsent(r.estudianteId, k -> ceros()).merge(r.estado, 1, Integer::sum)); return m; }
    @Override public List<ConteoCurso> porEstudiantePorCurso(long e) { List<ConteoCurso> l = new ArrayList<>(); for (long c : datos.stream().filter(r -> r.estudianteId == e).map(r -> r.cursoId).distinct().toList()) { l.add(new ConteoCurso(c, cursos.datos.get(c).nombre(), conteo(e, c))); } return l; }
    @Override public List<DiaAsistencia> tendencia(int dias) { Map<LocalDate, int[]> m = new TreeMap<>(); datos.forEach(r -> { int[] a = m.computeIfAbsent(r.fecha, k -> new int[2]); a[0]++; if (r.estado != EstadoAsistencia.FALTA) { a[1]++; } }); List<DiaAsistencia> l = new ArrayList<>(); m.forEach((f, a) -> l.add(new DiaAsistencia(f, a[0], a[1]))); return l.subList(Math.max(0, l.size() - dias), l.size()); }
    @Override public Map<EstadoAsistencia, Integer> resumenFecha(LocalDate f) { Map<EstadoAsistencia, Integer> m = ceros(); datos.stream().filter(r -> r.fecha.equals(f)).forEach(r -> m.merge(r.estado, 1, Integer::sum)); return m; }
    @Override public Optional<LocalDate> ultimaFecha() { return datos.stream().map(r -> r.fecha).max(Comparator.naturalOrder()); }
    @Override public List<LocalDate> fechasDeCurso(long c, int l) { return datos.stream().filter(r -> r.cursoId == c).map(r -> r.fecha).distinct().sorted(Comparator.reverseOrder()).limit(l).toList(); }
    @Override public List<RegistroDia> delMes(long e, LocalDate d, LocalDate h, Long c) { return datos.stream().filter(r -> r.estudianteId == e && !r.fecha.isBefore(d) && !r.fecha.isAfter(h) && (c == null || r.cursoId == c)).map(r -> new RegistroDia(r.fecha, r.cursoId, cursos.datos.get(r.cursoId).nombre(), r.estado)).toList(); }
}
