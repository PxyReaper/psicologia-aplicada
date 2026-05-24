package com.tfg.proyectolibreria.psicologiaAplicada.users.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String from;

    @Override
    public void sendPasswordEmail(String to, String name, String rawPassword) {
        if (from == null || from.isBlank()) {
            log.warn("Mail not configured: spring.mail.username is not set. Skipping email to {}", to);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Bienvenido a Psicología Aplicada - Credenciales de Acceso");

            Context context = new Context();
            context.setVariables(Map.of(
                    "name", name,
                    "email", to,
                    "password", rawPassword
            ));
            String body = templateEngine.process("email/password-email", context);

            helper.setText(body, true);

            mailSender.send(message);
            log.info("Password email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
