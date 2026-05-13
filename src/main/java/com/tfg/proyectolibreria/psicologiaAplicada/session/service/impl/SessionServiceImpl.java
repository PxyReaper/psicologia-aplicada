package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.event.SessionCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.session.service.SessionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final PatientAccess patientAccess;
    private final ApplicationEventPublisher eventPublisher;

    public SessionServiceImpl(SessionRepository sessionRepository,
                              PatientAccess patientAccess,
                              ApplicationEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.patientAccess = patientAccess;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(SessionRequestDTO requestDTO) {
        var patient = patientAccess.findById(requestDTO.idPatient())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        SessionEntity session = new SessionEntity(
                null,
                requestDTO.dateSession(),
                requestDTO.dateSessionEnd(),
                requestDTO.observatory(),
                requestDTO.observatorySummary(),
                false,
                requestDTO.idPatient()
        );
        sessionRepository.save(session);

        String patientFullName = patient.getName() + " " + patient.getSurname();
        eventPublisher.publishEvent(new SessionCreatedEvent(
                requestDTO.idPatient(),
                patientFullName,
                requestDTO.dateSession(),
                requestDTO.dateSessionEnd()
        ));
    }
}
