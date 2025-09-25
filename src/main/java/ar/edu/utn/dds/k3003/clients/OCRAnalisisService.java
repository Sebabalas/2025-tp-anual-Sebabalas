package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OCRAnalisisService implements AnalisisService {

    private static final Logger log = LoggerFactory.getLogger(OCRAnalisisService.class);

    private final OCRClient ocrClient;

    public OCRAnalisisService(OCRClient ocrClient) {
        this.ocrClient = ocrClient;
    }

    @Override
    public String tipo() {
        return "OCR";
    }

    @Override
    public ResultadoAnalisis ejecutar(PdI pdi) {
        if (pdi.getImageUrl() == null || !pdi.getImageUrl().startsWith("http")) {
            log.warn("[OCR][SKIP] PdI id={} no contiene URL válida (imageUrl={})",
                     pdi.getId(), pdi.getImageUrl());
            return null;
        }

        log.info("[OCR][START] Procesando PdI id={} con URL={}", pdi.getId(), pdi.getImageUrl());

        try {
            String textoExtraido = ocrClient.procesarImagen(pdi.getImageUrl());

            if (textoExtraido == null || textoExtraido.isBlank()) {
                log.warn("[OCR][EMPTY] No se extrajo texto para PdI id={} (URL={})",
                         pdi.getId(), pdi.getImageUrl());
            } else {
                log.debug("[OCR][RESULT] PdI id={} → texto extraído={}", pdi.getId(), textoExtraido);
            }

            ResultadoAnalisis resultado = new ResultadoAnalisis(tipo(), textoExtraido, pdi);

            log.info("[OCR][SUCCESS] Resultado agregado para PdI id={}", pdi.getId());
            return resultado;

        } catch (Exception e) {
            log.error("[OCR][ERROR] Falló el análisis para PdI id={} con URL={}. Causa={}",
                      pdi.getId(), pdi.getImageUrl(), e.getMessage(), e);
            return null;
        }
    }
}