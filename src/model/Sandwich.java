package model;

import java.math.BigDecimal;

public class Sandwich {
    private int idSan;
    private String codigoSan;
    private String nombreSan;
    private BigDecimal precioSan;
    private String ActivoSan;

    public Sandwich() {
    }

    public Sandwich(int idSan, String codigoSan, String nombreSan, BigDecimal precioSan, String activoSan) {
        this.idSan = idSan;
        this.codigoSan = codigoSan;
        this.nombreSan = nombreSan;
        this.precioSan = precioSan;
        ActivoSan = activoSan;
    }

    public int getIdSan() {
        return idSan;
    }

    public void setIdSan(int idSan) {
        this.idSan = idSan;
    }

    public String getCodigoSan() {
        return codigoSan;
    }

    public void setCodigoSan(String codigoSan) {
        this.codigoSan = codigoSan;
    }

    public String getNombreSan() {
        return nombreSan;
    }

    public void setNombreSan(String nombreSan) {
        this.nombreSan = nombreSan;
    }

    public BigDecimal getPrecioSan() {
        return precioSan;
    }

    public void setPrecioSan(BigDecimal precioSan) {
        this.precioSan = precioSan;
    }

    public String getActivoSan() {
        return ActivoSan;
    }

    public void setActivoSan(String activoSan) {
        ActivoSan = activoSan;
    }
}
