package com.tfg.proyectolibreria.psicologiaAplicada.patients.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.service.PatientsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PatientsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PatientsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientsService patientsService;

    @Test
    void save_shouldReturnCreated() throws Exception {
        String body = """
                {
                    "name": "Juan",
                    "surname": "Pérez",
                    "birthDay": "1990-05-15",
                    "cellPhone": "612345678",
                    "genre": "masculino",
                    "observation": "Ansiedad generalizada"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(patientsService).save(any(PatientsRequestDTO.class));
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        String body = """
                {
                    "name": "Juan",
                    "surname": "Pérez",
                    "birthDay": "1990-05-15",
                    "cellPhone": "612345678",
                    "genre": "masculino"
                }
                """;

        mockMvc.perform(put("/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(patientsService).update(eq(1L), any(PatientsRequestDTO.class));
    }

    @Test
    void discharge_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/patients/1/discharge"))
                .andExpect(status().isOk());

        verify(patientsService).discharge(1L);
    }
}
