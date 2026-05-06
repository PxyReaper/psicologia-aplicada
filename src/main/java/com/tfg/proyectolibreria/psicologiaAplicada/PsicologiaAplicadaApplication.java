package com.tfg.proyectolibreria.psicologiaAplicada;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class PsicologiaAplicadaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsicologiaAplicadaApplication.class, args);
    }
}

