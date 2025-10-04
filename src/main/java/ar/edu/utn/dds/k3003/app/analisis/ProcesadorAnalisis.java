package ar.edu.utn.dds.k3003.app.analisis;

import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcesadorAnalisis {

    private final List<AnalisisService> analizadores;
    private static final Logger log = LoggerFactory.getLogger(ProcesadorAnalisis.class);

    @Autowired
    public ProcesadorAnalisis(List<AnalisisService> analizadores) {
        this.analizadores = analizadores;
    }

    public void procesarAnalisis(PdI pdi) {
        log.info("[ANALISIS] Inicio del procesamiento para PdI id={} hechoId={}", pdi.getId(), pdi.getHechoId());
        for (AnalisisService analizador : analizadores) {
            String tipo = analizador.tipo();
            log.info("[ANALISIS:{}] INICIO ejecutar con parametros imageUrl={}, descripcion={}, lugar={}, momento={}",
                    tipo, pdi.getImageUrl(), pdi.getDescripcion(), pdi.getLugar(), pdi.getMomento());

            long t0 = System.currentTimeMillis();
            ResultadoAnalisis resultado = analizador.ejecutar(pdi);
            long dt = System.currentTimeMillis() - t0;

            if (resultado != null) {
                pdi.agregarResultado(resultado);
                log.info("[ANALISIS:{}] FIN exito en {} ms. detalle={} (truncado={})",
                        tipo, dt,
                        resultado.getDetalle() != null && resultado.getDetalle().length() > 200 ? resultado.getDetalle().substring(0, 200) + "..." : resultado.getDetalle(),
                        resultado.getDetalle() != null && resultado.getDetalle().length() > 200);
            } else {
                log.warn("[ANALISIS:{}] FIN sin resultado en {} ms", tipo, dt);
            }
        }
        log.info("[ANALISIS] Fin del procesamiento para PdI id={} con {} resultados", pdi.getId(), pdi.getResultados().size());
    }
}
