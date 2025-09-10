package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class Fachada implements FachadaProcesadorPdINueva {

    private static final Logger log = LoggerFactory.getLogger(Fachada.class);

    private final PdIRepository pdIRepository;
    private FachadaSolicitudes fachadaSolicitudes;
    private final AtomicLong generadorId = new AtomicLong(1);

    @Autowired
    public Fachada(PdIRepository pdiRepository) {
        this.pdIRepository = pdiRepository;
    }
    @Override
    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }
    @Override
    public PdIDTO procesar(PdIDTO dto) throws IllegalStateException {
        log.info("Procesando PdI para hechoId={}", dto.hechoId());

        /*if (!fachadaSolicitudes.estaActivo(dto.hechoId())) {
            log.warn("El hecho no está activo: {}", dto.hechoId());
            throw new IllegalStateException("El hecho no está activo");
        }
            */

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

        nuevoPdI.setId(generadorId.getAndIncrement()); 
        pdIRepository.save(nuevoPdI);

        System.out.println(
                "Se guardó el PdI con ID "
                        + nuevoPdI.getId()
                        + " en hechoId: "
                        + nuevoPdI.getHechoId());

        PdIDTO pdiDTOAEnviar = mapearADTO(nuevoPdI);
        return pdiDTOAEnviar;
    }

    @Override
    public PdIDTO buscarPdIPorId(String idString) throws NoSuchElementException {
        log.info("Buscando PdI por id={}", idString);
        Long id = Long.parseLong(idString);
        PdI pdi =
                pdIRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "No se encontró el PdI con id: " + id));
        PdIDTO pdiDTO = mapearADTO(pdi);
        return pdiDTO;
    }

    @Override
    public List<PdIDTO> buscarPorHecho(String hechoId) throws NoSuchElementException {
        log.info("Buscando PdIs por hechoId={}", hechoId);
        List<PdI> lista = pdIRepository.findByHechoId(hechoId);

        log.info("Encontrados={}", lista.size());

        List<PdIDTO> listaPdiDTO =
                lista.stream().map(this::mapearADTO).collect(Collectors.toList());

        return listaPdiDTO;
    }

    public PdI dtoAPDI(PdIDTO pdiDTO) {
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
    private PdIDTO mapearADTO(PdI pdi) {
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
        public List<PdIDTO> todosLosPdIs() {
            return this.pdIRepository.findAll()
                    .stream()
                    .map(this::mapearADTO)
                    .toList();
        }
}
