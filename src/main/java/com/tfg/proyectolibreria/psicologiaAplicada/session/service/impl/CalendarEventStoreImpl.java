package com.tfg.proyectolibreria.psicologiaAplicada.session.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.CalendarEventStore;
import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CalendarEventStoreImpl implements CalendarEventStore {

    private final SessionRepository sessionRepository;

    public CalendarEventStoreImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void storeEventId(Long sessionId, String eventId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setGoogleEventId(eventId);
            sessionRepository.save(session);
            log.info("Stored googleEventId {} for session {}", eventId, sessionId);
        });
    }
}
