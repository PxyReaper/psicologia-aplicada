package com.tfg.proyectolibreria.psicologiaAplicada;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;


class PsicologiaAplicadaApplicationTests {

    @Test
    void contextLoads() {
        ApplicationModules modules = ApplicationModules.of(PsicologiaAplicadaApplication.class);
        modules.forEach(System.out::println); // muestra los módulos detectados
        modules.verify();
    }

}
