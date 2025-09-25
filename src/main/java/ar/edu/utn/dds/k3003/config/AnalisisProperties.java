package ar.edu.utn.dds.k3003.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "analisis")
public class AnalisisProperties {
    private boolean ocrEnabled;
    private boolean etiquetadorEnabled;

    public boolean isOcrEnabled() {
        return ocrEnabled;
    }

    public void setOcrEnabled(boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
    }

    public boolean isEtiquetadorEnabled() {
        return etiquetadorEnabled;
    }

    public void setEtiquetadorEnabled(boolean etiquetadorEnabled) {
        this.etiquetadorEnabled = etiquetadorEnabled;
    }
}