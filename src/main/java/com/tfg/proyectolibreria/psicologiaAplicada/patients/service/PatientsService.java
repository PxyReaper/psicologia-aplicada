package com.tfg.proyectolibreria.psicologiaAplicada.patients.service;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;

public interface PatientsService {

    void save(PatientsRequestDTO requestDTO);

    void update(Long id, PatientsRequestDTO requestDTO);

    void discharge(Long id);
}
