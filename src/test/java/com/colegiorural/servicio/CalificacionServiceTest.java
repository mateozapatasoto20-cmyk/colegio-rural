package com.colegiorural.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colegiorural.memoria.Escenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** "Ingresar nota si es correcto": primero se escribió la prueba de lo que debe rechazarse y luego el servicio (TDD). */
class CalificacionServiceTest {
    private Escenario e;

    @BeforeEach
    void preparar() { e = new Escenario(); }

    @Test
    void rechazaNotaMayorA20() {
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(1, "Matemática", 21.0, 90.0));
    }

    @Test
    void rechazaNotaNegativa() {
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(1, "Matemática", -1.0, 90.0));
    }

    @Test
    void rechazaNotaVacia() {
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(1, "Matemática", null, 90.0));
    }

    @Test
    void rechazaAsistenciaMayorA100() {
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(1, "Matemática", 15.0, 101.0));
    }

    @Test
    void rechazaCursoVacio() {
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(1, "   ", 15.0, 90.0));
    }

    @Test
    void rechazaEstudianteInactivoOInexistente() {
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(4, "Matemática", 15.0, 90.0));
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(99, "Matemática", 15.0, 90.0));
    }

    @Test
    void siHayErrorNoSeGuardaNada() {
        assertThrows(ValidacionException.class, () -> e.calificaciones.registrar(1, "Matemática", 21.0, 90.0));
        assertTrue(e.calificacionesDao.datos.isEmpty());
    }

    @Test
    void notaBajaCreaUnaAlerta() {
        assertEquals(1, e.calificaciones.registrar(1, "Matemática", 8.0, 90.0));
        assertEquals("nota", e.alertasDao.datos.get(0).tipo());
    }

    @Test
    void laMismaAlertaNoSeDuplica() {
        e.calificaciones.registrar(1, "Matemática", 8.0, 90.0);
        assertEquals(0, e.calificaciones.registrar(1, "Matemática", 8.0, 90.0));
    }

    @Test
    void notaAprobatoriaNoCreaAlerta() {
        assertEquals(0, e.calificaciones.registrar(2, "Matemática", 15.0, 90.0));
    }

    @Test
    void laNotaMinimaExactaNoEsRiesgo() {
        assertEquals(0, e.calificaciones.registrar(3, "Matemática", 11.0, 80.0));
    }

    @Test
    void asistenciaBajaCreaAlertaDeAsistencia() {
        assertEquals(1, e.calificaciones.registrar(2, "Comunicación", 15.0, 50.0));
        assertEquals("asistencia", e.alertasDao.datos.get(0).tipo());
    }
}
