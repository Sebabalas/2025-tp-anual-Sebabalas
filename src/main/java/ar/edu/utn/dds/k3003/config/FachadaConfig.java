package ar.edu.utn.dds.k3003.config;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.clients.SolicitudesRestTemplateProxy;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import ar.edu.utn.dds.k3003.app.analisis.AnalisisService;
import ar.edu.utn.dds.k3003.app.analisis.ProcesadorAnalisis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class FachadaConfig {

    @Bean(name = "defaultRestTemplate")
    public RestTemplate defaultRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ProcesadorAnalisis procesadorAnalisis(List<AnalisisService> analizadores) {
        return new ProcesadorAnalisis(analizadores);
    }

    @Bean
    public FachadaProcesadorPdINueva fachadaProcesadorPdI(
        PdIRepository repo,
        @Value("${solicitudes.base-url}") String baseUrl,
        @Qualifier("defaultRestTemplate") RestTemplate restTemplate,
        ProcesadorAnalisis procesadorAnalisis
    ) {
        Fachada fachada = new Fachada(repo, new SolicitudesRestTemplateProxy(restTemplate, baseUrl), procesadorAnalisis);
        return fachada;
    }
}
