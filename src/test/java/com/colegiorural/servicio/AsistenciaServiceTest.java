package com.colegiorural.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colegiorural.dominio.EstadoAsistencia;
import com.colegiorural.memoria.Escenario;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AsistenciaServiceTest {
    private Escenario e;
    private final LocalDate hoy = LocalDate.now();

    @BeforeEach
    void preparar() { e = new Escenario(); }

    private void pasarLista(int diasAtras, EstadoAsistencia estudiante2) {
        Map<Long, EstadoAsistencia> estados = new LinkedHashMap<>();
        estados.put(1L, EstadoAsistencia.PRESENTE);
        estados.put(2L, estudiante2);
        e.asistencia.registrar(1, hoy.minusDays(diasAtras), estados);
    }

    @Test
    void tresFaltasSeguidasCreanAlerta() {
        for (int d = 2; d >= 0; d--) { pasarLista(d, EstadoAsistencia.FALTA); }
        assertTrue(e.alertasDao.datos.stream().anyMatch(a -> a.estudianteId() == 2));
    }

    @Test
    void unaFaltaSolaNoCreaAlerta() {
        pasarLista(0, EstadoAsistencia.FALTA);
        assertFalse(e.alertasDao.datos.stream().anyMatch(a -> a.estudianteId() == 2));
    }

    @Test
    void noSePuedePasarListaEnUnaFechaFutura() {
        assertThrows(ValidacionException.class, () -> e.asistencia.registrar(1, hoy.plusDays(1), Map.of(1L, EstadoAsistencia.FALTA)));
    }

    @Test
    void noSePuedeGuardarUnaListaVacia() {
        assertThrows(ValidacionException.class, () -> e.asistencia.registrar(1, hoy, Map.of()));
    }

    @Test
    void elResumenCuentaLasFaltas() {
        for (int d = 2; d >= 0; d--) { pasarLista(d, EstadoAsistencia.FALTA); }
        assertEquals(3, e.asistencia.resumenEstudiante(2).total().get(EstadoAsistencia.FALTA));
    }

    @Test
    void elCalendarioMarcaLosDiasConRegistro() {
        pasarLista(0, EstadoAsistencia.TARDANZA);
        var cal = e.asistencia.calendario(2, hoy.toString().substring(0, 7), null);
        assertEquals("tardanza", cal.porDia().get(hoy.getDayOfMonth()).estado());
    }
}
