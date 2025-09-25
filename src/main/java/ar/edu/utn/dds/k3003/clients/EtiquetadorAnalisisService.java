package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "analisis.etiquetador.enabled", havingValue = "true")
public class EtiquetadorAnalisisService implements AnalisisService {

    private static final Logger log = LoggerFactory.getLogger(EtiquetadorAnalisisService.class);
    private final EtiquetadorClient client;

    public EtiquetadorAnalisisService(EtiquetadorClient client) {
        this.client = client;
    }

    @Override
    public String tipo() {
        return "ETIQUETADOR";
    }

    @Override
    public ResultadoAnalisis ejecutar(PdI pdi) {
        if (pdi.getImageUrl() == null || !pdi.getImageUrl().startsWith("http")) {
            log.warn("No se procesará PdI sin URL válida: {}", pdi.getId());
            return null;
        }

        List<String> etiquetas = client.procesarImagen(pdi.getImageUrl());
        String detalle = String.join(",", etiquetas);

        log.info("Etiquetas detectadas para PdI {}: {}", pdi.getId(), detalle);
        return new ResultadoAnalisis(tipo(), detalle, pdi);
    }
}