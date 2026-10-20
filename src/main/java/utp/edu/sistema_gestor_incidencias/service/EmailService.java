package utp.edu.sistema_gestor_incidencias.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY:}")
    private String apiKey;

    @Value("${MAIL_FROM:sistema@ejemplo.com}")
    private String remitenteEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarCorreo(String destinatario, String asunto, String html) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("BREVO_API_KEY no está configurada.");
            return;
        }

        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        // Estructura JSON que espera Brevo
        Map<String, Object> sender = new HashMap<>();
        sender.put("name", "Sistema de Incidencias");
        sender.put("email", remitenteEmail);

        Map<String, Object> to = new HashMap<>();
        to.put("email", destinatario);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", List.of(to));
        body.put("subject", asunto);
        body.put("htmlContent", html);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out
                    .println("Correo enviado a " + destinatario + " vía Brevo. Status: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Error al enviar correo con Brevo: " + e.getMessage());
        }
    }
}