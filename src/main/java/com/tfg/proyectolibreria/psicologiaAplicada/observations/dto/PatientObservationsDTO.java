package com.tfg.proyectolibreria.psicologiaAplicada.observations.dto;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import lombok.NoArgsConstructor;

import java.util.List;

public record PatientObservationsDTO(Patient patient, List<String> observations) {
}
