package ar.edu.utn.dds.k3003.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Component
public class EtiquetadorClient {
    private static final Logger log = LoggerFactory.getLogger(EtiquetadorClient.class);
    private static final String API_URL = "https://api.apilayer.com/image_labeling/upload";
    private static final String API_KEY = "jREgCTEtuKMUqIkDMzSzqXu5n9ld4C3s";

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> procesarImagen(String imageUrl) {
        try {
            log.info("Enviando imagen a Etiquetador API: {}", imageUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("url", imageUrl);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Respuesta Etiquetador API: {}", response.getBody());
                Object labels = response.getBody().get("labels");
                if (labels instanceof List) {
                    return (List<String>) labels;
                }
            }
        } catch (Exception e) {
            log.error("Error al procesar imagen con Etiquetador API", e);
        }
        return List.of();
    }
}