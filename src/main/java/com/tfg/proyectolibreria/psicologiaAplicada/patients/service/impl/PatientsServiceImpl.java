package com.tfg.proyectolibreria.psicologiaAplicada.patients.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.PatientsEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.repository.PatientsRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.service.PatientsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
@Service
@RequiredArgsConstructor
public class PatientsServiceImpl implements PatientsService {
    private final PatientsRepository patientsRepository;
    /*  TODO
        Pendiente de preguntar si hay que dividir la observacion  en dos endpoints o aplicar evento de dominio en caso
        de creación directa(lo mas simple sera dividir en dos endpoints y gestiionar en front)

     */
    @Override
    public void save(PatientsRequestDTO requestDTO) {
        PatientsEntity patient =  new PatientsEntity(null,requestDTO.name(),requestDTO.surname(), LocalDate.now(),null,
                requestDTO.birthDay(),requestDTO.cellPhone(),requestDTO.genre());

        patientsRepository.save(patient);

    }
}
