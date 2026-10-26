package com.colegiorural.dominio;

/** Estados posibles de un día de clase. El valor es el texto que se guarda en la base de datos. */
public enum EstadoAsistencia {
    PRESENTE("presente", "Presente"),
    TARDANZA("tardanza", "Tardanza"),
    FALTA("falta", "Falta"),
    JUSTIFICADA("justificada", "Justificada");

    private final String valor;
    private final String etiqueta;

    EstadoAsistencia(String valor, String etiqueta) { this.valor = valor; this.etiqueta = etiqueta; }

    public String valor() { return valor; }
    public String etiqueta() { return etiqueta; }

    /** Cuanto más alto, "peor" para mostrar el día en el calendario cuando hay varios cursos. */
    public int gravedad() { return ordinal() == 0 ? 1 : ordinal() == 3 ? 2 : ordinal() == 1 ? 3 : 4; }

    public static EstadoAsistencia desde(String valor) {
        for (EstadoAsistencia e : values()) { if (e.valor.equals(valor)) { return e; } }
        return null;
    }
}
