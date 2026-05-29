package com.tfg.proyectolibreria.psicologiaAplicada.patients.dto;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;

import java.time.LocalDate;
import java.util.List;

public record PatientsResponseDTO(Long id, String name, String surname, LocalDate startDate, LocalDate endDate, LocalDate birthDay, String cellPhone, Genre genre, List<String> observations) {
}
