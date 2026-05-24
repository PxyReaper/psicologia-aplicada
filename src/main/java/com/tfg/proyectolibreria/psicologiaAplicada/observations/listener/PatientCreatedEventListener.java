package com.tfg.proyectolibreria.psicologiaAplicada.observations.listener;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.repository.ObservationsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PatientCreatedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PatientCreatedEventListener.class);

    private final ObservationsRepository observationsRepository;

    public PatientCreatedEventListener(ObservationsRepository observationsRepository) {
        this.observationsRepository = observationsRepository;
    }

    @EventListener
    public void onPatientCreated(PatientCreatedEvent event) {
        if (event.observation() == null || event.observation().isBlank()) {
            return;
        }

        logger.info("Saving observation for patient {}: {}", event.patientId(), event.patientFullName());

        ObservationsEntity observation = new ObservationsEntity(
                null,
                event.observation(),
                event.patientId()
        );

        observationsRepository.save(observation);
    }

    @EventListener
    public void onPatientUpdated(PatientUpdatedEvent event) {
        if (event.observation() == null || event.observation().isBlank()) {
            return;
        }

        logger.info("Adding observation on update for patient {}: {}", event.patientId(), event.patientFullName());

        ObservationsEntity observation = new ObservationsEntity(
                null,
                event.observation(),
                event.patientId()
        );

        observationsRepository.save(observation);
    }
}
