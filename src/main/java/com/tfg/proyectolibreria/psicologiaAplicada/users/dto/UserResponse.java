package com.tfg.proyectolibreria.psicologiaAplicada.users.dto;

import com.tfg.proyectolibreria.psicologiaAplicada.users.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(Long id, String email, String username, String name, String surname, UserRole role, boolean enabled, LocalDateTime createdAt) {
}
