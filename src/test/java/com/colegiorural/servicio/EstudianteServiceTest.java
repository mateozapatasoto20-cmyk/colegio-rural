package com.colegiorural.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colegiorural.memoria.Escenario;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstudianteServiceTest {
    private Escenario e;

    @BeforeEach
    void preparar() { e = new Escenario(); }

    @Test
    void rechazaDniDeSieteDigitos() {
        assertThrows(ValidacionException.class, () -> e.estudiantes.guardar(null, "Pedro", "Soto", "1ro Secundaria", "A", "1234567"));
    }

    @Test
    void rechazaDniRepetido() {
        assertThrows(ValidacionException.class, () -> e.estudiantes.guardar(null, "Pedro", "Soto", "1ro Secundaria", "A", "71234501"));
    }

    @Test
    void rechazaGradoInventado() {
        assertThrows(ValidacionException.class, () -> e.estudiantes.guardar(null, "Pedro", "Soto", "9no Grado", "A", "70000001"));
    }

    @Test
    void rechazaNombreVacio() {
        assertThrows(ValidacionException.class, () -> e.estudiantes.guardar(null, " ", "Soto", "1ro Secundaria", "A", "70000001"));
    }

    @Test
    void guardaUnEstudianteValido() {
        e.estudiantes.guardar(null, "Pedro", "Soto", "1ro Secundaria", "A", "70000001");
        assertTrue(e.estudiantesDao.buscarPorDni("70000001").isPresent());
    }

    @Test
    void importaUnCsvOmitiendoLasFilasConError() {
        String csv = "nombres,apellidos,grado,seccion,dni\nJuan,Pérez,1ro Secundaria,A,70000002\nMala,Fila,1ro Secundaria,A,123\n";
        var r = e.estudiantes.importarCsv(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, r.creados());
        assertEquals(1, r.omitidos().size());
    }

    @Test
    void importaCsvConPuntoYComa() {
        String csv = "nombres;apellidos;grado;seccion;dni\nAna;Ruiz;2do Secundaria;b;70000003\n";
        var r = e.estudiantes.importarCsv(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, r.creados());
    }
}
