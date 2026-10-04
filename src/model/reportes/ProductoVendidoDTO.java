package model.reportes;

import java.math.BigDecimal;

public class ProductoVendidoDTO {
    private int idItem;
    private String tipoItem;
    private String nombreItem;
    private BigDecimal cantidadVendida;
    private String origen;

    public ProductoVendidoDTO() {
    }

    public ProductoVendidoDTO(int idItem, String tipoItem, String nombreItem, BigDecimal cantidadVendida, String origen) {
        this.idItem = idItem;
        this.tipoItem = tipoItem;
        this.nombreItem = nombreItem;
        this.cantidadVendida = cantidadVendida;
        this.origen = origen;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getNombreItem() {
        return nombreItem;
    }

    public void setNombreItem(String nombreItem) {
        this.nombreItem = nombreItem;
    }

    public BigDecimal getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(BigDecimal cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
}