package com.tfg.proyectolibreria.psicologiaAplicada.observations.service;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ObservationService {
    Page<PatientObservationsDTO> getActivePatientsWithObservations(LocalDate rangeStart, LocalDate rangeEnd, Pageable pageable);
}
