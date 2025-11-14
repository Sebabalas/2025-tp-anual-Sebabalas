package ar.edi.itn.dds.k3003.model;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.repository.InMemoryPdiRepo;
import ar.edu.utn.dds.k3003.repository.PdIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

public class FachadaTest {

    private Fachada fachada;
    private FachadaSolicitudes fachadaSolicitudes;

   @BeforeEach
    void setUp() {
    PdIRepository pdIRepository = new InMemoryPdiRepo();
    fachadaSolicitudes = Mockito.mock(FachadaSolicitudes.class); // mock
    fachada = new Fachada(pdIRepository); 
}


    @Test
    void testProcesar() {
        // Configurar el mock de fachadaSolicitudes
        Mockito.when(fachadaSolicitudes.estaActivo("hecho1")).thenReturn(true);

        // Crear un PdIDTO para procesar con LocalDateTime
        PdIDTO dto = new PdIDTO("1", "hecho1", "Descripcion", "Lugar",
                LocalDateTime.of(2025, 6, 1, 10, 0), "Contenido", List.of());

        // Llamar al método procesar
        PdIDTO resultado = fachada.procesar(dto);

        // Verificar que el resultado no sea nulo
        assertNotNull(resultado, "El resultado no debe ser nulo");
        // Verificar que se ha procesado correctamente
        assertEquals("hecho1", resultado.hechoId(), "El hechoId debe ser igual");
    }

    @Test
    void testBuscarPdIPorId() {
        // Crear un PdIDTO para devolver con LocalDateTime
        PdIDTO dto = new PdIDTO("1", "hecho1", "Descripcion", "Lugar",
                LocalDateTime.of(2025, 6, 1, 10, 0), "Contenido", List.of());
        Mockito.when(fachadaSolicitudes.estaActivo("hecho1")).thenReturn(true);

        // Procesar el PdI
        fachada.procesar(dto);

        // Verificar que la búsqueda por ID funcione correctamente
        PdIDTO resultado = fachada.buscarPdIPorId("1");

        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals("hecho1", resultado.hechoId(), "El hechoId debe ser igual");
        assertEquals("1", resultado.id(), "El id debe ser igual");
    }

    @Test
    void testBuscarPorHecho() {
        // Crear varios PdIDTOs para devolver con LocalDateTime
        PdIDTO dto1 = new PdIDTO("1", "hecho1", "Descripcion1", "Lugar1",
                LocalDateTime.of(2025, 6, 1, 10, 0), "Contenido1", List.of());
        PdIDTO dto2 = new PdIDTO("2", "hecho1", "Descripcion2", "Lugar2",
                LocalDateTime.of(2025, 6, 2, 11, 0), "Contenido2", List.of());
        Mockito.when(fachadaSolicitudes.estaActivo("hecho1")).thenReturn(true);

        // Procesar los PdIs
        fachada.procesar(dto1);
        fachada.procesar(dto2);

        // Verificar que la búsqueda por hechoId funcione correctamente
        List<PdIDTO> resultado = fachada.buscarPorHecho("hecho1");

        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(2, resultado.size(), "Debe haber dos PdIs para el hecho");
    }

    @Test
    void testBuscarPorHechoNoEncontrado() {
        // Configurar el mock para devolver una lista vacía
        Mockito.when(fachadaSolicitudes.estaActivo("hechoInexistente")).thenReturn(true);

        // Verificar que lanzar una excepción al buscar PdIs de un hecho que no existe
        assertThrows(NoSuchElementException.class, () -> fachada.buscarPorHecho("hechoInexistente"));
    }

    @Test
    void testProcesarConHechoNoActivo() {
        // Configurar el mock para que el hecho no esté activo
        Mockito.when(fachadaSolicitudes.estaActivo("hechoInactivo")).thenReturn(false);

        // Crear un PdIDTO para procesar con LocalDateTime
        PdIDTO dto = new PdIDTO("1", "hechoInactivo", "Descripcion", "Lugar",
                LocalDateTime.of(2025, 6, 1, 10, 0), "Contenido", List.of());

        // Verificar que se lance una excepción al procesar un hecho no activo
        assertThrows(IllegalStateException.class, () -> fachada.procesar(dto));
    }
}