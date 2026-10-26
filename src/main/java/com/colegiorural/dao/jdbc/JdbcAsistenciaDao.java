package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.AsistenciaDao;
import com.colegiorural.dominio.EstadoAsistencia;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcAsistenciaDao extends BaseJdbc implements AsistenciaDao {

    public JdbcAsistenciaDao(JdbcTemplate jdbc) { super(jdbc); }

    private static Map<EstadoAsistencia, Integer> ceros() {
        Map<EstadoAsistencia, Integer> m = new EnumMap<>(EstadoAsistencia.class);
        for (EstadoAsistencia e : EstadoAsistencia.values()) { m.put(e, 0); }
        return m;
    }

    @Override
    public Map<Long, EstadoAsistencia> porCursoFecha(long cursoId, LocalDate fecha) {
        Map<Long, EstadoAsistencia> r = new HashMap<>();
        jdbc.query("SELECT estudiante_id, estado FROM asistencias WHERE curso_id = ? AND fecha = ?",
                rs -> { r.put(rs.getLong("estudiante_id"), EstadoAsistencia.desde(rs.getString("estado"))); }, cursoId, Date.valueOf(fecha));
        return r;
    }

    @Override
    @Transactional
    public void guardarLote(long cursoId, LocalDate fecha, Map<Long, EstadoAsistencia> estados) {
        for (Map.Entry<Long, EstadoAsistencia> e : estados.entrySet()) {
            ejecutar("DELETE FROM asistencias WHERE estudiante_id = ? AND curso_id = ? AND fecha = ?", e.getKey(), cursoId, Date.valueOf(fecha));
            ejecutar("INSERT INTO asistencias (estudiante_id, curso_id, fecha, estado) VALUES (?, ?, ?, ?)", e.getKey(), cursoId, Date.valueOf(fecha), e.getValue().valor());
        }
    }

    @Override
    public List<EstadoAsistencia> ultimosEstados(long estudianteId, long cursoId, int limite) {
        return filas("SELECT estado FROM asistencias WHERE estudiante_id = ? AND curso_id = ? ORDER BY fecha DESC LIMIT ?",
                (rs, n) -> EstadoAsistencia.desde(rs.getString("estado")), estudianteId, cursoId, limite);
    }

    @Override
    public Map<EstadoAsistencia, Integer> conteo(long estudianteId, Long cursoId) {
        Map<EstadoAsistencia, Integer> r = ceros();
        String sql = "SELECT estado, COUNT(*) AS n FROM asistencias WHERE estudiante_id = ?" + (cursoId != null ? " AND curso_id = ?" : "") + " GROUP BY estado";
        Object[] args = cursoId != null ? new Object[] {estudianteId, cursoId} : new Object[] {estudianteId};
        jdbc.query(sql, rs -> { r.put(EstadoAsistencia.desde(rs.getString("estado")), rs.getInt("n")); }, args);
        return r;
    }

    @Override
    public Map<Long, Map<EstadoAsistencia, Integer>> conteoPorCurso(long cursoId) {
        Map<Long, Map<EstadoAsistencia, Integer>> r = new HashMap<>();
        jdbc.query("SELECT estudiante_id, estado, COUNT(*) AS n FROM asistencias WHERE curso_id = ? GROUP BY estudiante_id, estado", rs -> {
            r.computeIfAbsent(rs.getLong("estudiante_id"), k -> ceros()).put(EstadoAsistencia.desde(rs.getString("estado")), rs.getInt("n"));
        }, cursoId);
        return r;
    }

    @Override
    public List<ConteoCurso> porEstudiantePorCurso(long estudianteId) {
        Map<Long, String> nombres = new LinkedHashMap<>();
        Map<Long, Map<EstadoAsistencia, Integer>> conteos = new HashMap<>();
        jdbc.query("SELECT a.curso_id, c.nombre, a.estado, COUNT(*) AS n FROM asistencias a JOIN cursos c ON c.id = a.curso_id "
                + "WHERE a.estudiante_id = ? GROUP BY a.curso_id, c.nombre, a.estado ORDER BY c.nombre", rs -> {
            long id = rs.getLong("curso_id");
            nombres.put(id, rs.getString("nombre"));
            conteos.computeIfAbsent(id, k -> ceros()).put(EstadoAsistencia.desde(rs.getString("estado")), rs.getInt("n"));
        }, estudianteId);
        List<ConteoCurso> r = new ArrayList<>();
        nombres.forEach((id, nombre) -> r.add(new ConteoCurso(id, nombre, conteos.get(id))));
        return r;
    }

    @Override
    public List<DiaAsistencia> tendencia(int dias) {
        List<DiaAsistencia> r = new ArrayList<>(filas("SELECT fecha, COUNT(*) AS total, SUM(CASE WHEN estado = 'falta' THEN 0 ELSE 1 END) AS asistio "
                + "FROM asistencias GROUP BY fecha ORDER BY fecha DESC LIMIT ?",
                (rs, n) -> new DiaAsistencia(rs.getDate("fecha").toLocalDate(), rs.getInt("total"), rs.getInt("asistio")), dias));
        java.util.Collections.reverse(r);
        return r;
    }

    @Override
    public Map<EstadoAsistencia, Integer> resumenFecha(LocalDate fecha) {
        Map<EstadoAsistencia, Integer> r = ceros();
        jdbc.query("SELECT estado, COUNT(*) AS n FROM asistencias WHERE fecha = ? GROUP BY estado",
                rs -> { r.put(EstadoAsistencia.desde(rs.getString("estado")), rs.getInt("n")); }, Date.valueOf(fecha));
        return r;
    }

    @Override
    public Optional<LocalDate> ultimaFecha() {
        Date d = jdbc.queryForObject("SELECT MAX(fecha) FROM asistencias", Date.class);
        return Optional.ofNullable(d).map(Date::toLocalDate);
    }

    @Override
    public List<LocalDate> fechasDeCurso(long cursoId, int limite) {
        return filas("SELECT DISTINCT fecha FROM asistencias WHERE curso_id = ? ORDER BY fecha DESC LIMIT ?", (rs, n) -> rs.getDate("fecha").toLocalDate(), cursoId, limite);
    }

    @Override
    public List<RegistroDia> delMes(long estudianteId, LocalDate desde, LocalDate hasta, Long cursoId) {
        String sql = "SELECT a.fecha, a.curso_id, c.nombre AS curso, a.estado FROM asistencias a JOIN cursos c ON c.id = a.curso_id "
                + "WHERE a.estudiante_id = ? AND a.fecha >= ? AND a.fecha <= ?" + (cursoId != null ? " AND a.curso_id = ?" : "") + " ORDER BY a.fecha, c.nombre";
        Object[] args = cursoId != null ? new Object[] {estudianteId, Date.valueOf(desde), Date.valueOf(hasta), cursoId}
                                        : new Object[] {estudianteId, Date.valueOf(desde), Date.valueOf(hasta)};
        return filas(sql, (rs, n) -> new RegistroDia(rs.getDate("fecha").toLocalDate(), rs.getLong("curso_id"), rs.getString("curso"),
                EstadoAsistencia.desde(rs.getString("estado"))), args);
    }
}
