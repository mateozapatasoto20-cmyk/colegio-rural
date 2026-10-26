package com.colegiorural.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.colegiorural.memoria.Escenario;
import com.colegiorural.regla.ReglaAlerta;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlertaServiceTest {
    private Escenario e;

    @BeforeEach
    void preparar() { e = new Escenario(); }

    /** Principio abierto/cerrado: se agrega una regla nueva a la lista y AlertaService no se modifica. */
    @Test
    void unaReglaNuevaSeAplicaSinTocarElServicio() {
        e.reglas.add(d -> d.nota() != null && d.nota() == 20
                ? Optional.of(new ReglaAlerta.Resultado("manual", "Felicitar: nota perfecta")) : Optional.empty());
        assertEquals(1, e.calificaciones.registrar(3, "Comunicación", 20.0, 100.0));
    }

    @Test
    void laAlertaManualSeCreaYSeResuelve() {
        e.alertas.crearManual(1, "Citar al apoderado");
        assertEquals(1, e.alertas.activas());
        long id = e.alertasDao.datos.get(0).id();
        e.alertas.resolver(id);
        assertEquals(0, e.alertas.activas());
    }

    @Test
    void laAlertaManualNoPuedeEstarVacia() {
        assertThrows(ValidacionException.class, () -> e.alertas.crearManual(1, "  "));
    }
}
