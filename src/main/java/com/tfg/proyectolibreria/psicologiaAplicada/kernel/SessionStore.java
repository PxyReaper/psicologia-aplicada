package com.tfg.proyectolibreria.psicologiaAplicada.kernel;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionStore {
    List<SessionData> findSessionsByPatientId(Long patientId);

    record SessionData(LocalDateTime sessionDate, LocalDateTime sessionDateEnd, boolean pay) {}
}
