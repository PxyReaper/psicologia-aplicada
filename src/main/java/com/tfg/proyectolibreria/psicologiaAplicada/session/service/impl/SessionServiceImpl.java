package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.session.service.SessionService;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final PatientAccess patientAccess;

    public SessionServiceImpl(SessionRepository sessionRepository, PatientAccess patientAccess) {
        this.sessionRepository = sessionRepository;
        this.patientAccess = patientAccess;
    }

    @Override
    public void save(SessionRequestDTO requestDTO) {
        patientAccess.findById(requestDTO.idPatient())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        SessionEntity session = new SessionEntity(
                null,
                requestDTO.dateSession(),
                requestDTO.observatory(),
                requestDTO.observatorySummary(),
                false,
                requestDTO.idPatient()
        );
        sessionRepository.save(session);
    }
}
