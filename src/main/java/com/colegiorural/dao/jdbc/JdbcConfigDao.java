package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.ConfigDao;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcConfigDao extends BaseJdbc implements ConfigDao {

    public JdbcConfigDao(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    public Map<String, String> todos() {
        Map<String, String> r = new HashMap<>();
        jdbc.query("SELECT clave, valor FROM configuracion", rs -> { r.put(rs.getString("clave"), rs.getString("valor")); });
        return r;
    }

    @Override
    public void guardar(String clave, String valor) {
        ejecutar("DELETE FROM configuracion WHERE clave = ?", clave);
        ejecutar("INSERT INTO configuracion (clave, valor) VALUES (?, ?)", clave, valor);
    }
}
