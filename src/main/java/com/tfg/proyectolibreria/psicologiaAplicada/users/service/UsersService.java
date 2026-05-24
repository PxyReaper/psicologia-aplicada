package com.tfg.proyectolibreria.psicologiaAplicada.users.service;

import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.UsersEntity;
import org.springframework.modulith.NamedInterface;
import org.springframework.security.core.userdetails.UserDetailsService;

@NamedInterface
public interface UsersService extends UserDetailsService {

    void register(RegisterRequest request);
}
