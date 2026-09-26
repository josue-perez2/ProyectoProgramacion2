package model;

import java.time.LocalDateTime;

public class HistorialPuntos {
    private int idHis;
    private int idCliHis;
    private LocalDateTime fechaHis;
    private String tipoOperacionHis;
    private int puntosHis;
    private String referenciaHis;

    public HistorialPuntos() {
    }

    public HistorialPuntos(int idHis, int idCliHis, LocalDateTime fechaHis, String tipoOperacionHis, int puntosHis, String referenciaHis) {
        this.idHis = idHis;
        this.idCliHis = idCliHis;
        this.fechaHis = fechaHis;
        this.tipoOperacionHis = tipoOperacionHis;
        this.puntosHis = puntosHis;
        this.referenciaHis = referenciaHis;
    }

    public int getIdHis() {
        return idHis;
    }

    public void setIdHis(int idHis) {
        this.idHis = idHis;
    }

    public int getIdCliHis() {
        return idCliHis;
    }

    public void setIdCliHis(int idCliHis) {
        this.idCliHis = idCliHis;
    }

    public LocalDateTime getFechaHis() {
        return fechaHis;
    }

    public void setFechaHis(LocalDateTime fechaHis) {
        this.fechaHis = fechaHis;
    }

    public String getTipoOperacionHis() {
        return tipoOperacionHis;
    }

    public void setTipoOperacionHis(String tipoOperacionHis) {
        this.tipoOperacionHis = tipoOperacionHis;
    }

    public int getPuntosHis() {
        return puntosHis;
    }

    public void setPuntosHis(int puntosHis) {
        this.puntosHis = puntosHis;
    }

    public String getReferenciaHis() {
        return referenciaHis;
    }

    public void setReferenciaHis(String referenciaHis) {
        this.referenciaHis = referenciaHis;
    }
}
