package com.tfg.proyectolibreria.psicologiaAplicada.patients.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.ObservationStore;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.SessionStore;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsResponseDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientUpdatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.repository.PatientsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.service.PatientsService;
import com.tfg.proyectolibreria.psicologiaAplicada.session.service.SessionService;
import com.tfg.proyectolibreria.psicologiaAplicada.web.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PatientsServiceImpl implements PatientsService {
    private final PatientsRepository patientsRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObservationStore observationStore;
    private final SessionStore sessionStore;
    private final SessionService sessionService;

    @Override
    public PatientsResponseDTO findById(Long id) {
        PatientsEntity patient = patientsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return new PatientsResponseDTO(
                patient.getId(),
                patient.getName(),
                patient.getSurname(),
                patient.getStartDate(),
                patient.getEndDate(),
                patient.getBirthDay(),
                patient.getCellPhone(),
                patient.getGenre(),
                observationStore.findObservationsByPatientId(id),
                sessionStore.findSessionsByPatientId(id)
        );
    }

    @Override
    public void save(PatientsRequestDTO requestDTO) {
        PatientsEntity patient = new PatientsEntity(null, requestDTO.name(), requestDTO.surname(), LocalDate.now(), null,
                requestDTO.birthDay(), requestDTO.cellPhone(), requestDTO.genre());

        PatientsEntity saved = patientsRepository.save(patient);

        eventPublisher.publishEvent(new PatientCreatedEvent(
                saved.getId(),
                saved.getName() + " " + saved.getSurname(),
                requestDTO.observation()
        ));
    }

    @Override
    public void update(Long id, PatientsRequestDTO requestDTO) {
        PatientsEntity patient = patientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        PatientsEntity updated = new PatientsEntity(
                id,
                requestDTO.name(),
                requestDTO.surname(),
                patient.getStartDate(),
                patient.getEndDate(),
                requestDTO.birthDay(),
                requestDTO.cellPhone(),
                requestDTO.genre()
        );

        patientsRepository.save(updated);

        eventPublisher.publishEvent(new PatientUpdatedEvent(
                id,
                updated.getName() + " " + updated.getSurname(),
                requestDTO.observation()
        ));
    }

    @Override
    public void discharge(Long id) {
        PatientsEntity patient = patientsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        PatientsEntity discharged = new PatientsEntity(
                id,
                patient.getName(),
                patient.getSurname(),
                patient.getStartDate(),
                LocalDate.now(),
                patient.getBirthDay(),
                patient.getCellPhone(),
                patient.getGenre()
        );

        patientsRepository.save(discharged);
        sessionService.deleteUpcomingSessionsByPatientId(id);
    }
}
