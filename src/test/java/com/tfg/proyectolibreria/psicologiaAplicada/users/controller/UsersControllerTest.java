package com.tfg.proyectolibreria.psicologiaAplicada.users.controller;

import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UpdateUserRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UserResponse;
import com.tfg.proyectolibreria.psicologiaAplicada.users.enums.UserRole;
import com.tfg.proyectolibreria.psicologiaAplicada.users.service.UsersService;
import com.tfg.proyectolibreria.psicologiaAplicada.web.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UsersController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsersService usersService;

    @Test
    void findAll_shouldReturnUsers() throws Exception {
        UserResponse user1 = new UserResponse(1L, "admin@test.com", "admin", "Admin", "User", UserRole.ADMIN, true, LocalDateTime.now());
        UserResponse user2 = new UserResponse(2L, "psych@test.com", "psych", "Psych", "User", UserRole.PSYCHOLOGIST, true, LocalDateTime.now());

        when(usersService.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$[1].email").value("psych@test.com"));

        verify(usersService).findAll();
    }

    @Test
    void findById_shouldReturnUser() throws Exception {
        UserResponse user = new UserResponse(1L, "admin@test.com", "admin", "Admin", "User", UserRole.ADMIN, true, LocalDateTime.now());

        when(usersService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(usersService).findById(1L);
    }

    @Test
    void findById_shouldReturn404_whenNotFound() throws Exception {
        when(usersService.findById(99L)).thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));

        verify(usersService).findById(99L);
    }

    @Test
    void update_shouldReturnUpdatedUser() throws Exception {
        UserResponse updated = new UserResponse(1L, "new@test.com", "newuser", "New", "Name", UserRole.ADMIN, true, LocalDateTime.now());

        when(usersService.update(eq(1L), any(UpdateUserRequest.class))).thenReturn(updated);

        String body = """
                {
                    "email": "new@test.com",
                    "username": "newuser",
                    "name": "New",
                    "surname": "Name",
                    "role": "ADMIN",
                    "enabled": true
                }
                """;

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(usersService).update(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(usersService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(usersService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: 99")).when(usersService).delete(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));

        verify(usersService).delete(99L);
    }

    @Test
    void resetPassword_shouldReturnOk() throws Exception {
        doNothing().when(usersService).resetPassword(1L);

        mockMvc.perform(post("/users/1/reset-password"))
                .andExpect(status().isOk());

        verify(usersService).resetPassword(1L);
    }

    @Test
    void resetPassword_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: 99")).when(usersService).resetPassword(99L);

        mockMvc.perform(post("/users/99/reset-password"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));

        verify(usersService).resetPassword(99L);
    }
}
