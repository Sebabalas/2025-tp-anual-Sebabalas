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
        String imageUrl = pdi.getImageUrl();
        if (imageUrl == null || !imageUrl.startsWith("http")) {
            log.warn("[ETIQUETADOR] Saltado PdI id={} por imageUrl inválida: {}", pdi.getId(), imageUrl);
            return null;
        }

        log.info("[ETIQUETADOR] Ejecutando sobre PdI id={} imageUrl={}", pdi.getId(), imageUrl);

        long t0 = System.currentTimeMillis();
        List<String> etiquetas = client.procesarImagen(imageUrl);
        long dt = System.currentTimeMillis() - t0;
        String detalle = String.join(",", etiquetas);

        log.info("[ETIQUETADOR] Finalizado para PdI id={} en {} ms, etiquetas={} (cantidad={})",
                pdi.getId(), dt, detalle, etiquetas.size());
        return new ResultadoAnalisis(tipo(), detalle, pdi);
    }
}