package com.tfg.proyectolibreria.psicologiaAplicada.session.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(com.tfg.proyectolibreria.psicologiaAplicada.config.TestJpaConfig.class)
class SessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void save_shouldPersistSession() {
        SessionEntity session = new SessionEntity(null, LocalDateTime.now(), "obs", "summary", false, 1L);
        SessionEntity saved = sessionRepository.save(session);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByPatientId_shouldReturnSessionsForPatient() {
        SessionEntity session1 = new SessionEntity(null, LocalDateTime.now(), "obs1", "summary1", false, 1L);
        SessionEntity session2 = new SessionEntity(null, LocalDateTime.now(), "obs2", "summary2", false, 1L);
        SessionEntity session3 = new SessionEntity(null, LocalDateTime.now(), "obs3", "summary3", false, 2L);
        entityManager.persist(session1);
        entityManager.persist(session2);
        entityManager.persist(session3);
        entityManager.flush();

        List<SessionEntity> result = sessionRepository.findByPatientId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getPatientId().equals(1L));
    }
}
