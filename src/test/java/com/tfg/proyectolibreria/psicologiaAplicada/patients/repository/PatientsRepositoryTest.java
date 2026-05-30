package com.tfg.proyectolibreria.psicologiaAplicada.patients.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(com.tfg.proyectolibreria.psicologiaAplicada.config.TestJpaConfig.class)
class PatientsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientsRepository patientsRepository;

    @Test
    void findActiveInRange_shouldReturnPatientsWithNullEndDate() {
        PatientsEntity activePatient = new PatientsEntity(null, "John", "Doe",
                LocalDate.of(2024, 1, 1), null, LocalDate.of(1990, 1, 1),
                "123456789", Genre.MASCULINO);
        entityManager.persist(activePatient);
        entityManager.flush();

        LocalDate rangeStart = LocalDate.of(2024, 1, 1);
        LocalDate rangeEnd = LocalDate.of(2024, 12, 31);

        var result = patientsRepository.findActiveInRange(rangeStart, rangeEnd, PageRequest.of(0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John");
    }

    @Test
    void findActiveInRange_shouldReturnMultiplePatientsWithNullEndDate() {
        PatientsEntity patient1 = new PatientsEntity(null, "Jane", "Doe",
                LocalDate.of(2024, 3, 1), null,
                LocalDate.of(1992, 5, 10), "987654321", Genre.FEMENINO);
        PatientsEntity patient2 = new PatientsEntity(null, "Alice", "Brown",
                LocalDate.of(2024, 4, 1), null,
                LocalDate.of(1988, 8, 20), "123498765", Genre.FEMENINO);
        entityManager.persist(patient1);
        entityManager.persist(patient2);
        entityManager.flush();

        LocalDate rangeStart = LocalDate.of(2024, 2, 1);
        LocalDate rangeEnd = LocalDate.of(2024, 7, 1);

        var result = patientsRepository.findActiveInRange(rangeStart, rangeEnd, PageRequest.of(0, 20));

        assertThat(result).hasSize(2);
    }

    @Test
    void findActiveInRange_shouldNotReturnDischargedBeforeRange() {
        PatientsEntity patient = new PatientsEntity(null, "Bob", "Smith",
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31),
                LocalDate.of(1985, 3, 15), "555555555", Genre.MASCULINO);
        entityManager.persist(patient);
        entityManager.flush();

        LocalDate rangeStart = LocalDate.of(2024, 1, 1);
        LocalDate rangeEnd = LocalDate.of(2024, 12, 31);

        var result = patientsRepository.findActiveInRange(rangeStart, rangeEnd, PageRequest.of(0, 20));

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveInRange_shouldNotReturnDischargedMidRange() {
        PatientsEntity patient = new PatientsEntity(null, "Charlie", "Brown",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 15),
                LocalDate.of(1990, 7, 20), "111222333", Genre.MASCULINO);
        entityManager.persist(patient);
        entityManager.flush();

        LocalDate rangeStart = LocalDate.of(2024, 1, 1);
        LocalDate rangeEnd = LocalDate.of(2024, 12, 31);

        var result = patientsRepository.findActiveInRange(rangeStart, rangeEnd, PageRequest.of(0, 20));

        assertThat(result).isEmpty();
    }
}
