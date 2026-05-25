package com.tfg.proyectolibreria.psicologiaAplicada.observations.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.repository.ObservationsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.ObservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ObservationServiceImpl implements ObservationService {

    private final PatientAccess patientAccess;
    private final ObservationsRepository observationsRepository;

    public ObservationServiceImpl(PatientAccess patientAccess, ObservationsRepository observationsRepository) {
        this.patientAccess = patientAccess;
        this.observationsRepository = observationsRepository;
    }

    @Override
    public Page<PatientObservationsDTO> getActivePatientsWithObservations(LocalDate rangeStart, LocalDate rangeEnd, Pageable pageable) {
        Page<Patient> activePatients = patientAccess.findActivePatientsInRange(rangeStart, rangeEnd, pageable);

        List<Long> patientIds = activePatients.getContent().stream()
                .map(Patient::getId)
                .toList();

        List<ObservationsEntity> observations = observationsRepository.findByPatientIdIn(patientIds);
        Map<Long, List<String>> observationsByPatient = observations.stream()
                .collect(Collectors.groupingBy(
                        ObservationsEntity::getPatientId,
                        Collectors.mapping(ObservationsEntity::getObservation, Collectors.toList())
                ));

        return activePatients.map(patient ->
                new PatientObservationsDTO(
                        patient,
                        observationsByPatient.getOrDefault(patient.getId(), List.of())
                )
        );
    }
}
