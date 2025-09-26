package ar.edu.utn.dds.k3003.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record PdiDTONuevo(
        String id,
        String hechoId,
        String descripcion,
        String lugar,
        LocalDateTime momento,
        String contenido,
        String imageUrl,
        List<ResultadoAnalisisDTO> resultados
) {
    public PdiDTONuevo(String id,String hechoId) {
        this(id,hechoId, null, null, null, null, null, List.of());
    }
}