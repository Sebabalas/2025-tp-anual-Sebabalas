package ar.edu.utn.dds.k3003.controller;


import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdINueva;
import ar.edu.utn.dds.k3003.dtos.PdiDTONuevo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pdis")
public class PdIController {

    private final FachadaProcesadorPdINueva fachadaProcesadorPdI;

    // Constructor
    @Autowired
    public PdIController(FachadaProcesadorPdINueva fachadaProcesadorPdI) {
        this.fachadaProcesadorPdI = fachadaProcesadorPdI;
    }

    // GET /pdis?hecho={hechoId}
    // GET /pdis
    @GetMapping
    public ResponseEntity<List<PdiDTONuevo>> listarPdisPorHecho(
            @RequestParam(required = false) String hecho) {
        if (hecho != null) {
            // GET /pdis?hecho={hechoId}
            return ResponseEntity.ok(fachadaProcesadorPdI.buscarPorHecho(hecho));
        } else {
            // GET /pdis
            // throw new UnsupportedOperationException("Falta agregar pids a FachadaProcesadorPdI");
            return ResponseEntity.ok(fachadaProcesadorPdI.todosLosPdIs());
        }
    }

    // GET /pdis/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PdiDTONuevo> obtenerPdiPorId(@PathVariable Long id) {
        PdiDTONuevo dto = fachadaProcesadorPdI.buscarPdIPorId(String.valueOf(id));
        return ResponseEntity.ok(dto);
    }

    // POST /pdis
    @PostMapping
    public ResponseEntity<PdiDTONuevo> procesarNuevoPdi(@RequestBody PdiDTONuevo pdi) {
        return ResponseEntity.ok(fachadaProcesadorPdI.procesar(pdi));
    }
}