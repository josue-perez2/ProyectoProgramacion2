package model;

import java.math.BigDecimal;

public class DetalleMenu {
    private int idDetMen;
    private int idMenDet;
    private String tipoItemDet;
    private int idItemDet;
    private BigDecimal cantidadDet;

    public DetalleMenu() {
    }

    public DetalleMenu(int idDetMen, int idMenDet, String tipoItemDet, int idItemDet, BigDecimal cantidadDet) {
        this.idDetMen = idDetMen;
        this.idMenDet = idMenDet;
        this.tipoItemDet = tipoItemDet;
        this.idItemDet = idItemDet;
        this.cantidadDet = cantidadDet;
    }

    public int getIdDetMen() {
        return idDetMen;
    }

    public void setIdDetMen(int idDetMen) {
        this.idDetMen = idDetMen;
    }

    public int getIdMenDet() {
        return idMenDet;
    }

    public void setIdMenDet(int idMenDet) {
        this.idMenDet = idMenDet;
    }

    public String getTipoItemDet() {
        return tipoItemDet;
    }

    public void setTipoItemDet(String tipoItemDet) {
        this.tipoItemDet = tipoItemDet;
    }

    public int getIdItemDet() {
        return idItemDet;
    }

    public void setIdItemDet(int idItemDet) {
        this.idItemDet = idItemDet;
    }

    public BigDecimal getCantidadDet() {
        return cantidadDet;
    }

    public void setCantidadDet(BigDecimal cantidadDet) {
        this.cantidadDet = cantidadDet;
    }
}
