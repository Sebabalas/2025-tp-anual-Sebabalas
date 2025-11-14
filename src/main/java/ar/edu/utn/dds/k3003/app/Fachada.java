package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.PdI;

import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.app.analisis.ProcesadorAnalisis;
import ar.edu.utn.dds.k3003.app.messaging.PdiPublisher;
import ar.edu.utn.dds.k3003.dtos.PdiDTONuevo;
import ar.edu.utn.dds.k3003.dtos.ResultadoAnalisisDTO;
import ar.edu.utn.dds.k3003.exceptions.dominio.pdi.HechoInactivoException;
import ar.edu.utn.dds.k3003.exceptions.dominio.pdi.HechoInexistenteException;
import ar.edu.utn.dds.k3003.exceptions.solicitudes.SolicitudesCommunicationException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Fachada implements FachadaProcesadorPdINueva {

    private final PdIRepository pdIRepository;
    private FachadaSolicitudes fachadaSolicitudes;
    private final ProcesadorAnalisis procesadorAnalisis;
    private final PdiPublisher pdiPublisher;

    private static final Logger log = LoggerFactory.getLogger(Fachada.class);

    @Autowired
    public Fachada(PdIRepository pdiRepository, FachadaSolicitudes fachadaSolicitudes, ProcesadorAnalisis procesadorAnalisis, PdiPublisher pdiPublisher) {
        this.pdIRepository = pdiRepository;
        this.fachadaSolicitudes = fachadaSolicitudes;
        this.procesadorAnalisis = procesadorAnalisis;
        this.pdiPublisher = pdiPublisher;

    }

    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }

    @Override
    public PdiDTONuevo procesar(PdiDTONuevo dto) {
        log.info("Procesando PdI para hechoId={} descripcion={} lugar={} momento={} imageUrl={}",
                dto.hechoId(), dto.descripcion(), dto.lugar(), dto.momento(), dto.imageUrl());

        // Idempotencia rápida: evitar encolar duplicados exactos ya procesados
        Optional<PdI> yaProcesado =
                pdIRepository.findByHechoId(dto.hechoId()).stream()
                        .filter(p ->
                                equalsSafe(p.getDescripcion(), dto.descripcion()) &&
                                equalsSafe(p.getLugar(), dto.lugar()) &&
                                equalsSafe(p.getMomento(), dto.momento()) &&
                                equalsSafe(p.getContenido(), dto.contenido()))
                        .findFirst();
        if (yaProcesado.isPresent()) {
            log.info("El PdI con hechoId={} ya estaba procesado. Se devuelve el existente con id={}",
                    dto.hechoId(), yaProcesado.get().getId());
            return mapearADTO(yaProcesado.get());
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

        // Encolado asíncrono del DTO completo (worker lo procesa y persiste)
        pdiPublisher.publish(dto);
        log.info("PdI encolado (DTO) para procesamiento async. hechoId={}", dto.hechoId());
        return dto;
    }

    private static boolean equalsSafe(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
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
                pdiDTO.contenido(),
                pdiDTO.imageUrl()
        );
    }

    private PdiDTONuevo mapearADTO(PdI pdi) {
        List<ResultadoAnalisisDTO> resultadosDTO = pdi.getResultados().stream()
                .map(r -> new ResultadoAnalisisDTO(r.getTipo(), r.getDetalle()))
                .toList();

        return new PdiDTONuevo(
                String.valueOf(pdi.getId()),
                pdi.getHechoId(),
                pdi.getDescripcion(),
                pdi.getLugar(),
                pdi.getMomento(),
                pdi.getContenido(),
                pdi.getImageUrl(),
                resultadosDTO
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