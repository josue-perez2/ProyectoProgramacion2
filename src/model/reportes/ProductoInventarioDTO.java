package model.reportes;

import java.math.BigDecimal;

public class ProductoInventarioDTO {
    private int idPro;
    private String codigoPro;
    private String nombrePro;
    private BigDecimal existenciaPro;
    private String estadoInventario;

    public ProductoInventarioDTO() {
    }

    public ProductoInventarioDTO(int idPro, String codigoPro, String nombrePro, BigDecimal existenciaPro, String estadoInventario) {
        this.idPro = idPro;
        this.codigoPro = codigoPro;
        this.nombrePro = nombrePro;
        this.existenciaPro = existenciaPro;
        this.estadoInventario = estadoInventario;
    }

    public int getIdPro() {
        return idPro;
    }

    public void setIdPro(int idPro) {
        this.idPro = idPro;
    }

    public String getCodigoPro() {
        return codigoPro;
    }

    public void setCodigoPro(String codigoPro) {
        this.codigoPro = codigoPro;
    }

    public String getNombrePro() {
        return nombrePro;
    }

    public void setNombrePro(String nombrePro) {
        this.nombrePro = nombrePro;
    }

    public BigDecimal getExistenciaPro() {
        return existenciaPro;
    }

    public void setExistenciaPro(BigDecimal existenciaPro) {
        this.existenciaPro = existenciaPro;
    }

    public String getEstadoInventario() {
        return estadoInventario;
    }

    public void setEstadoInventario(String estadoInventario) {
        this.estadoInventario = estadoInventario;
    }
}