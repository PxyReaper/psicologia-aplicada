package com.tfg.proyectolibreria.psicologiaAplicada.session.service;

import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionWithPatientDTO;

import java.time.LocalDate;
import java.util.List;

public interface SessionService {
    void save(SessionRequestDTO requestDTO);

    void update(Long id, SessionRequestDTO requestDTO);

    void delete(Long id);

    List<SessionWithPatientDTO> findByDate(LocalDate date);
}
