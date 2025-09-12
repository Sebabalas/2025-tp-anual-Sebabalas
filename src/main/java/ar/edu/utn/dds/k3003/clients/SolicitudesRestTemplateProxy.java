package ar.edu.utn.dds.k3003.clients;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoSolicitudBorradoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
//@Profile("prod")
public class SolicitudesRestTemplateProxy implements FachadaSolicitudes {

    private static final Logger log = LoggerFactory.getLogger(SolicitudesRestTemplateProxy.class);
    private final RestTemplate rt;
    private final String base; // debe terminar en "/"

    public SolicitudesRestTemplateProxy(RestTemplate rt,
                                        @Value("${solicitudes.base-url}") String base) {
        this.rt = rt;
        this.base = base.endsWith("/") ? base : base + "/";
    }

    private String api(String path) {
        return base + (path.startsWith("/") ? path.substring(1) : path);
    }

    @Override
    public SolicitudDTO agregar(SolicitudDTO solicitudDTO) {
        ResponseEntity<SolicitudDTO> resp = rt.postForEntity(
                api("/api/solicitudes"), solicitudDTO, SolicitudDTO.class);
        return resp.getBody();
    }

    @Override
    public SolicitudDTO modificar(String id,
                                  EstadoSolicitudBorradoEnum estado, String motivo) throws NoSuchElementException {

        // Supuesto común: PUT/PATCH a /api/solicitudes/{id}
        var payload = Map.of("estado", estado, "motivo", motivo);
        var entity = new HttpEntity<>(payload, new HttpHeaders() {{
            setContentType(MediaType.APPLICATION_JSON);
        }});

        try {
            ResponseEntity<SolicitudDTO> resp = rt.exchange(
                    api("/api/solicitudes/" + id), HttpMethod.PATCH, entity, SolicitudDTO.class);
            return resp.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("No existe la solicitud " + id);
        }
    }

    @Override
    public List<SolicitudDTO> buscarSolicitudXHecho(String hechoId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(api("/api/solicitudes"))
                .queryParam("hecho", hechoId)
                .build().toUri();

        ResponseEntity<List<SolicitudDTO>> resp = rt.exchange(
                uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<SolicitudDTO>>() {});
        return resp.getBody();
    }

    @Override
    public SolicitudDTO buscarSolicitudXId(String id) {
        try {
            return rt.getForObject(api("/api/solicitudes/{id}"), SolicitudDTO.class, id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException("No existe la solicitud " + id);
        }
    }

    @Override
    public boolean estaActivo(String hechoId) throws NoSuchElementException {
        /*// Opción A: endpoint booleano (si lo tienen)
        try {
            var uri = UriComponentsBuilder.fromHttpUrl(api("/api/solicitudes"))
                    .queryParam("hecho", hechoId).build().toUri();
            ResponseEntity<Boolean> resp = rt.getForEntity(uri, Boolean.class);
            if (resp.getBody() != null) return resp.getBody();
        } catch (HttpClientErrorException.NotFound ignored) {
            // sigue a opción B
        }*/
        // Opción B (fallback): hay solicitudes activas para ese hecho?
        log.info("==> Verificando si hecho {} está activo", hechoId);

    var lista = buscarSolicitudXHecho(hechoId);
    log.info("Solicitudes encontradas: {}", lista);

    if (lista == null || lista.isEmpty()) {
        log.info("No hay solicitudes para el hecho {} => retornando false", hechoId);
        return false; // no hay solicitudes => no está activo
    }

    log.info("Cantidad de solicitudes encontradas: {}", lista.size());
    lista.forEach(s -> log.info("Solicitud id={} estado={}", s.id(), s.estado()));

    boolean tieneAceptadaORechazada = lista.stream().anyMatch(s ->
            s.estado() == EstadoSolicitudBorradoEnum.ACEPTADA ||
            s.estado() == EstadoSolicitudBorradoEnum.RECHAZADA
    );

    log.info("¿Tiene alguna aceptada o rechazada? {}", tieneAceptadaORechazada);

    boolean resultado = !tieneAceptadaORechazada;
    log.info("Resultado final (activo): {}", resultado);

    return resultado;
}

    @Override
    public void setFachadaFuente(FachadaFuente fachadaFuente) {
        // si más adelante necesitás encadenar llamadas, inyectalo aquí
    }
}