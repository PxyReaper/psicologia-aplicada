package com.tfg.proyectolibreria.psicologiaAplicada.observations.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.ObservationStore;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.repository.ObservationsRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObservationStoreImpl implements ObservationStore {
    private final ObservationsRepository observationsRepository;

    public ObservationStoreImpl(ObservationsRepository observationsRepository) {
        this.observationsRepository = observationsRepository;
    }

    @Override
    public List<String> findObservationsByPatientId(Long patientId) {
        return observationsRepository.findByPatientId(patientId)
                .stream()
                .map(o -> o.getObservation())
                .toList();
    }
}
