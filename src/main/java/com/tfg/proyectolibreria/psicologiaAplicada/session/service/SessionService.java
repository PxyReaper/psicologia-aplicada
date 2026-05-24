package com.tfg.proyectolibreria.psicologiaAplicada.session.service;

import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;

public interface SessionService {
    void save(SessionRequestDTO requestDTO);

    void update(Long id, SessionRequestDTO requestDTO);

    void delete(Long id);
}
