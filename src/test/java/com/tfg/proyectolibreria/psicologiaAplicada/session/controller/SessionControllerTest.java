package com.tfg.proyectolibreria.psicologiaAplicada.session.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionRequestDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.dto.SessionWithPatientDTO;
import com.tfg.proyectolibreria.psicologiaAplicada.session.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SessionController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void save_shouldReturnCreated() throws Exception {
        String body = """
                {
                    "dateSession": "2026-05-25T10:00:00",
                    "dateSessionEnd": "2026-05-25T11:00:00",
                    "observatory": "El paciente presenta ansiedad",
                    "observatorySummary": "Sesión de ansiedad",
                    "idPatient": 1
                }
                """;

        mockMvc.perform(post("/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(sessionService).save(any(SessionRequestDTO.class));
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        String body = """
                {
                    "dateSession": "2026-05-25T10:00:00",
                    "dateSessionEnd": "2026-05-25T11:00:00",
                    "observatory": "Updated",
                    "observatorySummary": "Updated summary",
                    "idPatient": 1
                }
                """;

        mockMvc.perform(put("/session/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(sessionService).update(eq(1L), any(SessionRequestDTO.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/session/1"))
                .andExpect(status().isNoContent());

        verify(sessionService).delete(1L);
    }

    @Test
    void findByDate_shouldReturnSessions() throws Exception {
        SessionWithPatientDTO dto = new SessionWithPatientDTO(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                "obs", "summary", false, 1L, "John", "Doe");

        when(sessionService.findByDate(LocalDate.of(2026, 5, 25))).thenReturn(List.of(dto));

        mockMvc.perform(get("/session")
                        .param("date", "2026-05-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientName").value("John"))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(sessionService).findByDate(LocalDate.of(2026, 5, 25));
    }
}
