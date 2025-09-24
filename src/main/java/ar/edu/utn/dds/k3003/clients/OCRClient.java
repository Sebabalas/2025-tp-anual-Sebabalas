package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.dtos.OCRResponseDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OCRClient {

    private final RestTemplate restTemplate;
    private static final String OCR_API_URL = "https://api.ocr.space/parse/imageurl";
    private static final String API_KEY = "K81939395188957"; // cambiar por tu key real

    public OCRClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String procesarImagen(String imageUrl) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(OCR_API_URL)
                .queryParam("apikey", API_KEY)
                .queryParam("url", imageUrl);

        ResponseEntity<OCRResponseDTO> response =
                restTemplate.getForEntity(uri.toUriString(), OCRResponseDTO.class);

        OCRResponseDTO body = response.getBody();
        if (body == null || body.getParsedResults() == null || body.getParsedResults().isEmpty()) {
            return null;
        }

        return body.getParsedResults().get(0).getParsedText();
    }
}