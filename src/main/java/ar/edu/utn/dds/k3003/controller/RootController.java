package ar.edu.utn.dds.k3003.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    private static final String BASE_URL = "https://two025-tp-anual-sebabalas.onrender.com";

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "status", "✅ App corriendo en Render",
                "endpoints", new String[]{
                        "GET  " + BASE_URL + "/api/pdis",
                        "GET  " + BASE_URL + "/api/pdis?hecho={hechoId}",
                        "GET  " + BASE_URL + "/api/pdis/{id}",
                        "POST " + BASE_URL + "/api/pdis"
                }
        );
    }
}
