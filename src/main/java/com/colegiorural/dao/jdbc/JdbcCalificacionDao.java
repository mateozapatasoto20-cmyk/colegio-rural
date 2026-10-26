package com.colegiorural.dao.jdbc;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCalificacionDao extends BaseJdbc implements CalificacionDao {

    private static final RowMapper<Calificacion> MAPA = (rs, n) -> new Calificacion(rs.getLong("id"), rs.getLong("estudiante_id"),
            rs.getString("curso"), rs.getDouble("nota"), rs.getDouble("asistencia"));

    public JdbcCalificacionDao(JdbcTemplate jdbc) { super(jdbc); }

    private String donde(FiltroCalificacion f, List<Object> args) {
        List<String> w = new ArrayList<>();
        if (!f.q().isEmpty()) {
            w.add("(LOWER(e.nombres) LIKE ? OR LOWER(e.apellidos) LIKE ? OR e.dni LIKE ?)");
            String l = like(f.q()); args.add(l); args.add(l); args.add("%" + f.q() + "%");
        }
        if (!f.curso().isEmpty()) { w.add("c.curso = ?"); args.add(f.curso()); }
        if (f.soloRiesgo()) { w.add("(c.nota < ? OR c.asistencia < ?)"); args.add(f.notaMinima()); args.add(f.asistenciaMinima()); }
        return w.isEmpty() ? "" : " WHERE " + String.join(" AND ", w);
    }

    @Override
    public long crear(Calificacion c) {
        return insertar("INSERT INTO calificaciones (estudiante_id, curso, nota, asistencia) VALUES (?, ?, ?, ?)", c.estudianteId(), c.curso(), c.nota(), c.asistencia());
    }

    @Override public void eliminar(long id) { ejecutar("DELETE FROM calificaciones WHERE id = ?", id); }

    @Override
    public List<CalificacionFila> listar(FiltroCalificacion f, int limite, int offset) {
        List<Object> args = new ArrayList<>();
        String where = donde(f, args);
        return filas("SELECT c.*, e.nombres, e.apellidos, e.grado, e.seccion, e.dni, e.activo FROM calificaciones c JOIN estudiantes e ON e.id = c.estudiante_id"
                + where + " ORDER BY c.id DESC LIMIT ? OFFSET ?",
                (rs, n) -> new CalificacionFila(MAPA.mapRow(rs, n), JdbcEstudianteDao.MAPA.mapRow(rs, n)),
                con(args, limite, offset));
    }

    @Override
    public int contar(FiltroCalificacion f) {
        List<Object> args = new ArrayList<>();
        return contarFilas("SELECT COUNT(*) FROM calificaciones c JOIN estudiantes e ON e.id = c.estudiante_id" + donde(f, args), args.toArray());
    }

    @Override public List<Calificacion> porEstudiante(long id) { return filas("SELECT * FROM calificaciones WHERE estudiante_id = ? ORDER BY curso", MAPA, id); }

    @Override
    public Map<Long, Resumen> resumenPorEstudiante() {
        Map<Long, Resumen> r = new HashMap<>();
        jdbc.query("SELECT estudiante_id, AVG(nota) AS promedio, AVG(asistencia) AS asistencia, COUNT(*) AS n FROM calificaciones GROUP BY estudiante_id",
                rs -> { r.put(rs.getLong("estudiante_id"), new Resumen(rs.getDouble("promedio"), rs.getDouble("asistencia"), rs.getInt("n"))); });
        return r;
    }

    @Override public List<Double> todasLasNotas() { return filas("SELECT nota FROM calificaciones", (rs, n) -> rs.getDouble("nota")); }

    @Override
    public Optional<Resumen> promedioGeneral() {
        return filas("SELECT AVG(nota) AS nota, AVG(asistencia) AS asistencia, COUNT(*) AS n FROM calificaciones",
                (rs, n) -> new Resumen(rs.getDouble("nota"), rs.getDouble("asistencia"), rs.getInt("n"))).stream().filter(r -> r.cantidad() > 0).findFirst();
    }

    @Override public List<String> cursosRegistrados() { return filas("SELECT DISTINCT curso FROM calificaciones ORDER BY curso", (rs, n) -> rs.getString("curso")); }

    @Override
    public List<Calificacion> todasDeActivos() {
        return filas("SELECT c.* FROM calificaciones c JOIN estudiantes e ON e.id = c.estudiante_id WHERE e.activo = 1", MAPA);
    }

    @Override
    public List<Promedio> mejoresPromedios(int limite) {
        return filas("SELECT e.id, e.nombres, e.apellidos, e.grado, e.seccion, e.dni, e.activo, AVG(c.nota) AS promedio "
                + "FROM calificaciones c JOIN estudiantes e ON e.id = c.estudiante_id WHERE e.activo = 1 "
                + "GROUP BY e.id, e.nombres, e.apellidos, e.grado, e.seccion, e.dni, e.activo ORDER BY promedio DESC, e.apellidos LIMIT ?",
                (rs, n) -> new Promedio(JdbcEstudianteDao.MAPA.mapRow(rs, n), rs.getDouble("promedio")), limite);
    }
}
