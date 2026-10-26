package com.colegiorural.web;

import com.colegiorural.dominio.SesionUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Todo lo que se guarda en la sesión HTTP: usuario, token CSRF, avisos y el diálogo a reabrir. Es la única clase que toca HttpSession. */
public final class Sesion {
    public record Aviso(String tipo, String texto) implements java.io.Serializable { }

    private static final String USUARIO = "usuario";
    private static final String CSRF = "csrf";
    private static final String AVISOS = "avisos";
    private static final String DIALOGO = "dialogo";
    private static final SecureRandom AZAR = new SecureRandom();

    private Sesion() { }

    public static Optional<SesionUsuario> usuario(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        return s == null ? Optional.empty() : Optional.ofNullable((SesionUsuario) s.getAttribute(USUARIO));
    }

    /** Inicia sesión: cambia el id de sesión (evita fijación de sesión) y renueva el token CSRF. */
    public static void iniciar(HttpServletRequest r, SesionUsuario u) {
        r.getSession(true);
        r.changeSessionId();
        HttpSession s = r.getSession();
        s.removeAttribute(CSRF);
        s.setAttribute(USUARIO, u);
    }

    public static void cerrar(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        if (s != null) { s.invalidate(); }
    }

    public static String csrf(HttpServletRequest r) {
        HttpSession s = r.getSession(true);
        String t = (String) s.getAttribute(CSRF);
        if (t == null) {
            byte[] b = new byte[24];
            AZAR.nextBytes(b);
            t = HexFormat.of().formatHex(b);
            s.setAttribute(CSRF, t);
        }
        return t;
    }

    public static void aviso(HttpServletRequest r, String tipo, String texto) {
        HttpSession s = r.getSession(true);
        @SuppressWarnings("unchecked")
        List<Aviso> l = (List<Aviso>) s.getAttribute(AVISOS);
        if (l == null) { l = new ArrayList<>(); s.setAttribute(AVISOS, l); }
        l.add(new Aviso(tipo, texto));
    }

    public static void ok(HttpServletRequest r, String texto) { aviso(r, "ok", texto); }
    public static void error(HttpServletRequest r, String texto) { aviso(r, "error", texto); }

    public static List<Aviso> tomarAvisos(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        if (s == null) { return List.of(); }
        @SuppressWarnings("unchecked")
        List<Aviso> l = (List<Aviso>) s.getAttribute(AVISOS);
        s.removeAttribute(AVISOS);
        return l == null ? List.of() : l;
    }

    /** Guarda lo escrito en un formulario para volver a abrirlo con el error (patrón Post/Redirect/Get). */
    public static void dialogo(HttpServletRequest r, String id, String titulo, Map<String, String> datos, String error) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("abrir", id);
        m.put("titulo", titulo);
        m.put("datos", datos);
        m.put("error", error);
        r.getSession(true).setAttribute(DIALOGO, m);
    }

    public static Map<String, Object> tomarDialogo(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        if (s == null) { return null; }
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) s.getAttribute(DIALOGO);
        s.removeAttribute(DIALOGO);
        return m;
    }

    /** Después de un POST se vuelve a la misma pantalla con los mismos filtros. */
    public static String volver(HttpServletRequest r) {
        String qs = r.getQueryString();
        return "redirect:" + r.getRequestURI() + (qs == null || qs.isEmpty() ? "" : "?" + qs);
    }
}
