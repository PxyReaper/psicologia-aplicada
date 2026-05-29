package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.CalendarEventStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class CalendarAsyncService {

    private final GoogleCalendarService googleCalendarService;
    private final CalendarEventStore calendarEventStore;

    public CalendarAsyncService(GoogleCalendarService googleCalendarService, CalendarEventStore calendarEventStore) {
        this.googleCalendarService = googleCalendarService;
        this.calendarEventStore = calendarEventStore;
    }

    @Async
    public void createAndStoreEvent(Long sessionId, String patientFullName,
                                     LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd) {
        log.info("Creating calendar event for session {} (patient {})", sessionId, patientFullName);

        String eventId = googleCalendarService.createSessionEvent(patientFullName, sessionDateTime, sessionDateTimeEnd);

        if (eventId != null) {
            calendarEventStore.storeEventId(sessionId, eventId);
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

        String resolvedEventId = oldEventId;

        if (resolvedEventId == null) {
            log.warn("No googleEventId stored for session {}, searching by date/name...", sessionId);
            resolvedEventId = googleCalendarService.findSessionEventId(oldPatientFullName, oldSessionDateTime);
        }

        if (resolvedEventId != null) {
            googleCalendarService.updateSessionEvent(resolvedEventId, patientFullName, sessionDateTime, sessionDateTimeEnd);
        } else {
            log.warn("No existing event found, creating new one for session {}", sessionId);
            String newEventId = googleCalendarService.createSessionEvent(patientFullName, sessionDateTime, sessionDateTimeEnd);
            if (newEventId != null) {
                calendarEventStore.storeEventId(sessionId, newEventId);
            }
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
