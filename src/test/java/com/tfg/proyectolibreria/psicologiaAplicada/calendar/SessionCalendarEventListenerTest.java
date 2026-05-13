package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.tfg.proyectolibreria.psicologiaAplicada.session.event.SessionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionCalendarEventListenerTest {

    @Mock
    private GoogleCalendarService googleCalendarService;

    @InjectMocks
    private SessionCalendarEventListener listener;

    @Test
    void onSessionCreated_shouldCreateCalendarEvent() {
        LocalDateTime now = LocalDateTime.now();
        SessionCreatedEvent event = new SessionCreatedEvent(1L, "John Doe", now, now.plusHours(1));

        listener.onSessionCreated(event);

        verify(googleCalendarService).createSessionEvent("John Doe", now, now.plusHours(1));
    }
}
