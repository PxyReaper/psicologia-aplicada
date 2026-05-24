package com.tfg.proyectolibreria.psicologiaAplicada.patients.event;

public record PatientCreatedEvent(Long patientId, String patientFullName, String observation) {
}
