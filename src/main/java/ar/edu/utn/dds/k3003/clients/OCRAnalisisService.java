package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.springframework.stereotype.Service;

@Service
public class OCRAnalisisService implements AnalisisService {

    @Override
    public String tipo() {
        return "OCR";
    }

    @Override
    public ResultadoAnalisis ejecutar(PdI pdi) {
        if (pdi.getContenido() == null || !pdi.getContenido().startsWith("http")) {
            return null; // Solo analizamos si hay URL
        }

        // Simulación de OCR
        String textoExtraido = "Texto reconocido en imagen de " + pdi.getContenido();

        return new ResultadoAnalisis(null, tipo(), textoExtraido, pdi);
    }
}