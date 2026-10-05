package model.reportes;

import java.time.LocalDateTime;

public class HistorialCanjeDTO {
    private int idCan;
    private LocalDateTime fechaCan;
    private String nombreCliente;
    private String nombreRecompensa;
    private int puntosUtilizados;

    public HistorialCanjeDTO() {
    }

    public HistorialCanjeDTO(int idCan, LocalDateTime fechaCan, String nombreCliente, String nombreRecompensa, int puntosUtilizados) {
        this.idCan = idCan;
        this.fechaCan = fechaCan;
        this.nombreCliente = nombreCliente;
        this.nombreRecompensa = nombreRecompensa;
        this.puntosUtilizados = puntosUtilizados;
    }

    public int getIdCan() {
        return idCan;
    }

    public void setIdCan(int idCan) {
        this.idCan = idCan;
    }

    public LocalDateTime getFechaCan() {
        return fechaCan;
    }

    public void setFechaCan(LocalDateTime fechaCan) {
        this.fechaCan = fechaCan;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreRecompensa() {
        return nombreRecompensa;
    }

    public void setNombreRecompensa(String nombreRecompensa) {
        this.nombreRecompensa = nombreRecompensa;
    }

    public int getPuntosUtilizados() {
        return puntosUtilizados;
    }

    public void setPuntosUtilizados(int puntosUtilizados) {
        this.puntosUtilizados = puntosUtilizados;
    }
}