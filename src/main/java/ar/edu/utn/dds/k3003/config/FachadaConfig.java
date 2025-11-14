package ar.edu.utn.dds.k3003.config;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.clients.SolicitudesRestTemplateProxy;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.app.analisis.ProcesadorAnalisis;
import ar.edu.utn.dds.k3003.app.messaging.PdiPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class FachadaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FachadaConfig.class);

    @Bean(name = "defaultRestTemplate")
    public RestTemplate defaultRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ProcesadorAnalisis procesadorAnalisis(List<AnalisisService> analizadores) {
        LOGGER.info("Creando ProcesadorAnalisis con los siguientes analizadores:");
        analizadores.forEach(an -> LOGGER.info(" - {}", an.getClass().getName()));
        return new ProcesadorAnalisis(analizadores);
    }

    @Bean
    public FachadaProcesadorPdINueva fachadaProcesadorPdI(
        PdIRepository repo,
        @Value("${solicitudes.base-url}") String baseUrl,
        @Qualifier("defaultRestTemplate") RestTemplate restTemplate,
        ProcesadorAnalisis procesadorAnalisis,
        PdiPublisher pdiPublisher
    ) {
        Fachada fachada = new Fachada(repo, new SolicitudesRestTemplateProxy(restTemplate, baseUrl), procesadorAnalisis, pdiPublisher);
        return fachada;
    }
}
