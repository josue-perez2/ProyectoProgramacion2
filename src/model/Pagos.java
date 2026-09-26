package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pagos {
    private int idPad;
    private int idPedPag;
    private LocalDateTime fechaPag;
    private String metodoPagoPag;
    private BigDecimal montoRecibidoPag;
    private BigDecimal cambioPag;
    private String numeroReferenciaPag;
    private String estadoPagoPag;

    public Pagos() {
    }

    public Pagos(int idPad, int idPedPag, LocalDateTime fechaPag, String metodoPagoPag, BigDecimal montoRecibidoPag, BigDecimal cambioPag, String numeroReferenciaPag, String estadoPagoPag) {
        this.idPad = idPad;
        this.idPedPag = idPedPag;
        this.fechaPag = fechaPag;
        this.metodoPagoPag = metodoPagoPag;
        this.montoRecibidoPag = montoRecibidoPag;
        this.cambioPag = cambioPag;
        this.numeroReferenciaPag = numeroReferenciaPag;
        this.estadoPagoPag = estadoPagoPag;
    }

    public int getIdPad() {
        return idPad;
    }

    public void setIdPad(int idPad) {
        this.idPad = idPad;
    }

    public int getIdPedPag() {
        return idPedPag;
    }

    public void setIdPedPag(int idPedPag) {
        this.idPedPag = idPedPag;
    }

    public LocalDateTime getFechaPag() {
        return fechaPag;
    }

    public void setFechaPag(LocalDateTime fechaPag) {
        this.fechaPag = fechaPag;
    }

    public String getMetodoPagoPag() {
        return metodoPagoPag;
    }

    public void setMetodoPagoPag(String metodoPagoPag) {
        this.metodoPagoPag = metodoPagoPag;
    }

    public BigDecimal getMontoRecibidoPag() {
        return montoRecibidoPag;
    }

    public void setMontoRecibidoPag(BigDecimal montoRecibidoPag) {
        this.montoRecibidoPag = montoRecibidoPag;
    }

    public BigDecimal getCambioPag() {
        return cambioPag;
    }

    public void setCambioPag(BigDecimal cambioPag) {
        this.cambioPag = cambioPag;
    }

    public String getNumeroReferenciaPag() {
        return numeroReferenciaPag;
    }

    public void setNumeroReferenciaPag(String numeroReferenciaPag) {
        this.numeroReferenciaPag = numeroReferenciaPag;
    }

    public String getEstadoPagoPag() {
        return estadoPagoPag;
    }

    public void setEstadoPagoPag(String estadoPagoPag) {
        this.estadoPagoPag = estadoPagoPag;
    }
}
