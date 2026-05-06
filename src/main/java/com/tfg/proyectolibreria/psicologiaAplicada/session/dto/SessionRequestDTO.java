package com.tfg.proyectolibreria.psicologiaAplicada.session.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionRequestDTO(LocalDateTime dateSession,String observatory, String observatorySummary,Long idPatient) {
}
