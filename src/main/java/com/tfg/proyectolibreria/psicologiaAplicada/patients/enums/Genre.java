package com.tfg.proyectolibreria.psicologiaAplicada.patients.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Genre {
    MASCULINO("masculino"),
    FEMENINO("femenino");

    private final String value;

    private Genre(String value){
        this.value = value;
    }
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



