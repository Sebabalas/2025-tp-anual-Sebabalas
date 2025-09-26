package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.dtos.EtiquetaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EtiquetadorClient {

    private static final Logger log = LoggerFactory.getLogger(EtiquetadorClient.class);
    private final RestTemplate restTemplate;
    private static final String API_URL = "https://api.apilayer.com/image_labeling/url";
    private static final String API_KEY = "jREgCTEtuKMUqIkDMzSzqXu5n9ld4C3s";

    public EtiquetadorClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public List<String> procesarImagen(String imageUrl) {
        try {
            log.info("Enviando imagen a Etiquetador API: {}", imageUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", API_KEY);

            String uri = UriComponentsBuilder.fromHttpUrl(API_URL)
                    .queryParam("url", imageUrl)
                    .toUriString();

            ResponseEntity<EtiquetaDTO[]> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    EtiquetaDTO[].class
            );

            EtiquetaDTO[] body = response.getBody();
            if (body == null || body.length == 0) {
                log.warn("Etiquetador no devolvió resultados para {}", imageUrl);
                return Collections.emptyList();
            }

            // Nos quedamos solo con los labels
            List<String> etiquetas = Arrays.stream(body)
                    .map(EtiquetaDTO::getLabel)
                    .collect(Collectors.toList());

            log.debug("Etiquetas detectadas: {}", etiquetas);
            return etiquetas;

        } catch (Exception e) {
            log.error("Error al procesar imagen con Etiquetador API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}