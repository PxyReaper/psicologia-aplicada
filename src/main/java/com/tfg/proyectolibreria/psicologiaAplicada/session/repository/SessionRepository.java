package com.tfg.proyectolibreria.psicologiaAplicada.session.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.session.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity,Long> {
    List<SessionEntity> findByPatientId(Long patientId);
}
