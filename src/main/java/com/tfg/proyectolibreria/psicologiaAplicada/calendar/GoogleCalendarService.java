package com.tfg.proyectolibreria.psicologiaAplicada.calendar;

import java.time.LocalDateTime;
import java.util.List;

public interface GoogleCalendarService {

    void createSessionEvent(String patientFullName, LocalDateTime sessionDateTime, LocalDateTime sessionDateTimeEnd);

    List<String> listAccessibleCalendars();
}
