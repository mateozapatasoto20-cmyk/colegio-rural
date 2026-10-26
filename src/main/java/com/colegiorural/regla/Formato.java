package com.colegiorural.regla;

import java.util.Locale;

/** Números sin ceros de más: 8.0 se muestra 8 y 12.5 se queda 12.5. */
public final class Formato {
    private Formato() { }

    public static String numero(double v) {
        String s = String.format(Locale.US, "%.1f", v);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }
}
