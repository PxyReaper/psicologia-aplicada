package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.SessionStore;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionStoreImpl implements SessionStore {
    private final SessionRepository sessionRepository;

    public SessionStoreImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<SessionData> findSessionsByPatientId(Long patientId) {
        return sessionRepository.findByPatientId(patientId)
                .stream()
                .map(s -> new SessionData(s.getSessionDate(), s.getSessionDateEnd(), s.isPay()))
                .toList();
    }
}
