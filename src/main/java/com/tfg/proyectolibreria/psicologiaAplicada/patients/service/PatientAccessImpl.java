package com.tfg.proyectolibreria.psicologiaAplicada.patients.service;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.repository.PatientsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PatientAccessImpl implements PatientAccess {

    private final PatientsRepository patientsRepository;

    public PatientAccessImpl(PatientsRepository patientsRepository) {
        this.patientsRepository = patientsRepository;
    }

    @Override
    public Optional<Patient> findById(Long id) {
        return patientsRepository.findById(id).map(p -> (Patient) p);
    }

    @Override
    public List<Patient> findActivePatientsInRange(LocalDate rangeStart, LocalDate rangeEnd) {
        return patientsRepository.findActiveInRange(rangeStart, rangeEnd).stream()
                .map(p -> (Patient) p)
                .toList();
    }
}
