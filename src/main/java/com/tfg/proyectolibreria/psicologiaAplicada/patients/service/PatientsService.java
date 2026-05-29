package com.tfg.proyectolibreria.psicologiaAplicada.patients.service;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsResponseDTO;

public interface PatientsService {

    PatientsResponseDTO findById(Long id);

    void save(PatientsRequestDTO requestDTO);

    void update(Long id, PatientsRequestDTO requestDTO);

    void discharge(Long id);
}
