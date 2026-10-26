package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.UsuarioDao;
import com.colegiorural.dominio.Usuario;
import com.colegiorural.dominio.UsuarioDetalle;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUsuarioDao extends BaseJdbc implements UsuarioDao {

    private static Long estId(java.sql.ResultSet rs) throws java.sql.SQLException {
        long v = rs.getLong("estudiante_id");
        return rs.wasNull() ? null : v;
    }

    private static final RowMapper<Usuario> MAPA = (rs, n) -> new Usuario(rs.getLong("id"), rs.getString("username"), rs.getString("password"),
            rs.getString("rol"), rs.getString("nombre"), rs.getInt("activo") == 1, estId(rs));

    public JdbcUsuarioDao(JdbcTemplate jdbc) { super(jdbc); }

    @Override public Optional<Usuario> buscarPorId(long id) { return una("SELECT * FROM usuarios WHERE id = ?", MAPA, id); }
    @Override public Optional<Usuario> buscarPorUsername(String u) { return una("SELECT * FROM usuarios WHERE username = ?", MAPA, u); }

    @Override
    public List<UsuarioDetalle> listar() {
        return filas("SELECT u.*, e.nombres AS est_nombres, e.apellidos AS est_apellidos FROM usuarios u "
                + "LEFT JOIN estudiantes e ON e.id = u.estudiante_id ORDER BY u.rol, u.username", (rs, n) -> {
            String en = rs.getString("est_nombres");
            return new UsuarioDetalle(MAPA.mapRow(rs, n), en == null ? null : en + " " + rs.getString("est_apellidos"));
        });
    }

    @Override
    public long crear(Usuario u) {
        return insertar("INSERT INTO usuarios (username, password, rol, nombre, activo, estudiante_id) VALUES (?, ?, ?, ?, 1, ?)",
                u.username(), u.password(), u.rol(), u.nombre(), u.estudianteId());
    }

    @Override public void cambiarPassword(long id, String hash) { ejecutar("UPDATE usuarios SET password = ? WHERE id = ?", hash, id); }
    @Override public void cambiarEstado(long id, boolean activo) { ejecutar("UPDATE usuarios SET activo = ? WHERE id = ?", activo ? 1 : 0, id); }
}
