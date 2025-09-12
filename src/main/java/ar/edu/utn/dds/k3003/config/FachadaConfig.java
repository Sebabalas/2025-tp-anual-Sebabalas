package ar.edu.utn.dds.k3003.config;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.clients.SolicitudesRestTemplateProxy;
import ar.edu.utn.dds.k3003.repository.PdIRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class FachadaConfig {

    @Bean
    public FachadaProcesadorPdINueva fachadaProcesadorPdI(
        PdIRepository repo,
        @Value("${solicitudes.base-url}") String baseUrl) {

    RestTemplate rt = new RestTemplate();
    Fachada fachada = new Fachada(repo, new SolicitudesRestTemplateProxy(rt, baseUrl));
    return fachada;
}
}