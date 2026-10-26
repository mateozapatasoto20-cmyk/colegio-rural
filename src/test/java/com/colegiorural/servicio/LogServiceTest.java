package com.colegiorural.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.colegiorural.memoria.Escenario;
import org.junit.jupiter.api.Test;

class LogServiceTest {
    @Test
    void lasAccionesQuedanEnLaBitacoraConElUsuario() {
        Escenario e = new Escenario();
        e.calificaciones.registrar(1, "Matemática", 15.0, 90.0);
        assertTrue(e.logsDao.datos.size() >= 1);
        assertEquals("admin", e.logsDao.datos.get(0).usuario());
    }
}
