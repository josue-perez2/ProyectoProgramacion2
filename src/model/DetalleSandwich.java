package model;

import java.math.BigDecimal;

public class DetalleSandwich {
    private int idDetSan;
    private int idSanDet;
    private int idProDet;
    private BigDecimal cantidadDet;
    private String obligatorioDet;

    public DetalleSandwich() {
    }

    public DetalleSandwich(int idDetSan, int idSanDet, int idProDet, BigDecimal cantidadDet, String obligatorioDet) {
        this.idDetSan = idDetSan;
        this.idSanDet = idSanDet;
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

    public int getIdSanDet() {
        return idSanDet;
    }

    public void setIdSanDet(int idSanDet) {
        this.idSanDet = idSanDet;
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
