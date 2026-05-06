package com.tfg.proyectolibreria.psicologiaAplicada.observations.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.repository.ObservationsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.ObservationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ObservationServiceImpl implements ObservationService {

    private final PatientAccess patientAccess;
    private final ObservationsRepository observationsRepository;

    public ObservationServiceImpl(PatientAccess patientAccess, ObservationsRepository observationsRepository) {
        this.patientAccess = patientAccess;
        this.observationsRepository = observationsRepository;
    }

    @Override
    public List<PatientObservationsDTO> getActivePatientsWithObservations(LocalDate rangeStart, LocalDate rangeEnd) {
        List<Patient> activePatients = patientAccess.findActivePatientsInRange(rangeStart, rangeEnd);
        List<Long> patientIds = activePatients.stream()
                .map(Patient::getId)
                .toList();

        List<ObservationsEntity> observations = observationsRepository.findByPatientIdIn(patientIds);

        return activePatients.stream()
                .map(patient -> {
                    List<String> patientObservations = observations.stream()
                            .filter(o -> o.getPatientId().equals(patient.getId()))
                            .map(ObservationsEntity::getObservation)
                            .toList();
                    return new PatientObservationsDTO(patient, patientObservations);
                })
                .toList();
    }
}
