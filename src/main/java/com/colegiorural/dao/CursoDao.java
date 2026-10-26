package com.colegiorural.dao;

import com.colegiorural.dominio.Curso;
import com.colegiorural.dominio.CursoConDocente;
import java.util.List;
import java.util.Optional;

public interface CursoDao {
    List<CursoConDocente> listarConDocente();
    Optional<Curso> buscarPorId(long id);
    long crear(Curso datos);
    void eliminar(long id);
    /** Tabla puente docente_cursos. docenteId 0 = quitar al docente. */
    void asignarDocente(long cursoId, long docenteId, String periodo);
    int contar();
    List<String> nombresDistintos();
}
