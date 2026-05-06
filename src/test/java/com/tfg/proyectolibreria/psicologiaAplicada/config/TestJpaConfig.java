package com.tfg.proyectolibreria.psicologiaAplicada.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.tfg.proyectolibreria.psicologiaAplicada")
@EntityScan("com.tfg.proyectolibreria.psicologiaAplicada")
public class TestJpaConfig {
}
