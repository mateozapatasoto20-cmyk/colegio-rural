package com.colegiorural.memoria;

import com.colegiorural.dao.CalificacionDao;
import com.colegiorural.dao.FiltroCalificacion;
import com.colegiorural.dominio.Calificacion;
import com.colegiorural.dominio.CalificacionFila;
import com.colegiorural.dominio.Promedio;
import com.colegiorural.dominio.Resumen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

public class MemoriaCalificacionDao implements CalificacionDao {
    public final List<Calificacion> datos = new ArrayList<>();
    private final MemoriaEstudianteDao estudiantes;
    private long sig = 1;

    public MemoriaCalificacionDao(MemoriaEstudianteDao estudiantes) { this.estudiantes = estudiantes; }

    @Override public long crear(Calificacion c) { long id = sig++; datos.add(new Calificacion(id, c.estudianteId(), c.curso(), c.nota(), c.asistencia())); return id; }
    @Override public void eliminar(long id) { datos.removeIf(c -> c.id() == id); }

    private boolean pasa(Calificacion c, FiltroCalificacion f) {
        return (f.curso().isEmpty() || f.curso().equals(c.curso())) && (!f.soloRiesgo() || c.nota() < f.notaMinima() || c.asistencia() < f.asistenciaMinima());
    }

    @Override
    public List<CalificacionFila> listar(FiltroCalificacion f, int limite, int offset) {
        return datos.stream().filter(c -> pasa(c, f)).skip(offset).limit(limite)
                .map(c -> new CalificacionFila(c, estudiantes.datos.get(c.estudianteId()))).toList();
    }

    @Override public int contar(FiltroCalificacion f) { return (int) datos.stream().filter(c -> pasa(c, f)).count(); }
    @Override public List<Calificacion> porEstudiante(long id) { return datos.stream().filter(c -> c.estudianteId() == id).toList(); }

    @Override
    public Map<Long, Resumen> resumenPorEstudiante() {
        Map<Long, Resumen> r = new HashMap<>();
        for (long id : datos.stream().map(Calificacion::estudianteId).distinct().toList()) {
            var l = porEstudiante(id);
            r.put(id, new Resumen(l.stream().mapToDouble(Calificacion::nota).average().orElse(0), l.stream().mapToDouble(Calificacion::asistencia).average().orElse(0), l.size()));
        }
        return r;
    }

    @Override public List<Double> todasLasNotas() { return datos.stream().map(Calificacion::nota).toList(); }
    @Override public Optional<Resumen> promedioGeneral() { return datos.isEmpty() ? Optional.empty() : Optional.of(new Resumen(datos.stream().mapToDouble(Calificacion::nota).average().orElse(0), datos.stream().mapToDouble(Calificacion::asistencia).average().orElse(0), datos.size())); }
    @Override public List<String> cursosRegistrados() { return new ArrayList<>(new TreeSet<>(datos.stream().map(Calificacion::curso).toList())); }
    @Override public List<Calificacion> todasDeActivos() { return datos.stream().filter(c -> estudiantes.datos.get(c.estudianteId()).activo()).toList(); }
    @Override public List<Promedio> mejoresPromedios(int limite) { return List.of(); }
}
