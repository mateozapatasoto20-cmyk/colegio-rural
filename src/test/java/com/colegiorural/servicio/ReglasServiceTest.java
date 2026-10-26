package com.colegiorural.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.colegiorural.memoria.Escenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReglasServiceTest {
    private Escenario e;

    @BeforeEach
    void preparar() { e = new Escenario(); }

    @Test
    void rechazaValoresFueraDeRango() {
        assertThrows(ValidacionException.class, () -> e.reglasService.guardar(0.0, 65.0, 3));
        assertThrows(ValidacionException.class, () -> e.reglasService.guardar(11.0, 150.0, 3));
        assertThrows(ValidacionException.class, () -> e.reglasService.guardar(11.0, 65.0, 1));
        assertThrows(ValidacionException.class, () -> e.reglasService.guardar(null, 65.0, 3));
    }

    @Test
    void lasReglasNuevasSeAplicanEnseguida() {
        e.reglasService.guardar(14.0, 65.0, 3);
        assertEquals(1, e.calificaciones.registrar(3, "Arte", 12.0, 90.0));
    }
}
