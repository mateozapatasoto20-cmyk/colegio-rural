package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.DocenteDao;
import com.colegiorural.dominio.Docente;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDocenteDao extends BaseJdbc implements DocenteDao {

    private static final RowMapper<Docente> MAPA = (rs, n) ->
            new Docente(rs.getLong("id"), rs.getString("nombres"), rs.getString("apellidos"), rs.getString("especialidad"));

    public JdbcDocenteDao(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    public List<Docente> listar(String busqueda) {
        if (busqueda == null || busqueda.isEmpty()) { return filas("SELECT * FROM docentes ORDER BY apellidos, nombres", MAPA); }
        String l = like(busqueda);
        return filas("SELECT * FROM docentes WHERE LOWER(nombres) LIKE ? OR LOWER(apellidos) LIKE ? OR LOWER(especialidad) LIKE ? ORDER BY apellidos, nombres", MAPA, l, l, l);
    }

    @Override public Optional<Docente> buscarPorId(long id) { return una("SELECT * FROM docentes WHERE id = ?", MAPA, id); }

    @Override
    public long crear(Docente d) {
        return insertar("INSERT INTO docentes (nombres, apellidos, especialidad) VALUES (?, ?, ?)", d.nombres(), d.apellidos(), d.especialidad());
    }

    @Override public void eliminar(long id) { ejecutar("DELETE FROM docentes WHERE id = ?", id); }
    @Override public int contar() { return contarFilas("SELECT COUNT(*) FROM docentes"); }

    @Override
    public Map<Long, Integer> cursosAsignados() {
        Map<Long, Integer> r = new HashMap<>();
        jdbc.query("SELECT docente_id, COUNT(*) AS n FROM docente_cursos GROUP BY docente_id", rs -> { r.put(rs.getLong("docente_id"), rs.getInt("n")); });
        return r;
    }
}
