package com.tfg.proyectolibreria.psicologiaAplicada.users.service.impl;

import com.tfg.proyectolibreria.psicologiaAplicada.users.dto.RegisterRequest;
import com.tfg.proyectolibreria.psicologiaAplicada.users.UserDetailsImpl;
import com.tfg.proyectolibreria.psicologiaAplicada.users.UsersEntity;
import com.tfg.proyectolibreria.psicologiaAplicada.users.event.UserCreatedEvent;
import com.tfg.proyectolibreria.psicologiaAplicada.users.repository.UsersRepository;
import com.tfg.proyectolibreria.psicologiaAplicada.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String DIGITS = "0123456789";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + DIGITS;
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsersEntity entity = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new UserDetailsImpl(entity);
    }

    @Override
    public UsersEntity register(RegisterRequest request) {
        String rawPassword = generatePassword();
        UsersEntity entity = new UsersEntity(
                null,
                request.email(),
                request.username(),
                passwordEncoder.encode(rawPassword),
                request.name(),
                request.surname(),
                request.role(),
                true,
                null
        );
        UsersEntity saved = usersRepository.save(entity);
        eventPublisher.publishEvent(new UserCreatedEvent(
                saved.getEmail(), saved.getName(), saved.getSurname(), rawPassword
        ));
        return saved;
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
