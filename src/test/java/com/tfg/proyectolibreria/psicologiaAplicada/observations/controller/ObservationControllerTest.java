package com.tfg.proyectolibreria.psicologiaAplicada.observations.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.observations.service.ObservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ObservationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ObservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObservationService observationService;

    @Test
    void getActivePatientsWithObservations_shouldReturnOk() throws Exception {
        when(observationService.getActivePatientsWithObservations(any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/observations/patients")
                        .param("rangeStart", "2024-01-01")
                        .param("rangeEnd", "2024-12-31"))
                .andExpect(status().isOk());
    }
}
