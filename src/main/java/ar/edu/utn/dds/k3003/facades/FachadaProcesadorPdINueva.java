package ar.edu.utn.dds.k3003.facades;

import ar.edu.utn.dds.k3003.dtos.PdiDTONuevo;
import java.util.List;
import java.util.NoSuchElementException;

public interface FachadaProcesadorPdINueva {

    PdiDTONuevo procesar(PdiDTONuevo pdi) throws IllegalStateException;

    PdiDTONuevo buscarPdIPorId(String pdiId) throws NoSuchElementException;

    List<PdiDTONuevo> buscarPorHecho(String hechoId)
            throws NoSuchElementException;

    void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes);

    List<PdiDTONuevo> todosLosPdIs();

    void eliminarTodos();

    void eliminarPorHecho(String hechoId);


}