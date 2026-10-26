package com.colegiorural.dao.jdbc;

import com.colegiorural.dao.CursoDao;
import com.colegiorural.dominio.Curso;
import com.colegiorural.dominio.CursoConDocente;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcCursoDao extends BaseJdbc implements CursoDao {

    private static final RowMapper<Curso> MAPA = (rs, n) ->
            new Curso(rs.getLong("id"), rs.getString("nombre"), rs.getString("grado"), rs.getString("seccion"), rs.getString("periodo"));

    public JdbcCursoDao(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    public List<CursoConDocente> listarConDocente() {
        return filas("SELECT c.id, c.nombre, c.grado, c.seccion, c.periodo, d.id AS docente_id, d.nombres AS doc_nombres, d.apellidos AS doc_apellidos "
                + "FROM cursos c LEFT JOIN docente_cursos dc ON dc.curso_id = c.id LEFT JOIN docentes d ON d.id = dc.docente_id "
                + "ORDER BY c.nombre, c.grado, c.seccion", (rs, n) -> {
            long did = rs.getLong("docente_id");
            boolean tiene = !rs.wasNull();
            return new CursoConDocente(MAPA.mapRow(rs, n), tiene ? did : null, tiene ? rs.getString("doc_nombres") + " " + rs.getString("doc_apellidos") : null);
        });
    }

    @Override public Optional<Curso> buscarPorId(long id) { return una("SELECT * FROM cursos WHERE id = ?", MAPA, id); }

    @Override
    public long crear(Curso c) {
        return insertar("INSERT INTO cursos (nombre, grado, seccion, periodo) VALUES (?, ?, ?, ?)", c.nombre(), c.grado(), c.seccion(), c.periodo());
    }

    @Override public void eliminar(long id) { ejecutar("DELETE FROM cursos WHERE id = ?", id); }

    /** Tabla puente docente_cursos: un docente responsable por curso. Todo o nada (transacción). */
    @Override
    @Transactional
    public void asignarDocente(long cursoId, long docenteId, String periodo) {
        ejecutar("DELETE FROM docente_cursos WHERE curso_id = ?", cursoId);
        if (docenteId > 0) { ejecutar("INSERT INTO docente_cursos (docente_id, curso_id, periodo) VALUES (?, ?, ?)", docenteId, cursoId, periodo); }
    }

    @Override public int contar() { return contarFilas("SELECT COUNT(*) FROM cursos"); }

    @Override
    public List<String> nombresDistintos() { return filas("SELECT DISTINCT nombre FROM cursos ORDER BY nombre", (rs, n) -> rs.getString("nombre")); }
}
