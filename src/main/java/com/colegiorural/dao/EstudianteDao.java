package com.colegiorural.dao;

import com.colegiorural.dominio.Estudiante;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/* ISP: una interfaz pequeña por entidad, solo con lo que los servicios necesitan.
   DIP: los servicios dependen de esta interfaz, nunca de JDBC. Si la escuela cambia de base de datos
   (o de tecnología), se escribe otra clase que cumpla esta interfaz y los servicios no se tocan. */
public interface EstudianteDao {
    Optional<Estudiante> buscarPorId(long id);
    Optional<Estudiante> buscarPorDni(String dni);
    List<Estudiante> listar(FiltroEstudiante filtro, int limite, int offset);
    int contar(FiltroEstudiante filtro);
    long crear(Estudiante datos);
    void actualizar(Estudiante datos);
    void cambiarEstado(long id, boolean activo);
    void eliminar(long id);
    Map<String, Integer> contarPorGrado();
    int contarActivos();
    List<Estudiante> activosDeGrupo(String grado, String seccion);
}
