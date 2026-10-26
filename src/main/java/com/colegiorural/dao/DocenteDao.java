package com.colegiorural.dao;

import com.colegiorural.dominio.Docente;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DocenteDao {
    List<Docente> listar(String busqueda);
    Optional<Docente> buscarPorId(long id);
    long crear(Docente datos);
    void eliminar(long id);
    int contar();
    Map<Long, Integer> cursosAsignados();
}
