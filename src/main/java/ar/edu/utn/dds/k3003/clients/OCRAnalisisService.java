package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "analisis.ocr.enabled", havingValue = "true")
public class OCRAnalisisService implements AnalisisService {

    private static final Logger log = LoggerFactory.getLogger(OCRAnalisisService.class);
    private final OCRClient client;

    public OCRAnalisisService(OCRClient client) {
        this.client = client;
    }

    @Override
    public String tipo() {
        return "OCR";
    }

    @Override
    public ResultadoAnalisis ejecutar(PdI pdi) {
        String imageUrl = pdi.getImageUrl();
        if (imageUrl == null || !imageUrl.startsWith("http") ||
            !(imageUrl.endsWith(".png") || imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg"))) {
            log.warn("[OCR] Saltado PdI id={} por imageUrl inválida o extensión no permitida: {}", pdi.getId(), imageUrl);
            return null;
        }

        log.info("[OCR] Ejecutando sobre PdI id={} imageUrl={}", pdi.getId(), imageUrl);
        long t0 = System.currentTimeMillis();
        String texto = client.procesarImagen(imageUrl);
        long dt = System.currentTimeMillis() - t0;
        int len = texto == null ? 0 : texto.length();
        String preview = texto == null ? null : (len > 200 ? texto.substring(0, 200) + "..." : texto);
        log.info("[OCR] Finalizado para PdI id={} en {} ms, caracteres={} preview={}", pdi.getId(), dt, len, preview);
        return new ResultadoAnalisis(tipo(), texto, pdi);
    }
}
