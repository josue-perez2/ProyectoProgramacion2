package model;

import java.time.LocalDateTime;

public class Canjes {
    private int idCan;
    private int idCliCan;
    private int idRecCan;
    private LocalDateTime fechaCan;
    private int puntosRecompensaCan;

    public Canjes() {
    }

    public Canjes(int idCan, int idCliCan, int idRecCan, LocalDateTime fechaCan, int puntosRecompensaCan) {
        this.idCan = idCan;
        this.idCliCan = idCliCan;
        this.idRecCan = idRecCan;
        this.fechaCan = fechaCan;
        this.puntosRecompensaCan = puntosRecompensaCan;
    }

    public int getIdCan() {
        return idCan;
    }

    public void setIdCan(int idCan) {
        this.idCan = idCan;
    }

    public int getIdCliCan() {
        return idCliCan;
    }

    public void setIdCliCan(int idCliCan) {
        this.idCliCan = idCliCan;
    }

    public int getIdRecCan() {
        return idRecCan;
    }

    public void setIdRecCan(int idRecCan) {
        this.idRecCan = idRecCan;
    }

    public LocalDateTime getFechaCan() {
        return fechaCan;
    }

    public void setFechaCan(LocalDateTime fechaCan) {
        this.fechaCan = fechaCan;
    }

    public int getPuntosRecompensaCan() {
        return puntosRecompensaCan;
    }

    public void setPuntosRecompensaCan(int puntosRecompensaCan) {
        this.puntosRecompensaCan = puntosRecompensaCan;
    }
}
