package com.colegiorural.dao;

import java.util.List;
import java.util.Map;

public interface RespaldoDao {
    /** Tablas que forman parte de la copia, en orden de inserción (primero las "padre"). */
    List<String> tablas();
    /** Una sentencia SQL por línea: primero los DELETE (hijas antes que padres), luego los INSERT. */
    List<String> volcar();
    /** Ejecuta sentencias ya validadas dentro de una transacción. */
    void restaurar(List<String> sentencias);
    Map<String, Integer> conteos();
    /** true si el motor escapa con barra invertida (MySQL / MariaDB). */
    boolean escapaConBarra();
}
