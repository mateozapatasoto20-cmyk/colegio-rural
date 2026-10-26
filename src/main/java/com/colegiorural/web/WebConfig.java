package com.colegiorural.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AccesoInterceptor acceso;
    private final ContextoInterceptor contexto;

    public WebConfig(AccesoInterceptor acceso, ContextoInterceptor contexto) { this.acceso = acceso; this.contexto = contexto; }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(acceso).addPathPatterns("/**").excludePathPatterns("/assets/**", "/error");
        registro.addInterceptor(contexto).addPathPatterns("/**").excludePathPatterns("/assets/**", "/error");
    }
}
