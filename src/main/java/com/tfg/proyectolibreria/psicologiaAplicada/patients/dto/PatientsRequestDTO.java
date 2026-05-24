package com.tfg.proyectolibreria.psicologiaAplicada.patients.dto;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;

import java.time.LocalDate;

public record PatientsRequestDTO(String name, String surname, LocalDate birthDay, String cellPhone, Genre genre, String observation) {
}
