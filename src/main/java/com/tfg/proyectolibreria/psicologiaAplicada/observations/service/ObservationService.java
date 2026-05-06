package com.tfg.proyectolibreria.psicologiaAplicada.observations.service;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;

import java.time.LocalDate;
import java.util.List;

public interface ObservationService {
    List<PatientObservationsDTO> getActivePatientsWithObservations(LocalDate rangeStart, LocalDate rangeEnd);
}
