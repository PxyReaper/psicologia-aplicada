package com.tfg.proyectolibreria.psicologiaAplicada.kernel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientAccess {
    Optional<Patient> findById(Long id);
    List<Patient> findActivePatientsInRange(LocalDate rangeStart, LocalDate rangeEnd);
}
