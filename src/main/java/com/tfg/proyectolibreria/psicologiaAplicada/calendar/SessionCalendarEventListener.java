package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.tfg.proyectolibreria.psicologiaAplicada.session.event.SessionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SessionCalendarEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionCalendarEventListener.class);

    private final GoogleCalendarService googleCalendarService;

    public SessionCalendarEventListener(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    @Async
    @EventListener
    public void onSessionCreated(SessionCreatedEvent event) {
        logger.info("Handling SessionCreatedEvent asynchronously for patient {}: {}", event.patientId(), event.patientFullName());
        googleCalendarService.createSessionEvent(
                event.patientFullName(),
                event.sessionDateTime(),
                event.sessionDateTimeEnd()
        );
    }
}
