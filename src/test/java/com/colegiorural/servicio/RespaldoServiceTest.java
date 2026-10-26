package com.colegiorural.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colegiorural.memoria.Escenario;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Seguridad: la restauración solo acepta archivos hechos por el propio sistema. */
class RespaldoServiceTest {
    private Escenario e;

    private static byte[] b(String s) { return s.getBytes(StandardCharsets.UTF_8); }

    @BeforeEach
    void preparar() { e = new Escenario(); }

    @Test
    void laCopiaPropiaSeExportaYSeRestaura() {
        var copia = e.respaldo.exportar();
        assertTrue(new String(copia.contenido(), StandardCharsets.UTF_8).startsWith("-- COLEGIO-RURAL-RESPALDO v1"));
        assertEquals(2, e.respaldo.restaurar(copia.contenido()));
    }

    @Test
    void rechazaUnArchivoAjeno() {
        assertThrows(ValidacionException.class, () -> e.respaldo.restaurar(b("hola")));
    }

    @Test
    void rechazaUnaSegundaSentenciaEscondida() {
        assertThrows(ValidacionException.class, () -> e.respaldo.restaurar(b("-- COLEGIO-RURAL-RESPALDO v1\nDELETE FROM logs; DROP TABLE usuarios;\n")));
    }

    @Test
    void rechazaTablasDesconocidas() {
        assertThrows(ValidacionException.class, () -> e.respaldo.restaurar(b("-- COLEGIO-RURAL-RESPALDO v1\nDELETE FROM mysql_user;\n")));
    }

    @Test
    void rechazaUnaCopiaSinAdministrador() {
        assertThrows(ValidacionException.class, () -> e.respaldo.restaurar(b("-- COLEGIO-RURAL-RESPALDO v1\nDELETE FROM logs;\n")));
    }

    @Test
    void loRechazadoNoEjecutaNada() {
        assertThrows(ValidacionException.class, () -> e.respaldo.restaurar(b("hola")));
        assertNull(e.respaldoDao.ejecutadas);
    }
}
