package com.tfg.proyectolibreria.psicologiaAplicada.users.email;

public interface EmailService {

    void sendPasswordEmail(String to, String name, String rawPassword);
}
