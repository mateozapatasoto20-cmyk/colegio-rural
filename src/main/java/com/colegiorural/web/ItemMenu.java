package com.colegiorural.web;

import com.google.common.collect.ImmutableList;
import java.util.List;

public record ItemMenu(String ruta, String icono, String titulo, List<String> roles) {
    public static final List<ItemMenu> TODOS = ImmutableList.of(
        new ItemMenu("/dashboard",     "inicio",   "Inicio",                List.of("admin", "coordinador", "docente")),
        new ItemMenu("/portal",        "notas",    "Mis notas",             List.of("alumno")),
        new ItemMenu("/estudiantes",   "alumnos",  "Estudiantes",           List.of("admin", "docente")),
        new ItemMenu("/docentes",      "docentes", "Docentes",              List.of("admin", "coordinador")),
        new ItemMenu("/cursos",        "cursos",   "Cursos",                List.of("admin", "coordinador")),
        new ItemMenu("/calificaciones","notas",    "Notas",                 List.of("admin", "docente")),
        new ItemMenu("/asistencia",    "lista",    "Pase de lista",         List.of("admin", "docente")),
        new ItemMenu("/alertas",       "alerta",   "Alertas",               List.of("admin", "coordinador", "docente")),
        new ItemMenu("/comunicados",   "mega",     "Comunicados",           List.of("admin", "coordinador", "docente", "alumno")),
        new ItemMenu("/reportes",      "reportes", "Reportes",              List.of("admin", "coordinador", "docente")),
        new ItemMenu("/usuarios",      "usuarios", "Usuarios",              List.of("admin")),
        new ItemMenu("/reglas",        "config",   "Reglas del sistema",    List.of("admin")),
        new ItemMenu("/respaldo",      "respaldo", "Copia de seguridad",    List.of("admin")),
        new ItemMenu("/logs",          "logs",     "Registro de actividad", List.of("admin")),
        new ItemMenu("/perfil",        "perfil",   "Mi perfil",             List.of("admin", "coordinador", "docente", "alumno")));

    public static List<ItemMenu> para(String rol) { return TODOS.stream().filter(i -> i.roles().contains(rol)).toList(); }
}
