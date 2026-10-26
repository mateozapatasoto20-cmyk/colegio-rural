package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.LogDao;
import com.colegiorural.dominio.Registro;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLogDao extends BaseJdbc implements LogDao {

    public JdbcLogDao(JdbcTemplate jdbc) { super(jdbc); }

    @Override public void registrar(String usuario, String accion) { ejecutar("INSERT INTO logs (usuario, accion) VALUES (?, ?)", usuario, accion); }

    @Override
    public List<Registro> listar(int limite, int offset) {
        return filas("SELECT * FROM logs ORDER BY id DESC LIMIT ? OFFSET ?",
                (rs, n) -> new Registro(rs.getLong("id"), rs.getString("usuario"), rs.getString("accion"), rs.getTimestamp("creado").toLocalDateTime()), limite, offset);
    }

    @Override public int contar() { return contarFilas("SELECT COUNT(*) FROM logs"); }
}
