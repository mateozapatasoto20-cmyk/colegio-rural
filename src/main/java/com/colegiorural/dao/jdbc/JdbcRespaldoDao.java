package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.RespaldoDao;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Copia de seguridad: una sentencia SQL por línea (DELETE e INSERT de las tablas del sistema). */
@Repository
public class JdbcRespaldoDao extends BaseJdbc implements RespaldoDao {

    /** Orden de inserción: primero las tablas "padre". Para borrar se usa el orden inverso. */
    private static final List<String> TABLAS = List.of("estudiantes", "docentes", "cursos", "docente_cursos", "usuarios",
            "calificaciones", "alertas", "logs", "asistencias", "anuncios", "configuracion");

    public JdbcRespaldoDao(JdbcTemplate jdbc) { super(jdbc); }

    private String motor() {
        return jdbc.execute((ConnectionCallback<String>) con -> con.getMetaData().getDatabaseProductName().toLowerCase());
    }

    @Override public List<String> tablas() { return TABLAS; }

    @Override public boolean escapaConBarra() { String m = motor(); return m.contains("mysql") || m.contains("mariadb"); }

    @Override
    public Map<String, Integer> conteos() {
        Map<String, Integer> r = new LinkedHashMap<>();
        for (String t : TABLAS) { r.put(t, contarFilas("SELECT COUNT(*) FROM " + t)); }
        return r;
    }

    @Override
    public List<String> volcar() {
        boolean barra = escapaConBarra();
        List<String> lineas = new ArrayList<>();
        List<String> inversa = new ArrayList<>(TABLAS);
        Collections.reverse(inversa);
        for (String t : inversa) { lineas.add("DELETE FROM " + t + ";"); }
        for (String t : TABLAS) {
            jdbc.query("SELECT * FROM " + t, (ResultSet rs) -> {
                ResultSetMetaData md = rs.getMetaData();
                StringBuilder cols = new StringBuilder();
                for (int i = 1; i <= md.getColumnCount(); i++) { cols.append(i > 1 ? "," : "").append(md.getColumnLabel(i).toLowerCase()); }
                StringBuilder vals = new StringBuilder();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String s;
                    if (v == null) { s = "NULL"; }
                    else if (v instanceof Number) { s = v.toString(); }
                    else { s = "'" + escapar(rs.getString(i), barra) + "'"; }
                    vals.append(i > 1 ? "," : "").append(s);
                }
                lineas.add("INSERT INTO " + t + " (" + cols + ") VALUES (" + vals + ");");
            });
        }
        return lineas;
    }

    /** Los saltos de línea se cambian por espacio para que cada sentencia quede en una sola línea. */
    private static String escapar(String texto, boolean barra) {
        String t = texto.replaceAll("\\R", " ");
        if (barra) { t = t.replace("\\", "\\\\"); }
        return t.replace("'", "''");
    }

    @Override
    public void restaurar(List<String> sentencias) {
        boolean postgres = motor().contains("postgres");
        jdbc.execute((ConnectionCallback<Void>) con -> {
            boolean auto = con.getAutoCommit();
            con.setAutoCommit(false);
            try (Statement st = con.createStatement()) {
                for (String s : sentencias) { st.executeUpdate(s); }
                if (postgres) { ajustarSecuencias(con, st); }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(auto);
            }
            return null;
        });
    }

    /** PostgreSQL: después de insertar ids explícitos hay que adelantar las secuencias. */
    private void ajustarSecuencias(Connection con, Statement st) throws SQLException {
        for (String t : TABLAS) {
            if (t.equals("configuracion")) { continue; }
            st.execute("SELECT setval(pg_get_serial_sequence('" + t + "','id'), COALESCE((SELECT MAX(id) FROM " + t + "), 0) + 1, false)");
        }
    }
}
