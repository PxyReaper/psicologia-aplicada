package com.tfg.proyectolibreria.psicologiaAplicada.kernel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientAccess {
    Optional<Patient> findById(Long id);
    List<Patient> findByIdIn(List<Long> ids);
    Page<Patient> findActivePatientsInRange(LocalDate rangeStart, LocalDate rangeEnd, Pageable pageable);
}
