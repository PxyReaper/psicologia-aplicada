package com.tfg.proyectolibreria.psicologiaAplicada.patients.event;

public record PatientUpdatedEvent(Long patientId, String patientFullName, String observation) {
}
