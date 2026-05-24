package com.tfg.proyectolibreria.psicologiaAplicada.users.event;

public record UserCreatedEvent(String email, String name, String surname, String rawPassword) {
}
