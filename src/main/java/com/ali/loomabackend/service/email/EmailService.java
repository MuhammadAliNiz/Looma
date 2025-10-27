package com.ali.loomabackend.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import Slf4j
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j // Add Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    public void sendVerificationEmail(String to, String verificationCode) {
        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);

        String htmlBody;
        try {
            htmlBody = templateEngine.process("verification-email", context);
        } catch (Exception e) {
            log.error("Failed to process email template 'verification-email' for {}: {}", to, e.getMessage(), e);
            // Throw a standard runtime exception
            throw new RuntimeException("Error processing email template", e);
        }


        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("Your Verification Code");
            helper.setText(htmlBody, true); // Set to 'true' to indicate HTML content

            // 4. Send the email
            mailSender.send(mimeMessage);
            log.info("Verification email sent successfully to {}", to);

        } catch (MessagingException e) {
            // Handle exception (e.g., log it)
            log.error("Error sending email to {}: {}", to, e.getMessage(), e);
            // Re-throw as a runtime exception to be handled by the async exception handler
            throw new RuntimeException("Error sending email", e);
        }
    }
}

