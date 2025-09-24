package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EtiquetadorAnalisisService implements AnalisisService {

    @Override
    public String tipo() {
        return "ETIQUETADOR";
    }

    @Override
    public ResultadoAnalisis ejecutar(PdI pdi) {
        if (pdi.getContenido() == null || !pdi.getContenido().startsWith("http")) {
            return null; // Solo analizamos si hay URL
        }

        // Simulación de etiquetado
        List<String> etiquetas = List.of("persona", "calle", "auto");
        String detalle = String.join(",", etiquetas);

        return new ResultadoAnalisis(null, tipo(), detalle, pdi);
    }
}