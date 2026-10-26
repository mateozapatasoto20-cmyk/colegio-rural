package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.AlertaDao;
import com.colegiorural.dao.FiltroAlerta;
import com.colegiorural.dominio.Alerta;
import com.colegiorural.dominio.AlertaFila;
import com.colegiorural.dominio.EnRiesgo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAlertaDao extends BaseJdbc implements AlertaDao {

    private static final RowMapper<Alerta> MAPA = (rs, n) -> new Alerta(rs.getLong("id"), rs.getLong("estudiante_id"), rs.getString("mensaje"),
            rs.getString("tipo"), rs.getString("estado"), rs.getTimestamp("creada").toLocalDateTime());

    public JdbcAlertaDao(JdbcTemplate jdbc) { super(jdbc); }

    private String donde(FiltroAlerta f, List<Object> args) {
        List<String> w = new ArrayList<>();
        if (!f.estado().isEmpty()) { w.add("a.estado = ?"); args.add(f.estado()); }
        if (!f.tipo().isEmpty()) { w.add("a.tipo = ?"); args.add(f.tipo()); }
        if (!f.q().isEmpty()) { w.add("(LOWER(e.nombres) LIKE ? OR LOWER(e.apellidos) LIKE ?)"); String l = like(f.q()); args.add(l); args.add(l); }
        return w.isEmpty() ? "" : " WHERE " + String.join(" AND ", w);
    }

    @Override
    public List<AlertaFila> listar(FiltroAlerta f, int limite, int offset) {
        List<Object> args = new ArrayList<>();
        String where = donde(f, args);
        return filas("SELECT a.*, e.nombres, e.apellidos, e.grado, e.seccion, e.dni, e.activo FROM alertas a JOIN estudiantes e ON e.id = a.estudiante_id"
                + where + " ORDER BY a.id DESC LIMIT ? OFFSET ?",
                (rs, n) -> new AlertaFila(MAPA.mapRow(rs, n), JdbcEstudianteDao.MAPA.mapRow(rs, n)), con(args, limite, offset));
    }

    @Override
    public int contar(FiltroAlerta f) {
        List<Object> args = new ArrayList<>();
        return contarFilas("SELECT COUNT(*) FROM alertas a JOIN estudiantes e ON e.id = a.estudiante_id" + donde(f, args), args.toArray());
    }

    @Override
    public long crear(long estudianteId, String mensaje, String tipo) {
        return insertar("INSERT INTO alertas (estudiante_id, mensaje, tipo, estado, creada) VALUES (?, ?, ?, 'activa', CURRENT_TIMESTAMP)", estudianteId, mensaje, tipo);
    }

    @Override
    public boolean existeActiva(long estudianteId, String tipo, String mensaje) {
        return contarFilas("SELECT COUNT(*) FROM alertas WHERE estudiante_id = ? AND tipo = ? AND mensaje = ? AND estado = 'activa'", estudianteId, tipo, mensaje) > 0;
    }

    @Override public void resolver(long id) { ejecutar("UPDATE alertas SET estado = 'resuelta' WHERE id = ?", id); }
    @Override public Optional<Alerta> buscarPorId(long id) { return una("SELECT * FROM alertas WHERE id = ?", MAPA, id); }
    @Override public List<Alerta> porEstudiante(long id) { return filas("SELECT * FROM alertas WHERE estudiante_id = ? ORDER BY id DESC", MAPA, id); }

    @Override
    public Map<String, Integer> contarPorTipo(String estado) {
        Map<String, Integer> r = new LinkedHashMap<>();
        jdbc.query("SELECT tipo, COUNT(*) AS n FROM alertas WHERE estado = ? GROUP BY tipo", rs -> { r.put(rs.getString("tipo"), rs.getInt("n")); }, estado);
        return r;
    }

    @Override
    public Map<Long, Integer> conteoActivasPorEstudiante() {
        Map<Long, Integer> r = new HashMap<>();
        jdbc.query("SELECT estudiante_id, COUNT(*) AS n FROM alertas WHERE estado = 'activa' GROUP BY estudiante_id", rs -> { r.put(rs.getLong("estudiante_id"), rs.getInt("n")); });
        return r;
    }

    @Override
    public List<EnRiesgo> estudiantesEnRiesgo(int limite) {
        return filas("SELECT e.id, e.nombres, e.apellidos, e.grado, e.seccion, e.dni, e.activo, COUNT(a.id) AS n FROM alertas a JOIN estudiantes e ON e.id = a.estudiante_id "
                + "WHERE a.estado = 'activa' GROUP BY e.id, e.nombres, e.apellidos, e.grado, e.seccion, e.dni, e.activo ORDER BY n DESC, e.apellidos LIMIT ?",
                (rs, n) -> new EnRiesgo(JdbcEstudianteDao.MAPA.mapRow(rs, n), rs.getInt("n")), limite);
    }
}
