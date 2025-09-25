package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resultados_analisis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoAnalisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;   // OCR, ETIQUETADOR, etc.
   
    @Column(columnDefinition = "TEXT")
    private String detalle; // JSON, texto, lista serializada

    @ManyToOne
    @JoinColumn(name = "pdi_id")
    private PdI pdi;

    public ResultadoAnalisis(String tipo, String detalle, PdI pdi) {
        this.tipo = tipo;
        this.detalle = detalle;
        this.pdi = pdi;
    }

}