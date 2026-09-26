package model;

import java.math.BigDecimal;

public class DetalleSandwich {
    private int idDetSan;
    private int idSamDet;
    private int idProDet;
    private BigDecimal cantidadDet;
    private String obligatorioDet;

    public DetalleSandwich() {
    }

    public DetalleSandwich(int idDetSan, int idSamDet, int idProDet, BigDecimal cantidadDet, String obligatorioDet) {
        this.idDetSan = idDetSan;
        this.idSamDet = idSamDet;
        this.idProDet = idProDet;
        this.cantidadDet = cantidadDet;
        this.obligatorioDet = obligatorioDet;
    }

    public int getIdDetSan() {
        return idDetSan;
    }

    public void setIdDetSan(int idDetSan) {
        this.idDetSan = idDetSan;
    }

    public int getIdSamDet() {
        return idSamDet;
    }

    public void setIdSamDet(int idSamDet) {
        this.idSamDet = idSamDet;
    }

    public int getIdProDet() {
        return idProDet;
    }

    public void setIdProDet(int idProDet) {
        this.idProDet = idProDet;
    }

    public BigDecimal getCantidadDet() {
        return cantidadDet;
    }

    public void setCantidadDet(BigDecimal cantidadDet) {
        this.cantidadDet = cantidadDet;
    }

    public String getObligatorioDet() {
        return obligatorioDet;
    }

    public void setObligatorioDet(String obligatorioDet) {
        this.obligatorioDet = obligatorioDet;
    }
}
