package model;

import java.math.BigDecimal;

public class DetallesPedido {
    private int idDet;
    private int idPedDet;
    private String tipoItemDet;
    private int idItemDet;
    private BigDecimal cantidadDet;
    private BigDecimal precioUnitarioDet;
    private BigDecimal subTotalDet;
    private int puntosGeneradosDet;

    public DetallesPedido() {
    }

    public DetallesPedido(int idDet, int idPedDet, String tipoItemDet, int idItemDet, BigDecimal cantidadDet, BigDecimal precioUnitarioDet, BigDecimal subTotalDet, int puntosGeneradosDet) {
        this.idDet = idDet;
        this.idPedDet = idPedDet;
        this.tipoItemDet = tipoItemDet;
        this.idItemDet = idItemDet;
        this.cantidadDet = cantidadDet;
        this.precioUnitarioDet = precioUnitarioDet;
        this.subTotalDet = subTotalDet;
        this.puntosGeneradosDet = puntosGeneradosDet;
    }

    public int getIdDet() {
        return idDet;
    }

    public void setIdDet(int idDet) {
        this.idDet = idDet;
    }

    public int getIdPedDet() {
        return idPedDet;
    }

    public void setIdPedDet(int idPedDet) {
        this.idPedDet = idPedDet;
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

    public BigDecimal getPrecioUnitarioDet() {
        return precioUnitarioDet;
    }

    public void setPrecioUnitarioDet(BigDecimal precioUnitarioDet) {
        this.precioUnitarioDet = precioUnitarioDet;
    }

    public BigDecimal getSubTotalDet() {
        return subTotalDet;
    }

    public void setSubTotalDet(BigDecimal subTotalDet) {
        this.subTotalDet = subTotalDet;
    }

    public int getPuntosGeneradosDet() {
        return puntosGeneradosDet;
    }

    public void setPuntosGeneradosDet(int puntosGeneradosDet) {
        this.puntosGeneradosDet = puntosGeneradosDet;
    }
}
