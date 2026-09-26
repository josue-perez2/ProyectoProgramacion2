package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventarioMovimientos {
    private int idImo;
    private int idProImo;
    private LocalDateTime fechaImo;
    private BigDecimal cantidadImo;
    private String tipoMovimientoImo;

    public InventarioMovimientos() {
    }

    public InventarioMovimientos(int idImo, int idProImo, LocalDateTime fechaImo, BigDecimal cantidadImo, String tipoMovimientoImo) {
        this.idImo = idImo;
        this.idProImo = idProImo;
        this.fechaImo = fechaImo;
        this.cantidadImo = cantidadImo;
        this.tipoMovimientoImo = tipoMovimientoImo;
    }

    public int getIdImo() {
        return idImo;
    }

    public void setIdImo(int idImo) {
        this.idImo = idImo;
    }

    public int getIdProImo() {
        return idProImo;
    }

    public void setIdProImo(int idProImo) {
        this.idProImo = idProImo;
    }

    public LocalDateTime getFechaImo() {
        return fechaImo;
    }

    public void setFechaImo(LocalDateTime fechaImo) {
        this.fechaImo = fechaImo;
    }

    public BigDecimal getCantidadImo() {
        return cantidadImo;
    }

    public void setCantidadImo(BigDecimal cantidadImo) {
        this.cantidadImo = cantidadImo;
    }

    public String getTipoMovimientoImo() {
        return tipoMovimientoImo;
    }

    public void setTipoMovimientoImo(String tipoMovimientoImo) {
        this.tipoMovimientoImo = tipoMovimientoImo;
    }
}
