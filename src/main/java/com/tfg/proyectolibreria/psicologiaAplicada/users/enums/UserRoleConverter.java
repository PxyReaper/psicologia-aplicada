package com.tfg.proyectolibreria.psicologiaAplicada.users.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        if (role == null) return null;
        return role.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String s) {
        if (s == null) return null;
        return Stream.of(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
