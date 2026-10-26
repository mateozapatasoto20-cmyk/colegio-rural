package com.colegiorural.dao.jdbc;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** DRY: lo común de todos los DAO con JdbcTemplate (la librería de Spring que evita escribir el JDBC a mano). */
abstract class BaseJdbc {
    protected final JdbcTemplate jdbc;

    protected BaseJdbc(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    protected <T> List<T> filas(String sql, RowMapper<T> mapa, Object... args) { return jdbc.query(sql, mapa, args); }

    protected <T> Optional<T> una(String sql, RowMapper<T> mapa, Object... args) { return jdbc.query(sql, mapa, args).stream().findFirst(); }

    protected int contarFilas(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }

    protected void ejecutar(String sql, Object... args) { jdbc.update(sql, args); }

    /** INSERT que devuelve el id generado (sirve igual en MySQL y en PostgreSQL). */
    protected long insertar(String sql, Object... args) {
        KeyHolder llave = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});
            for (int i = 0; i < args.length; i++) { ps.setObject(i + 1, args[i]); }
            return ps;
        }, llave);
        Number k = llave.getKey();
        return k == null ? 0 : k.longValue();
    }

    protected static Object[] con(List<Object> args, Object... extra) {
        Object[] r = new Object[args.size() + extra.length];
        for (int i = 0; i < args.size(); i++) { r[i] = args.get(i); }
        for (int i = 0; i < extra.length; i++) { r[args.size() + i] = extra[i]; }
        return r;
    }

    protected static String like(String texto) { return "%" + texto.toLowerCase() + "%"; }
}
