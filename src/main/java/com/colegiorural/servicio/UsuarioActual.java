package com.colegiorural.servicio;

import com.colegiorural.dominio.SesionUsuario;
import java.util.Optional;

/* DIP: los servicios no saben de HTTP ni de sesiones. Piden "el usuario actual" a esta interfaz;
   en la web la implementa SesionHttp y en las pruebas se pasa una función cualquiera. */
public interface UsuarioActual {
    Optional<SesionUsuario> actual();
}
