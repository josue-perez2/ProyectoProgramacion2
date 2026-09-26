package model;

import java.math.BigDecimal;

public class Menus {
    private int idMen;
    private String codigoMen;
    private String nombreMen;
    private BigDecimal precioMen;
    private String activoMen;

    public Menus() {
    }

    public Menus(int idMen, String codigoMen, String nombreMen, BigDecimal precioMen, String activoMen) {
        this.idMen = idMen;
        this.codigoMen = codigoMen;
        this.nombreMen = nombreMen;
        this.precioMen = precioMen;
        this.activoMen = activoMen;
    }

    public int getIdMen() {
        return idMen;
    }

    public void setIdMen(int idMen) {
        this.idMen = idMen;
    }

    public String getCodigoMen() {
        return codigoMen;
    }

    public void setCodigoMen(String codigoMen) {
        this.codigoMen = codigoMen;
    }

    public String getNombreMen() {
        return nombreMen;
    }

    public void setNombreMen(String nombreMen) {
        this.nombreMen = nombreMen;
    }

    public BigDecimal getPrecioMen() {
        return precioMen;
    }

    public void setPrecioMen(BigDecimal precioMen) {
        this.precioMen = precioMen;
    }

    public String getActivoMen() {
        return activoMen;
    }

    public void setActivoMen(String activoMen) {
        this.activoMen = activoMen;
    }
}
