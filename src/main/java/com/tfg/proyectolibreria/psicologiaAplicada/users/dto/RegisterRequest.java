package com.tfg.proyectolibreria.psicologiaAplicada.users.dto;

import com.tfg.proyectolibreria.psicologiaAplicada.users.enums.UserRole;

public record RegisterRequest(String email, String username, String password, String name, String surname, UserRole role) {
}
