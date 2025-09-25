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
        if (pdi.getImageUrl() == null || !pdi.getImageUrl().startsWith("http")) {
            log.warn("PdI sin URL válida: {}", pdi.getId());
            return null;
        }
        String texto = client.procesarImagen(pdi.getImageUrl());
        return new ResultadoAnalisis(tipo(), texto, pdi);
    }
}