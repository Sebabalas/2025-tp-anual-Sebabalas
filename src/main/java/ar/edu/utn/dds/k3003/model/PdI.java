package ar.edu.utn.dds.k3003.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPdi estado = EstadoPdi.PENDIENTE;

    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // resultados de análisis asociados
    @OneToMany(mappedBy = "pdi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ResultadoAnalisis> resultados = new ArrayList<>();

    public void agregarResultado(ResultadoAnalisis resultado) {
        this.resultados.add(resultado);
    }
    
    // constructor sin resultados (se usan después con addResultado)
    public PdI(String hechoId, String descripcion, String lugar, LocalDateTime momento, String contenido, String imageUrl) {
        this.hechoId = hechoId;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.momento = momento;
        this.contenido = contenido;
        this.imageUrl = imageUrl;
        this.estado = EstadoPdi.PENDIENTE;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.estado == null) {
            this.estado = EstadoPdi.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}