package com.tfg.proyectolibreria.psicologiaAplicada.patients.service;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.repository.PatientsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<Patient> findByIdIn(List<Long> ids) {
        return patientsRepository.findByIdIn(ids).stream()
                .map(p -> (Patient) p)
                .toList();
    }

    @Override
    public Page<Patient> findActivePatientsInRange(LocalDate rangeStart, LocalDate rangeEnd, Pageable pageable) {
        return patientsRepository.findActiveInRange(rangeStart, rangeEnd, pageable)
                .map(p -> (Patient) p);
    }
}
