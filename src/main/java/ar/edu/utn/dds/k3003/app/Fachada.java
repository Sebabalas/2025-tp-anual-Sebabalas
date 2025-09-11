package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.dtos.PdiDTONuevo;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import ar.edu.utn.dds.k3003.exceptions.dominio.pdi.HechoInactivoException;
import ar.edu.utn.dds.k3003.exceptions.dominio.pdi.HechoInexistenteException;
import ar.edu.utn.dds.k3003.exceptions.solicitudes.SolicitudesCommunicationException;

@Service
public class Fachada implements FachadaProcesadorPdINueva {

    private static final Logger log = LoggerFactory.getLogger(Fachada.class);

    private final PdIRepository pdIRepository;
    private FachadaSolicitudes fachadaSolicitudes;

    @Autowired
    public Fachada(PdIRepository pdiRepository) {
        this.pdIRepository = pdiRepository;
    }
    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }
    @Override
    public PdiDTONuevo procesar(PdiDTONuevo dto) throws IllegalStateException {
        log.info("Procesando PdI para hechoId={}", dto.hechoId());

            boolean activo;
    try {
        activo = fachadaSolicitudes.estaActivo(dto.hechoId());
        log.debug("Resultado de la consulta a Solicitudes.estaActivo({}): {}", dto.hechoId(), activo);
    } catch (NoSuchElementException e) {
        log.error("El hecho {} no existe en el sistema de Solicitudes", dto.hechoId(), e);
        throw new HechoInexistenteException("Hecho inexistente: " + dto.hechoId(), e);
    } catch (RestClientException  e) {
        log.error("Error de comunicación con el servicio de Solicitudes al consultar {}", dto.hechoId(), e);
        throw new SolicitudesCommunicationException("Fallo de comuniación con Solicitudes para el hecho: " + dto.hechoId(), e);
    }

    if (!activo) {
        log.warn("Hecho {} inactivo: se interrumpe el procesamiento", dto.hechoId());
        throw new HechoInactivoException("El hecho no se encuentra activo");
    }
    

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
            return mapearADTO(PdIYaProcesado.get());
        } 
        PdI guardado = pdIRepository.save(nuevoPdI);

        log.info("Se guardó el PdI con ID {} en hechoId: {}", guardado.getId(), guardado.getHechoId());

        return mapearADTO(guardado);
    }

    @Override
    public PdiDTONuevo buscarPdIPorId(String idString) throws NoSuchElementException {
        log.info("Buscando PdI por id={}", idString);
        Long id = Long.parseLong(idString);
        PdI pdi =
                pdIRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "No se encontró el PdI con id: " + id));
        PdiDTONuevo pdiDTO = mapearADTO(pdi);
        return pdiDTO;
    }

    @Override
    public List<PdiDTONuevo> buscarPorHecho(String hechoId) throws NoSuchElementException {
        log.info("Buscando PdIs por hechoId={}", hechoId);
        List<PdI> lista = pdIRepository.findByHechoId(hechoId);

        log.info("Encontrados={}", lista.size());

        List<PdiDTONuevo> listaPdiDTO =
                lista.stream().map(this::mapearADTO).collect(Collectors.toList());

        return listaPdiDTO;
    }

    public List<String> etiquetar(String contenido) {
        List<String> etiquetas = new ArrayList<>();

        if (contenido == null || contenido.isBlank()) {
            etiquetas.add("sin clasificar");
            return etiquetas;
        }

        String texto = contenido.toLowerCase();

        // Diccionario de etiquetas con sus sinónimos
        Map<String, List<String>> diccionario = new HashMap<>();
        diccionario.put("incendio", List.of("fuego", "incendio", "quemar", "llamas"));
        diccionario.put("inundación", List.of("agua", "inundación", "anegado", "desborde", "sumergido"));
        diccionario.put("test", List.of("prueba", "probando", "test", "ensayo", "evaluación"));
        diccionario.put("delito", List.of("robo", "asalto", "hurto", "saqueo", "crimen"));
        diccionario.put("clima", List.of("tormenta", "lluvia fuerte", "granizo", "temporal", "viento"));
        diccionario.put("social", List.of("manifestación", "protesta", "marcha", "huelga", "piquete"));

        // Recorremos cada etiqueta y sus sinónimos
        for (Map.Entry<String, List<String>> entrada : diccionario.entrySet()) {
            String etiqueta = entrada.getKey();

            for (String sinonimo : entrada.getValue()) {
                if (texto.contains(sinonimo)) {
                    etiquetas.add(etiqueta);
                    break; // si ya la encontró, no hace falta seguir buscando ese grupo
                }
            }
        }

        if (etiquetas.isEmpty()) {
            etiquetas.add("sin clasificar");
        }

        return etiquetas;
    }

    public PdI dtoAPDI(PdiDTONuevo pdiDTO) {
        PdI nuevoPdI =
                new PdI(
                        pdiDTO.hechoId(),
                        pdiDTO.descripcion(),
                        pdiDTO.lugar(),
                        pdiDTO.momento(),
                        pdiDTO.contenido(),
                        pdiDTO.etiquetas());
        return nuevoPdI;
    }
    private PdiDTONuevo mapearADTO(PdI pdi) {
        return new PdIDTO(
                String.valueOf(pdi.getId()),
                pdi.getHechoId(),
                pdi.getDescripcion(),
                pdi.getLugar(),
                pdi.getMomento(),
                pdi.getContenido(),
                pdi.getEtiquetas()
        );
    }
    @Override
        public List<PdiDTONuevo> todosLosPdIs() {
            return this.pdIRepository.findAll()
                    .stream()
                    .map(this::mapearADTO)
                    .toList();
        }
}
