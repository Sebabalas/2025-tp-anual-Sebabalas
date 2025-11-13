package ar.edu.utn.dds.k3003.app.workers;

import ar.edu.utn.dds.k3003.app.analisis.ProcesadorAnalisis;
import ar.edu.utn.dds.k3003.model.EstadoPdi;
import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.persistence.OptimisticLockException;
import java.util.List;

@Service
public class PdiWorker {

    private static final Logger log = LoggerFactory.getLogger(PdiWorker.class);

    private final PdIRepository pdiRepository;
    private final ProcesadorAnalisis procesadorAnalisis;

    @Value("${workers.pdi.enabled:true}")
    private boolean enabled;

    @Value("${workers.pdi.batch-size:5}")
    private int batchSize;

    public PdiWorker(PdIRepository pdiRepository, ProcesadorAnalisis procesadorAnalisis) {
        this.pdiRepository = pdiRepository;
        this.procesadorAnalisis = procesadorAnalisis;
    }

    @Scheduled(fixedDelayString = "${workers.pdi.poll-interval-ms:1000}")
    public void pollAndProcess() {
        if (!enabled) {
            return;
        }

        List<PdI> pendientes = pdiRepository.findByEstadoOrderByIdAsc(EstadoPdi.PENDIENTE, PageRequest.of(0, batchSize));
        if (pendientes.isEmpty()) {
            return;
        }

        for (PdI pdi : pendientes) {
            boolean claimed = tryClaim(pdi);
            if (!claimed) {
                continue;
            }

            try {
                log.info("[WORKER] Procesando PdI id={} hechoId={}", pdi.getId(), pdi.getHechoId());
                procesadorAnalisis.procesarAnalisis(pdi);
                pdi.setEstado(EstadoPdi.COMPLETADO);
                pdiRepository.save(pdi);
                log.info("[WORKER] Completo PdI id={} con {} resultados", pdi.getId(), pdi.getResultados().size());
            } catch (Exception ex) {
                log.error("[WORKER] Fallo procesando PdI id={}: {}", pdi.getId(), ex.getMessage(), ex);
                try {
                    pdi.setEstado(EstadoPdi.FALLIDO);
                    pdiRepository.save(pdi);
                } catch (Exception ignored) {
                    // Si falla el guardado del estado FALLIDO, se logueó arriba
                }
            }
        }
    }

    private boolean tryClaim(PdI pdi) {
        try {
            pdi.setEstado(EstadoPdi.EN_PROCESO);
            pdiRepository.save(pdi);
            return true;
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            // Otro worker lo tomó
            log.debug("[WORKER] PdI id={} ya fue tomado por otro worker", pdi.getId());
            return false;
        }
    }
}

