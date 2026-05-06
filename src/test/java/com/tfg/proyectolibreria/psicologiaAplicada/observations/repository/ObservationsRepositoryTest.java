package com.tfg.proyectolibreria.psicologiaAplicada.observations.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.ObservationsEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(com.tfg.proyectolibreria.psicologiaAplicada.config.TestJpaConfig.class)
class ObservationsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ObservationsRepository observationsRepository;

    @Test
    void findByPatientIdIn_shouldReturnObservationsForGivenPatientIds() {
        ObservationsEntity obs1 = new ObservationsEntity(null, "Observation 1", 1L);
        ObservationsEntity obs2 = new ObservationsEntity(null, "Observation 2", 1L);
        ObservationsEntity obs3 = new ObservationsEntity(null, "Observation 3", 2L);
        entityManager.persist(obs1);
        entityManager.persist(obs2);
        entityManager.persist(obs3);
        entityManager.flush();

        List<ObservationsEntity> result = observationsRepository.findByPatientIdIn(List.of(1L));

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(o -> o.getPatientId().equals(1L));
    }

    @Test
    void findByPatientIdIn_shouldReturnEmptyForNonExistingPatient() {
        ObservationsEntity obs = new ObservationsEntity(null, "Observation 1", 1L);
        entityManager.persist(obs);
        entityManager.flush();

        List<ObservationsEntity> result = observationsRepository.findByPatientIdIn(List.of(99L));

        assertThat(result).isEmpty();
    }
}
