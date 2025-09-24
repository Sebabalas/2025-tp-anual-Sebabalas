package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.PdI;

import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.app.analisis.ProcesadorAnalisis;
import ar.edu.utn.dds.k3003.dtos.PdiDTONuevo;
import ar.edu.utn.dds.k3003.exceptions.dominio.pdi.HechoInactivoException;
import ar.edu.utn.dds.k3003.exceptions.dominio.pdi.HechoInexistenteException;
import ar.edu.utn.dds.k3003.exceptions.solicitudes.SolicitudesCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class Fachada implements FachadaProcesadorPdINueva {

    private final PdIRepository pdIRepository;
    private FachadaSolicitudes fachadaSolicitudes;
    private final ProcesadorAnalisis procesadorAnalisis;

    @Autowired
    public Fachada(PdIRepository pdiRepository, FachadaSolicitudes fachadaSolicitudes, OCRService OCRService) {
        this.pdIRepository = pdiRepository;
        this.fachadaSolicitudes = fachadaSolicitudes;
        this.OCRService = OCRService;

    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }

    @Override
    public PdiDTONuevo procesar(PdiDTONuevo dto) {
        log.info("Procesando PdI para hechoId={}", dto.hechoId());

        PdI nuevoPdI = dtoAPDI(dto);

        Optional<PdI> PdIYaProcesado =
                pdIRepository.findByHechoId(nuevoPdI.getHechoId()).stream()
                        .filter(
                                p ->
                                        p.getDescripcion().equals(nuevoPdI.getDescripcion())
                                                && p.getLugar().equals(nuevoPdI.getLugar())
                                                && p.getMomento().equals(nuevoPdI.getMomento())
                                                && p.getContenido().equals(nuevoPdI.getContenido()))
                        .findFirst();

        if (PdIYaProcesado.isPresent()) {
            log.info("El PdI con hechoId={} ya estaba procesado. Se devuelve el existente con id={}",
                    nuevoPdI.getHechoId(),
                    PdIYaProcesado.get().getId());
            return mapearADTO(PdIYaProcesado.get());
        }

        boolean activo;
        try {
            activo = fachadaSolicitudes.estaActivo(dto.hechoId());
            log.debug("Resultado de la consulta a Solicitudes.estaActivo({}): {}", dto.hechoId(), activo);
        } catch (NoSuchElementException e) {
            throw new HechoInexistenteException("Hecho inexistente: " + dto.hechoId(), e);
        } catch (RestClientException e) {
            throw new SolicitudesCommunicationException("Fallo de comunicación con Solicitudes para el hecho: " + dto.hechoId(), e);
        }

        if (!activo) {
            throw new HechoInactivoException("El hecho no se encuentra activo");
        }

         // 🚨 Llamada al Procesador de Análisis (OCR, etiquetas, etc.)
        procesadorAnalisis.procesarAnalisis(nuevoPdI);

        PdI guardado = pdIRepository.save(nuevoPdI);

        log.info("Se guardó el PdI con ID {} en hechoId: {}", guardado.getId(), guardado.getHechoId());

        return mapearADTO(guardado);
    }

    @Override
    public PdiDTONuevo buscarPdIPorId(String idString) {
        Long id = Long.parseLong(idString);
        PdI pdi =
                pdIRepository
                        .findById(id)
                        .orElseThrow(() -> new NoSuchElementException("No se encontró el PdI con id: " + id));
        return mapearADTO(pdi);
    }

    @Override
    public List<PdiDTONuevo> buscarPorHecho(String hechoId) {
        List<PdI> lista = pdIRepository.findByHechoId(hechoId);
        return lista.stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    private PdI dtoAPDI(PdiDTONuevo pdiDTO) {
        return new PdI(
                pdiDTO.hechoId(),
                pdiDTO.descripcion(),
                pdiDTO.lugar(),
                pdiDTO.momento(),
                pdiDTO.contenido()
        );
    }

    private PdiDTONuevo mapearADTO(PdI pdi) {
        List<String> resultados = pdi.getResultados().stream()
                .map(r -> r.getTipo() + ":" + r.getDetalle())
                .toList();

        return new PdiDTONuevo(
                String.valueOf(pdi.getId()),
                pdi.getHechoId(),
                pdi.getDescripcion(),
                pdi.getLugar(),
                pdi.getMomento(),
                pdi.getContenido(),
                resultados
        );
    }

    @Override
    public List<PdiDTONuevo> todosLosPdIs() {
        return pdIRepository.findAll()
                .stream()
                .map(this::mapearADTO)
                .toList();
    }

    @Override
    public void eliminarTodos() {
        pdIRepository.deleteAll();
    }

    @Override
    public void eliminarPorHecho(String hechoId) {
        pdIRepository.deleteByHechoId(hechoId);
    }
}