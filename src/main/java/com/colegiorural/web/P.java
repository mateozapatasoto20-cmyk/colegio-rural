package com.colegiorural.web;

import com.colegiorural.servicio.ValidacionException;
import com.google.common.base.Strings;

/** Conversión de lo que llega del navegador (siempre texto) a números, sin romper la página si escriben algo raro. */
public final class P {
    private P() { }

    public static int pagina(String v) { return (int) Math.max(1, entero(v, 1)); }

    public static long entero(String v, long defecto) {
        if (v == null || v.isBlank()) { return defecto; }
        try { return Long.parseLong(v.trim()); } catch (NumberFormatException e) { return defecto; }
    }

    /** Número decimal opcional: vacío = null; "12,5" y "12.5" valen igual; algo que no es número lanza ValidacionException. */
    public static Double decimal(String v, String campo) {
        if (v == null || v.isBlank()) { return null; }
        try {
            double d = Double.parseDouble(v.trim().replace(',', '.'));
            if (Double.isNaN(d) || Double.isInfinite(d)) { throw new NumberFormatException(); }
            return d;
        } catch (NumberFormatException e) {
            throw new ValidacionException(campo + " debe ser un número.");
        }
    }

    public static Integer entero(String v, String campo) {
        if (v == null || v.isBlank()) { return null; }
        try { return Integer.valueOf(v.trim()); } catch (NumberFormatException e) { throw new ValidacionException(campo + " debe ser un número entero."); }
    }

    public static String texto(String v) { return Strings.nullToEmpty(v).trim(); }
}
