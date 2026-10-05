package model.reportes;

import java.math.BigDecimal;

public class MenuVendidoDTO {
    private int idMen;
    private String codigoMen;
    private String nombreMen;
    private BigDecimal cantidadVendida;
    private BigDecimal totalGenerado;

    public MenuVendidoDTO() {
    }

    public MenuVendidoDTO(int idMen, String codigoMen, String nombreMen, BigDecimal cantidadVendida, BigDecimal totalGenerado) {
        this.idMen = idMen;
        this.codigoMen = codigoMen;
        this.nombreMen = nombreMen;
        this.cantidadVendida = cantidadVendida;
        this.totalGenerado = totalGenerado;
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

    public BigDecimal getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(BigDecimal cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getTotalGenerado() {
        return totalGenerado;
    }

    public void setTotalGenerado(BigDecimal totalGenerado) {
        this.totalGenerado = totalGenerado;
    }
}