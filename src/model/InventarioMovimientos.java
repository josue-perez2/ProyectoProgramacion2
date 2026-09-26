package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventarioMovimientos {
    private int idMon;
    private int idProMon;
    private LocalDateTime fechaMon;
    private BigDecimal cantidadMon;
    private String tipoMovimientoMon;

    public InventarioMovimientos() {
    }

    public InventarioMovimientos(int idMon, int idProMon, LocalDateTime fechaMon, BigDecimal cantidadMon, String tipoMovimientoMon) {
        this.idMon = idMon;
        this.idProMon = idProMon;
        this.fechaMon = fechaMon;
        this.cantidadMon = cantidadMon;
        this.tipoMovimientoMon = tipoMovimientoMon;
    }

    public int getIdMon() {
        return idMon;
    }

    public void setIdMon(int idMon) {
        this.idMon = idMon;
    }

    public int getIdProMon() {
        return idProMon;
    }

    public void setIdProMon(int idProMon) {
        this.idProMon = idProMon;
    }

    public LocalDateTime getFechaMon() {
        return fechaMon;
    }

    public void setFechaMon(LocalDateTime fechaMon) {
        this.fechaMon = fechaMon;
    }

    public BigDecimal getCantidadMon() {
        return cantidadMon;
    }

    public void setCantidadMon(BigDecimal cantidadMon) {
        this.cantidadMon = cantidadMon;
    }

    public String getTipoMovimientoMon() {
        return tipoMovimientoMon;
    }

    public void setTipoMovimientoMon(String tipoMovimientoMon) {
        this.tipoMovimientoMon = tipoMovimientoMon;
    }
}
