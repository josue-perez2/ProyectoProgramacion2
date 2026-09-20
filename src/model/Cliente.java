package model;

import java.math.BigDecimal;

public class Cliente {
    private int idCli;
    private String dpiCli;
    private String nombreCli;
    private String telefonoCli;
    private String correoCli;
    private String direccionCli;
    private BigDecimal saldoPuntoCli;
    private String estadoCli;

    public Cliente() {
    }

    public Cliente(int idCli, String dpiCli, String nombreCli, String telefonoCli, String correoCli, String direccionCli, BigDecimal saldoPuntoCli, String estadoCli) {
        this.idCli = idCli;
        this.dpiCli = dpiCli;
        this.nombreCli = nombreCli;
        this.telefonoCli = telefonoCli;
        this.correoCli = correoCli;
        this.direccionCli = direccionCli;
        this.saldoPuntoCli = saldoPuntoCli;
        this.estadoCli = estadoCli;
    }

    public int getIdCli() {
        return idCli;
    }

    public void setIdCli(int idCli) {
        this.idCli = idCli;
    }

    public String getDpiCli() {
        return dpiCli;
    }

    public void setDpiCli(String dpiCli) {
        this.dpiCli = dpiCli;
    }

    public String getNombreCli() {
        return nombreCli;
    }

    public void setNombreCli(String nombreCli) {
        this.nombreCli = nombreCli;
    }

    public String getTelefonoCli() {
        return telefonoCli;
    }

    public void setTelefonoCli(String telefonoCli) {
        this.telefonoCli = telefonoCli;
    }

    public String getCorreoCli() {
        return correoCli;
    }

    public void setCorreoCli(String correoCli) {
        this.correoCli = correoCli;
    }

    public String getDireccionCli() {
        return direccionCli;
    }

    public void setDireccionCli(String direccionCli) {
        this.direccionCli = direccionCli;
    }

    public BigDecimal getSaldoPuntoCli() {
        return saldoPuntoCli;
    }

    public void setSaldoPuntoCli(BigDecimal saldoPuntoCli) {
        this.saldoPuntoCli = saldoPuntoCli;
    }

    public String getEstadoCli() {
        return estadoCli;
    }

    public void setEstadoCli(String estadoCli) {
        this.estadoCli = estadoCli;
    }
}
