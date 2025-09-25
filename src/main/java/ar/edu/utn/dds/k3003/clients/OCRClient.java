package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.dtos.OCRResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OCRClient {

    private static final Logger log = LoggerFactory.getLogger(OCRClient.class);
    private final RestTemplate restTemplate;
    private static final String OCR_API_URL = "https://api.ocr.space/parse/imageurl";
    private static final String API_KEY = "K81939395188957"; // reemplazar por tu key real

    public OCRClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String procesarImagen(String imageUrl) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(OCR_API_URL)
                .queryParam("apikey", API_KEY)
                .queryParam("url", imageUrl)
                .queryParam("filetype", inferirExtension(imageUrl));

        try {
            String rawJson = restTemplate.getForObject(uri.toUriString(), String.class);
            log.debug("Respuesta cruda OCR: {}", rawJson);

            ResponseEntity<OCRResponseDTO> response =
                    restTemplate.getForEntity(uri.toUriString(), OCRResponseDTO.class);

            OCRResponseDTO body = response.getBody();
            if (body == null || body.getIsErroredOnProcessing() != null && body.getIsErroredOnProcessing()) {
                log.warn("OCR falló para URL {}: {}", imageUrl, body != null ? body.getErrorMessage() : "Sin cuerpo");
                return null;
            }

            if (body.getParsedResults() == null || body.getParsedResults().isEmpty()) {
                log.warn("OCR sin resultados para URL: {}", imageUrl);
                return null;
            }

            return body.getParsedResults().get(0).getParsedText();
        } catch (Exception e) {
            log.error("Error al deserializar OCRResponseDTO: {}", e.getMessage(), e);
            return null;
        }
    }

    private String inferirExtension(String url) {
        if (url.endsWith(".png")) return "png";
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) return "jpg";
        return "jpg"; // default
    }
}
