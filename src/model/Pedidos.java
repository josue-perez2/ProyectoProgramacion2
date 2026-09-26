package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedidos {
    private int idPed;
    private int idCliPed;
    private LocalDateTime fechaPed;
    private BigDecimal totalPed;
    private int puntosObtenidosPed;
    private String estadoPed;

    public Pedidos() {
    }

    public Pedidos(int idPed, int idCliPed, LocalDateTime fechaPed, BigDecimal totalPed, int puntosObtenidosPed, String estadoPed) {
        this.idPed = idPed;
        this.idCliPed = idCliPed;
        this.fechaPed = fechaPed;
        this.totalPed = totalPed;
        this.puntosObtenidosPed = puntosObtenidosPed;
        this.estadoPed = estadoPed;
    }

    public int getIdPed() {
        return idPed;
    }

    public void setIdPed(int idPed) {
        this.idPed = idPed;
    }

    public int getIdCliPed() {
        return idCliPed;
    }

    public void setIdCliPed(int idCliPed) {
        this.idCliPed = idCliPed;
    }

    public LocalDateTime getFechaPed() {
        return fechaPed;
    }

    public void setFechaPed(LocalDateTime fechaPed) {
        this.fechaPed = fechaPed;
    }

    public BigDecimal getTotalPed() {
        return totalPed;
    }

    public void setTotalPed(BigDecimal totalPed) {
        this.totalPed = totalPed;
    }

    public int getPuntosObtenidosPed() {
        return puntosObtenidosPed;
    }

    public void setPuntosObtenidosPed(int puntosObtenidosPed) {
        this.puntosObtenidosPed = puntosObtenidosPed;
    }

    public String getEstadoPed() {
        return estadoPed;
    }

    public void setEstadoPed(String estadoPed) {
        this.estadoPed = estadoPed;
    }
}
