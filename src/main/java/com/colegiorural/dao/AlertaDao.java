package com.colegiorural.dao;

import com.colegiorural.dominio.Alerta;
import com.colegiorural.dominio.AlertaFila;
import com.colegiorural.dominio.EnRiesgo;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AlertaDao {
    List<AlertaFila> listar(FiltroAlerta filtro, int limite, int offset);
    int contar(FiltroAlerta filtro);
    long crear(long estudianteId, String mensaje, String tipo);
    boolean existeActiva(long estudianteId, String tipo, String mensaje);
    void resolver(long id);
    Optional<Alerta> buscarPorId(long id);
    List<Alerta> porEstudiante(long estudianteId);
    Map<String, Integer> contarPorTipo(String estado);
    Map<Long, Integer> conteoActivasPorEstudiante();
    List<EnRiesgo> estudiantesEnRiesgo(int limite);
}
