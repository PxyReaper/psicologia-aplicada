package com.tfg.proyectolibreria.psicologiaAplicada.patients.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.dto.PatientsResponseDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.enums.Genre;
import com.tfg.proyectolibreria.psicologiaAplicada.patients.service.PatientsService;
import com.tfg.proyectolibreria.psicologiaAplicada.web.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    void findById_shouldReturnPatient() throws Exception {
        PatientsResponseDTO dto = new PatientsResponseDTO(1L, "Juan", "Pérez", LocalDate.of(2026, 1, 1), null, LocalDate.of(1990, 5, 15), "612345678", Genre.MASCULINO, List.of("obs1", "obs2"));

        when(patientsService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.surname").value("Pérez"))
                .andExpect(jsonPath("$.genre").value("masculino"))
                .andExpect(jsonPath("$.observations[0]").value("obs1"))
                .andExpect(jsonPath("$.observations[1]").value("obs2"));

        verify(patientsService).findById(1L);
    }

    @Test
    void findById_shouldReturn404_whenNotFound() throws Exception {
        when(patientsService.findById(99L)).thenThrow(new ResourceNotFoundException("Patient not found with id: 99"));

        mockMvc.perform(get("/patients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found with id: 99"));

        verify(patientsService).findById(99L);
    }
}
