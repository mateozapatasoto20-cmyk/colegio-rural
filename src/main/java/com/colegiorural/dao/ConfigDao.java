package com.colegiorural.dao;

import java.util.Map;

public interface ConfigDao {
    Map<String, String> todos();
    void guardar(String clave, String valor);
}
