package com.tfg.proyectolibreria.psicologiaAplicada.observations.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.dto.PatientObservationsDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.ObservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ObservationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ObservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObservationService observationService;

    @Test
    void getActivePatientsWithObservations_shouldReturnOk() throws Exception {
        Page<PatientObservationsDTO> emptyPage = new PageImpl<>(List.of());
        when(observationService.getActivePatientsWithObservations(any(), any(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/observations/patients")
                        .param("rangeStart", "2024-01-01")
                        .param("rangeEnd", "2024-12-31")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
