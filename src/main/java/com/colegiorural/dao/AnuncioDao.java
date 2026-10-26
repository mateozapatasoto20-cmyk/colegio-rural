package com.colegiorural.dao;

import com.colegiorural.dominio.Anuncio;
import java.util.List;

public interface AnuncioDao {
    List<Anuncio> listar(List<String> audiencias, int limite);
    long crear(String titulo, String mensaje, String audiencia, String autor);
    void eliminar(long id);
}
