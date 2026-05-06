package com.tfg.proyectolibreria.psicologiaAplicada.observations.service;

import com.tfg.proyectolibreria.psicologiaAplicada.kernel.Patient;
import com.tfg.proyectolibreria.psicologiaAplicada.kernel.PatientAccess;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.repository.ObservationsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.ObservationService;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.impl.ObservationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObservationServiceImplTest {

    @Mock
    private PatientAccess patientAccess;

    @Mock
    private ObservationsRepository observationsRepository;

    @InjectMocks
    private ObservationServiceImpl observationService;

    @Test
    void getActivePatientsWithObservations_shouldReturnPatientWithObservations() {
        Patient patient = new Patient() {
            @Override            public Long getId() {                return 1L;            }
            @Override            public String getName() {                return "John";            }
            @Override            public String getSurname() {                return "Doe";            }
        };

        ObservationsEntity obs = new ObservationsEntity(null, "Test observation", 1L);

        when(patientAccess.findActivePatientsInRange(any(), any())).thenReturn(List.of(patient));
        when(observationsRepository.findByPatientIdIn(any())).thenReturn(List.of(obs));

        List<PatientObservationsDTO> result = observationService.getActivePatientsWithObservations(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).patient().getId()).isEqualTo(1L);
        assertThat(result.get(0).observations()).hasSize(1);
    }

    @Test
    void getActivePatientsWithObservations_shouldReturnEmptyWhenNoPatients() {
        when(patientAccess.findActivePatientsInRange(any(), any())).thenReturn(List.of());

        List<PatientObservationsDTO> result = observationService.getActivePatientsWithObservations(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertThat(result).isEmpty();
    }
}
