package com.tfg.proyectolibreria.psicologiaAplicada.users.listener;

import com.tfg.proyectolibreria.psicologiaAplicada.users.email.EmailService;
import com.tfg.proyectolibreria.psicologiaAplicada.users.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        log.info("User created: {}. Sending credentials email...", event.email());
        emailService.sendPasswordEmail(event.email(), event.name(), event.rawPassword());
    }
}
