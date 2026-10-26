package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.EstudianteDao;
import com.colegiorural.dao.FiltroEstudiante;
import com.colegiorural.dominio.Estudiante;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/* LSP: esta clase y cualquier otra que cumpla EstudianteDao (por ejemplo la de memoria de las pruebas) son intercambiables. */
@Repository
public class JdbcEstudianteDao extends BaseJdbc implements EstudianteDao {

    static final RowMapper<Estudiante> MAPA = (rs, n) -> new Estudiante(rs.getLong("id"), rs.getString("nombres"), rs.getString("apellidos"),
            rs.getString("grado"), rs.getString("seccion"), rs.getString("dni"), rs.getInt("activo") == 1);

    private static final Map<String, String> ORDENES = Map.of(
            "apellidos", "apellidos ASC, nombres ASC",
            "apellidos_desc", "apellidos DESC, nombres DESC",
            "nombres", "nombres ASC, apellidos ASC",
            "grado", "grado ASC, seccion ASC, apellidos ASC",
            "reciente", "id DESC");

    public JdbcEstudianteDao(JdbcTemplate jdbc) { super(jdbc); }

    private String donde(FiltroEstudiante f, List<Object> args) {
        List<String> w = new ArrayList<>();
        if (!f.q().isEmpty()) {
            w.add("(LOWER(nombres) LIKE ? OR LOWER(apellidos) LIKE ? OR dni LIKE ?)");
            String l = like(f.q()); args.add(l); args.add(l); args.add("%" + f.q() + "%");
        }
        if (!f.grado().isEmpty()) { w.add("grado = ?"); args.add(f.grado()); }
        if (!f.estado().isEmpty()) { w.add("activo = ?"); args.add(Integer.parseInt(f.estado())); }
        return w.isEmpty() ? "" : " WHERE " + String.join(" AND ", w);
    }

    @Override public Optional<Estudiante> buscarPorId(long id) { return una("SELECT * FROM estudiantes WHERE id = ?", MAPA, id); }
    @Override public Optional<Estudiante> buscarPorDni(String dni) { return una("SELECT * FROM estudiantes WHERE dni = ?", MAPA, dni); }

    @Override
    public List<Estudiante> listar(FiltroEstudiante f, int limite, int offset) {
        List<Object> args = new ArrayList<>();
        String where = donde(f, args);
        String orden = ORDENES.getOrDefault(f.orden(), ORDENES.get("apellidos"));
        return filas("SELECT * FROM estudiantes" + where + " ORDER BY " + orden + " LIMIT ? OFFSET ?", MAPA, con(args, limite, offset));
    }

    @Override
    public int contar(FiltroEstudiante f) {
        List<Object> args = new ArrayList<>();
        return contarFilas("SELECT COUNT(*) FROM estudiantes" + donde(f, args), args.toArray());
    }

    @Override
    public long crear(Estudiante e) {
        return insertar("INSERT INTO estudiantes (nombres, apellidos, grado, seccion, dni, activo) VALUES (?, ?, ?, ?, ?, 1)",
                e.nombres(), e.apellidos(), e.grado(), e.seccion(), e.dni());
    }

    @Override
    public void actualizar(Estudiante e) {
        ejecutar("UPDATE estudiantes SET nombres = ?, apellidos = ?, grado = ?, seccion = ?, dni = ? WHERE id = ?",
                e.nombres(), e.apellidos(), e.grado(), e.seccion(), e.dni(), e.id());
    }

    @Override public void cambiarEstado(long id, boolean activo) { ejecutar("UPDATE estudiantes SET activo = ? WHERE id = ?", activo ? 1 : 0, id); }
    @Override public void eliminar(long id) { ejecutar("DELETE FROM estudiantes WHERE id = ?", id); }

    @Override
    public Map<String, Integer> contarPorGrado() {
        Map<String, Integer> r = new LinkedHashMap<>();
        jdbc.query("SELECT grado, COUNT(*) AS n FROM estudiantes GROUP BY grado ORDER BY grado", rs -> { r.put(rs.getString("grado"), rs.getInt("n")); });
        return r;
    }

    @Override public int contarActivos() { return contarFilas("SELECT COUNT(*) FROM estudiantes WHERE activo = 1"); }

    @Override
    public List<Estudiante> activosDeGrupo(String grado, String seccion) {
        return filas("SELECT * FROM estudiantes WHERE activo = 1 AND grado = ? AND seccion = ? ORDER BY apellidos, nombres", MAPA, grado, seccion);
    }
}
