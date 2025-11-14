package ar.edu.utn.dds.k3003.app.workers;

import ar.edu.utn.dds.k3003.app.analisis.ProcesadorAnalisis;
import ar.edu.utn.dds.k3003.dtos.PdiDTONuevo;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PdiWorker {

    private static final Logger log = LoggerFactory.getLogger(PdiWorker.class);

    private final PdIRepository pdiRepository;
    private final ProcesadorAnalisis procesadorAnalisis;

    public PdiWorker(PdIRepository pdiRepository, ProcesadorAnalisis procesadorAnalisis) {
        this.pdiRepository = pdiRepository;
        this.procesadorAnalisis = procesadorAnalisis;
    }

    @RabbitListener(queues = "${amqp.pdi.queue}")
    public void onMessage(PdiDTONuevo dto) {
        log.info("[WORKER] Recibido PdI DTO hechoId={} desc={} lugar={}", dto.hechoId(), dto.descripcion(), dto.lugar());

        Optional<PdI> existente = pdiRepository.findByHechoId(dto.hechoId()).stream()
                .filter(p ->
                        equalsSafe(p.getDescripcion(), dto.descripcion()) &&
                        equalsSafe(p.getLugar(), dto.lugar()) &&
                        equalsSafe(p.getMomento(), dto.momento()) &&
                        equalsSafe(p.getContenido(), dto.contenido()))
                .findFirst();
        if (existente.isPresent()) {
            log.info("[WORKER] PdI ya existente, se omite. id={}", existente.get().getId());
            return;
        }

        PdI pdi = new PdI(
                dto.hechoId(),
                dto.descripcion(),
                dto.lugar(),
                dto.momento(),
                dto.contenido(),
                dto.imageUrl()
        );

        try {
            procesadorAnalisis.procesarAnalisis(pdi);
            PdI guardado = pdiRepository.save(pdi);
            log.info("[WORKER] Procesado y guardado PdI id={} con {} resultados", guardado.getId(),
                    guardado.getResultados().size());
        } catch (Exception ex) {
            log.error("[WORKER] Error procesando PdI hechoId={}: {}", dto.hechoId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private static boolean equalsSafe(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}

