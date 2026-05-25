package com.tfg.proyectolibreria.psicologiaAplicada.session.dto;

import java.time.LocalDateTime;

public record SessionWithPatientDTO(
        Long id,
        LocalDateTime dateSession,
        LocalDateTime dateSessionEnd,
        String observation,
        String observationSummary,
        boolean pay,
        Long patientId,
        String patientName,
        String patientSurname
) {
}
