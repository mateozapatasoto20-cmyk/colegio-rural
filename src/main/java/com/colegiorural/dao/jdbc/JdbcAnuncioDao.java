package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.AnuncioDao;
import com.colegiorural.dominio.Anuncio;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAnuncioDao extends BaseJdbc implements AnuncioDao {

    public JdbcAnuncioDao(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    public List<Anuncio> listar(List<String> audiencias, int limite) {
        String marcas = String.join(",", java.util.Collections.nCopies(audiencias.size(), "?"));
        List<Object> args = new ArrayList<>(audiencias);
        return filas("SELECT * FROM anuncios WHERE audiencia IN (" + marcas + ") ORDER BY creado DESC, id DESC LIMIT ?",
                (rs, n) -> new Anuncio(rs.getLong("id"), rs.getString("titulo"), rs.getString("mensaje"), rs.getString("audiencia"),
                        rs.getString("autor"), rs.getTimestamp("creado").toLocalDateTime()), con(args, limite));
    }

    @Override
    public long crear(String titulo, String mensaje, String audiencia, String autor) {
        return insertar("INSERT INTO anuncios (titulo, mensaje, audiencia, autor, creado) VALUES (?, ?, ?, ?, ?)",
                titulo, mensaje, audiencia, autor, Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override public void eliminar(long id) { ejecutar("DELETE FROM anuncios WHERE id = ?", id); }
}
