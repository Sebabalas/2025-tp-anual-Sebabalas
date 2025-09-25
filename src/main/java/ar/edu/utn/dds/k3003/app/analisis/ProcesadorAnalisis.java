package ar.edu.utn.dds.k3003.app.analisis;

import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcesadorAnalisis {

    private final List<AnalisisService> analizadores;

    @Autowired
    public ProcesadorAnalisis(List<AnalisisService> analizadores) {
        this.analizadores = analizadores;
    }

    public void procesarAnalisis(PdI pdi) {
        for (AnalisisService analizador : analizadores) {
                ResultadoAnalisis resultado = analizador.ejecutar(pdi);
                if (resultado != null) {
                    pdi.agregarResultado(resultado);
                }
        
        }
    }
}
