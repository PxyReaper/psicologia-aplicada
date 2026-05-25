package com.tfg.proyectolibreria.psicologiaAplicada.kernel;

public interface CalendarEventStore {
    void storeEventId(Long sessionId, String eventId);
}
