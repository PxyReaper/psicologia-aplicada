package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.CalendarEventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarAsyncServiceTest {

    @Mock
    private GoogleCalendarService googleCalendarService;

    @Mock
    private CalendarEventStore calendarEventStore;

    @InjectMocks
    private CalendarAsyncService calendarAsyncService;

    @Test
    void createAndStoreEvent_shouldStoreEventIdWhenCreated() {
        when(googleCalendarService.createSessionEvent("John Doe",
                LocalDateTime.of(2026, 5, 25, 10, 0),
                LocalDateTime.of(2026, 5, 25, 11, 0)))
                .thenReturn("event-123");

        calendarAsyncService.createAndStoreEvent(1L, "John Doe",
                LocalDateTime.of(2026, 5, 25, 10, 0),
                LocalDateTime.of(2026, 5, 25, 11, 0));

        verify(calendarEventStore).storeEventId(1L, "event-123");
    }

    @Test
    void createAndStoreEvent_shouldNotStoreWhenEventIdIsNull() {
        when(googleCalendarService.createSessionEvent(anyString(), any(), any())).thenReturn(null);

        calendarAsyncService.createAndStoreEvent(1L, "John Doe",
                LocalDateTime.of(2026, 5, 25, 10, 0),
                LocalDateTime.of(2026, 5, 25, 11, 0));

        verify(calendarEventStore, never()).storeEventId(any(), any());
    }

    @Test
    void deleteEvent_shouldCallServiceWhenEventIdNotNull() {
        calendarAsyncService.deleteEvent("event-123");

        verify(googleCalendarService).deleteSessionEvent("event-123");
    }

    @Test
    void deleteEvent_shouldNotCallServiceWhenEventIdNull() {
        calendarAsyncService.deleteEvent(null);

        verify(googleCalendarService, never()).deleteSessionEvent(anyString());
    }
}
