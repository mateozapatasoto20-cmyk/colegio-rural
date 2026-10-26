package com.colegiorural.dao;

import com.colegiorural.dominio.EstadoAsistencia;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AsistenciaDao {
    /** Un día de un curso: estudiante -> estado. */
    Map<Long, EstadoAsistencia> porCursoFecha(long cursoId, LocalDate fecha);
    /** Reemplaza el registro del día para los estudiantes indicados (en una transacción). */
    void guardarLote(long cursoId, LocalDate fecha, Map<Long, EstadoAsistencia> estados);
    /** Últimos estados de un estudiante en un curso, del más reciente al más antiguo. */
    List<EstadoAsistencia> ultimosEstados(long estudianteId, long cursoId, int limite);
    /** Conteo por estado. cursoId null = todos los cursos. Siempre trae los 4 estados (con 0). */
    Map<EstadoAsistencia, Integer> conteo(long estudianteId, Long cursoId);
    Map<Long, Map<EstadoAsistencia, Integer>> conteoPorCurso(long cursoId);
    List<ConteoCurso> porEstudiantePorCurso(long estudianteId);
    /** Últimos días con registro: fecha, total de registros y cuántos asistieron. */
    List<DiaAsistencia> tendencia(int dias);
    Map<EstadoAsistencia, Integer> resumenFecha(LocalDate fecha);
    Optional<LocalDate> ultimaFecha();
    List<LocalDate> fechasDeCurso(long cursoId, int limite);
    List<RegistroDia> delMes(long estudianteId, LocalDate desde, LocalDate hasta, Long cursoId);

    record ConteoCurso(long cursoId, String nombre, Map<EstadoAsistencia, Integer> conteo) { }
    record DiaAsistencia(LocalDate fecha, int total, int asistio) { }
    record RegistroDia(LocalDate fecha, long cursoId, String curso, EstadoAsistencia estado) { }
}
