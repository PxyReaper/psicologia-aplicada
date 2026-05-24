package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.tfg.proyectolibreria.psicologiaAplicada.session.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class CalendarAsyncService {



    private final GoogleCalendarService googleCalendarService;
    private final SessionRepository sessionRepository;

    public CalendarAsyncService(GoogleCalendarService googleCalendarService, SessionRepository sessionRepository) {
        this.googleCalendarService = googleCalendarService;
        this.sessionRepository = sessionRepository;
    }

    @Async
    public void createAndStoreEvent(Long sessionId, String patientFullName,
                                     LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        log.info("Creating calendar event for session {} (patient {})", sessionId, patientFullName);

        String eventId = googleCalendarService.createSessionEvent(patientFullName, sessionDateTime, sessionDateTimeEnd);

        if (eventId != null) {
            sessionRepository.findById(sessionId).ifPresent(session -> {
                session.setGoogleEventId(eventId);
                sessionRepository.save(session);
                log.info("Stored googleEventId {} for session {}", eventId, sessionId);
            });
        }
    }

    @Async
    public void updateAndStoreEvent(Long sessionId, String patientFullName,
                                     LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd,
                                     String oldEventId,
                                     String oldPatientFullName,
                                     LocalDateTime oldSessionDateTime,
                                     LocalDateTime oldSessionDateTimeEnd) {
        log.info("Updating calendar for session {} (patient {})", sessionId, patientFullName);

        if (oldEventId != null) {
            googleCalendarService.deleteSessionEvent(oldEventId);
        } else {
            log.warn("No googleEventId stored for session {}, falling back to search by time", sessionId);
            googleCalendarService.deleteSessionEvent(oldPatientFullName, oldSessionDateTime, oldSessionDateTimeEnd);
        }

        String newEventId = googleCalendarService.createSessionEvent(patientFullName, sessionDateTime, sessionDateTimeEnd);

        if (newEventId != null) {
            sessionRepository.findById(sessionId).ifPresent(session -> {
                session.setGoogleEventId(newEventId);
                sessionRepository.save(session);
                log.info("Updated googleEventId {} for session {}", newEventId, sessionId);
            });
        }
    }

    @Async
    public void deleteEvent(String eventId) {
        if (eventId != null) {
            log.info("Deleting calendar event: {}", eventId);
            googleCalendarService.deleteSessionEvent(eventId);
        }
    }
}
