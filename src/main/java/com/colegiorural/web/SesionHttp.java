package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import com.colegiorural.servicio.UsuarioActual;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Implementa UsuarioActual con la sesión HTTP. Los servicios solo conocen la interfaz (DIP), nunca HttpSession. */
@Component
public class SesionHttp implements UsuarioActual {
    @Override
    public Optional<SesionUsuario> actual() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a) {
            HttpServletRequest r = a.getRequest();
            return Sesion.usuario(r);
        }
        return Optional.empty();
    }
}
