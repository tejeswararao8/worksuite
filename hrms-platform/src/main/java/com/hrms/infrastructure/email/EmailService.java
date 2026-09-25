package com.hrms.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendSimple(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to={} subject={}", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to={} subject={} error={}", to, subject, ex.getMessage());
        }
    }

    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("HTML email sent to={} subject={}", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send HTML email to={} error={}", to, ex.getMessage());
        }
    }

    @Async
    public void sendPasswordReset(String to, String tempPassword) {
        sendSimple(to, "Your HRMS Password Reset",
                "Your temporary password is: " + tempPassword + "\nPlease change it after login.");
    }

    @Async
    public void sendWelcome(String to, String name, String tempPassword) {
        sendHtml(to, "Welcome to HRMS",
                "<h2>Welcome, " + name + "!</h2>" +
                "<p>Your account has been created.</p>" +
                "<p>Temporary password: <strong>" + tempPassword + "</strong></p>" +
                "<p>Please change your password after first login.</p>");
    }
}
