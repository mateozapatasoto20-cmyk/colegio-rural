package com.colegiorural.dao;

import com.colegiorural.dominio.Calificacion;
import com.colegiorural.dominio.CalificacionFila;
import com.colegiorural.dominio.Promedio;
import com.colegiorural.dominio.Resumen;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CalificacionDao {
    long crear(Calificacion datos);
    void eliminar(long id);
    List<CalificacionFila> listar(FiltroCalificacion filtro, int limite, int offset);
    int contar(FiltroCalificacion filtro);
    List<Calificacion> porEstudiante(long estudianteId);
    Map<Long, Resumen> resumenPorEstudiante();
    List<Double> todasLasNotas();
    Optional<Resumen> promedioGeneral();
    List<String> cursosRegistrados();
    List<Calificacion> todasDeActivos();
    List<Promedio> mejoresPromedios(int limite);
}
