package com.tfg.proyectolibreria.psicologiaAplicada.users.dto;

import com.tfg.proyectolibreria.psicologiaAplicada.users.enums.UserRole;

public record UpdateUserRequest(String email, String username, String name, String surname, UserRole role, Boolean enabled) {
}
