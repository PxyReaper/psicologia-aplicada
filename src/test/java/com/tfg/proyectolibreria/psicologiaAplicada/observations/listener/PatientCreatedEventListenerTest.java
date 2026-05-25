package com.tfg.proyectolibreria.psicologiaAplicada.observations.listener;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.repository.ObservationsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.event.PatientUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PatientCreatedEventListenerTest {

    @Mock
    private ObservationsRepository observationsRepository;

    @InjectMocks
    private PatientCreatedEventListener listener;

    @Test
    void onPatientCreated_shouldSaveObservationWhenPresent() {
        PatientCreatedEvent event = new PatientCreatedEvent(1L, "John Doe", "Initial observation");

        listener.onPatientCreated(event);

        ArgumentCaptor<ObservationsEntity> captor = ArgumentCaptor.forClass(ObservationsEntity.class);
        verify(observationsRepository).save(captor.capture());
        assertThat(captor.getValue().getObservation()).isEqualTo("Initial observation");
        assertThat(captor.getValue().getPatientId()).isEqualTo(1L);
    }

    @Test
    void onPatientCreated_shouldNotSaveObservationWhenNull() {
        PatientCreatedEvent event = new PatientCreatedEvent(1L, "John Doe", null);

        listener.onPatientCreated(event);

        verify(observationsRepository, never()).save(any());
    }

    @Test
    void onPatientCreated_shouldNotSaveObservationWhenBlank() {
        PatientCreatedEvent event = new PatientCreatedEvent(1L, "John Doe", "   ");

        listener.onPatientCreated(event);

        verify(observationsRepository, never()).save(any());
    }

    @Test
    void onPatientUpdated_shouldSaveObservationWhenPresent() {
        PatientUpdatedEvent event = new PatientUpdatedEvent(1L, "John Doe", "Updated observation");

        listener.onPatientUpdated(event);

        verify(observationsRepository).save(any(ObservationsEntity.class));
    }

    @Test
    void onPatientUpdated_shouldNotSaveObservationWhenNull() {
        PatientUpdatedEvent event = new PatientUpdatedEvent(1L, "John Doe", null);

        listener.onPatientUpdated(event);

        verify(observationsRepository, never()).save(any());
    }

}
