package ar.edu.utn.dds.k3003.app.analisis;

import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;

public interface AnalisisService {
    String tipo(); // Identificador del análisis (ej: "OCR", "ETIQUETADOR")
    ResultadoAnalisis ejecutar(PdI pdi);
}