package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.springframework.stereotype.Service;

@Service
public class OCRAnalisisService implements AnalisisService {

    private final OCRClient OCRClient;

    public OCRAnalisisService(OCRClient OCRClient) {
        this.OCRClient = OCRClient;
    }

    @Override
    public String tipo() {
        return "OCR";
    }

    @Override
    public ResultadoAnalisis ejecutar(PdI pdi) {
        if (pdi.getImageUrl() == null || !pdi.getImageUrl().startsWith("http")) {
            return null; // Solo analizamos si hay URL
        }

        String textoExtraido = OCRClient.procesarImagen(pdi.getImageUrl());
        return new ResultadoAnalisis(tipo(), textoExtraido, pdi);
    }
}