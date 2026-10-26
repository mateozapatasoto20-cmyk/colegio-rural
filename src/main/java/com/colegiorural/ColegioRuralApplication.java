package com.colegiorural;

import com.colegiorural.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Punto de entrada. Spring Boot arma el servidor, la conexión a la BD y conecta todas las clases (inyección de dependencias). */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ColegioRuralApplication {
    public static void main(String[] args) {
        SpringApplication.run(ColegioRuralApplication.class, args);
    }
}
