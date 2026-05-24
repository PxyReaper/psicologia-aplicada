package com.tfg.proyectolibreria.psicologiaAplicada.session.event;

import java.time.LocalDateTime;

public record SessionCreatedEvent(Long patientId, String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
}
