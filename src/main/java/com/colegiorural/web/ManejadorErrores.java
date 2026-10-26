package com.colegiorural.web;

import com.colegiorural.servicio.ValidacionException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Cualquier ValidacionException que no atrape el controlador termina aquí: aviso rojo y se vuelve a la misma pantalla. */
@ControllerAdvice
public class ManejadorErrores {
    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);
    @ExceptionHandler(ValidacionException.class)
    public String validacion(ValidacionException e, HttpServletRequest req) {
        LOG.info("Validación rechazada en {}: {}", req.getRequestURI(), e.getMessage());
        Sesion.error(req, e.getMessage());
        return Sesion.volver(req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String grande(HttpServletRequest req) {
        Sesion.error(req, "El archivo es demasiado grande (máximo 6 MB).");
        return Sesion.volver(req);
    }
}
