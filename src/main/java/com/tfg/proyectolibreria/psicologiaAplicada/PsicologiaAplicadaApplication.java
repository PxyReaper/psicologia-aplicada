package com.tfg.proyectolibreria.psicologiaAplicada;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class PsicologiaAplicadaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsicologiaAplicadaApplication.class, args);
    }
}

