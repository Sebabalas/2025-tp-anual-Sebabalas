package ar.edu.utn.dds.k3003.dtos;

public class EtiquetaDTO {
    private String label;
    private double confidence;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}