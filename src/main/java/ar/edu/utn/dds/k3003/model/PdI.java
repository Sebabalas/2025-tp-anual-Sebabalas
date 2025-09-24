package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pdis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PdI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hechoId;
    private String descripcion;
    private String lugar;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime momento;

    private String contenido;
    private String imageUrl = "";

    private Set<String> tiposDeAnalisisSolicitados = new HashSet<>();


    // resultados de análisis asociados
    @OneToMany(mappedBy = "pdi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ResultadoAnalisis> resultados = new ArrayList<>();

    public void agregarResultado(ResultadoAnalisis resultado) {
        this.resultados.add(resultado);
    }

    public void solicitarAnalisis(String tipo) {
        tiposDeAnalisisSolicitados.add(tipo);
    }

    // constructor sin resultados (se usan después con addResultado)
    public PdI(String hechoId, String descripcion, String lugar, LocalDateTime momento, String contenido, String imageUrl) {
        this.hechoId = hechoId;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.momento = momento;
        this.contenido = contenido;
        this.imageUrl = imageUrl;
    }
}