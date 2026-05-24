package com.tfg.proyectolibreria.psicologiaAplicada.patients.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientUpdatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.repository.PatientsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.service.PatientsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PatientsServiceImpl implements PatientsService {
    private final PatientsRepository patientsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void save(PatientsRequestDTO requestDTO) {
        PatientsEntity patient = new PatientsEntity(null, requestDTO.name(), requestDTO.surname(), LocalDate.now(), null,
                requestDTO.birthDay(), requestDTO.cellPhone(), requestDTO.genre());

        patientsRepository.save(patient);

        eventPublisher.publishEvent(new PatientCreatedEvent(
                patient.getId(),
                patient.getName() + " " + patient.getSurname(),
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
    }
}
