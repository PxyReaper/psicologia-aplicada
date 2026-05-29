package com.tfg.proyectolibreria.psicologiaAplicada.patients.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Genre {
    MASCULINO("masculino"),
    FEMENINO("femenino");

    private final String value;

    private Genre(String value){
        this.value = value;
    }

    @JsonValue
    public String getValue(){
        return this.value;
    }

    @JsonCreator
    public static Genre fromValue(String value) {
        for (Genre genre : values()) {
            if (genre.value.equalsIgnoreCase(value)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Invalid genre value: " + value);
    }
}



