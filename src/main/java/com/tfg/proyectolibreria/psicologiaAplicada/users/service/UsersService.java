package com.tfg.proyectolibreria.psicologiaAplicada.users.service;

import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UpdateUserRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.UserResponse;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@NamedInterface
public interface UsersService extends UserDetailsService {

    void register(RegisterRequest request);

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);

    void resetPassword(Long id);

    void incrementTokenVersion(String email);
}
