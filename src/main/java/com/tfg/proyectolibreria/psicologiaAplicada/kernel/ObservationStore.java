package com.tfg.proyectolibreria.psicologiaAplicada.kernel;

import java.util.List;

public interface ObservationStore {
    List<String> findObservationsByPatientId(Long patientId);
}
