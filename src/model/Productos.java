package model;

import java.math.BigDecimal;

public class Productos {
    private int idPro;
    private String codigoPro;
    private int idCatPro;
    private String nombrePro;
    private BigDecimal precioPro;
    private BigDecimal existenciaPro;
    private String activoPro;

    public Productos() {
    }

    public Productos(int idPro, String codigoPro, int idCatPro, String nombrePro, BigDecimal precioPro, BigDecimal existenciaPro, String activoPro) {
        this.idPro = idPro;
        this.codigoPro = codigoPro;
        this.idCatPro = idCatPro;
        this.nombrePro = nombrePro;
        this.precioPro = precioPro;
        this.existenciaPro = existenciaPro;
        this.activoPro = activoPro;
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

    public int getIdCatPro() {
        return idCatPro;
    }

    public void setIdCatPro(int idCatPro) {
        this.idCatPro = idCatPro;
    }

    public String getNombrePro() {
        return nombrePro;
    }

    public void setNombrePro(String nombrePro) {
        this.nombrePro = nombrePro;
    }

    public BigDecimal getPrecioPro() {
        return precioPro;
    }

    public void setPrecioPro(BigDecimal precioPro) {
        this.precioPro = precioPro;
    }

    public BigDecimal getExistenciaPro() {
        return existenciaPro;
    }

    public void setExistenciaPro(BigDecimal existenciaPro) {
        this.existenciaPro = existenciaPro;
    }

    public String getActivoPro() {
        return activoPro;
    }

    public void setActivoPro(String activoPro) {
        this.activoPro = activoPro;
    }
}
