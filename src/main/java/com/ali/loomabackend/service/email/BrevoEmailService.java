package com.ali.loomabackend.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrevoEmailService {

    private final TemplateEngine templateEngine;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    @Async
    public void sendVerificationEmail(String to, String verificationCode) {
        try {
            // 1️⃣ Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("verificationCode", verificationCode);

            // 2️⃣ Generate HTML from template
            String htmlBody = templateEngine.process("brevo-verification-email", context);

            // 3️⃣ Build Brevo API request
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> sender = Map.of("email", senderEmail);
            Map<String, Object> recipient = Map.of("email", to);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", sender);
            payload.put("to", List.of(recipient));
            payload.put("subject", "Your Verification Code");
            payload.put("htmlContent", htmlBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");
            headers.set("api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // 4️⃣ Send to Brevo API
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Verification email sent successfully to {}", to);
            } else {
                log.error("❌ Failed to send email to {}. Response: {}", to, response.getBody());
            }

        } catch (Exception e) {
            log.error("❌ Error sending email to {}: {}", to, e.getMessage(), e);
        }
    }
}