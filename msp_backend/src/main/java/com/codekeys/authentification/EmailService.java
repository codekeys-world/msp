package com.codekeys.authentification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${application.mail.from:no-reply@msp.local}") private String from;

    public void envoyerLienReinitialisation(String to, String username, String resetUrl) throws MessagingException {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("resetUrl", resetUrl);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject("Réinitialisation de votre mot de passe MSP");
        helper.setText(templateEngine.process("password-reset", context), true);
        mailSender.send(message);
    }

    public void envoyerCredentialsAdherent(String to, String nom, String matricule, String password) throws MessagingException {
        Context context = new Context();
        context.setVariable("nom", nom);
        context.setVariable("matricule", matricule);
        context.setVariable("password", password);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject("Bienvenue à la MSP — Vos identifiants de connexion");
        helper.setText(templateEngine.process("adherent-welcome", context), true);
        helper.addInline("logo", new ClassPathResource("static/logo_msp.png"), "image/png");
        mailSender.send(message);
    }
}
