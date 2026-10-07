package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventarioMovimientos {

    private int idImo;
    private int idProImo;
    private LocalDateTime fechaImo;
    private BigDecimal cantidadImo;
    private String tipoMovimientoImo;
    private BigDecimal existenciaAnteriorImo;
    private BigDecimal existenciaNuevaImo;

    public InventarioMovimientos() {
        this.cantidadImo = BigDecimal.ZERO;
        this.existenciaAnteriorImo = BigDecimal.ZERO;
        this.existenciaNuevaImo = BigDecimal.ZERO;
    }

    public InventarioMovimientos(int idImo, int idProImo, LocalDateTime fechaImo, BigDecimal cantidadImo, String tipoMovimientoImo) {
        this.idImo = idImo;
        this.idProImo = idProImo;
        this.fechaImo = fechaImo;
        this.cantidadImo = cantidadImo;
        this.tipoMovimientoImo = tipoMovimientoImo;
        this.existenciaAnteriorImo = BigDecimal.ZERO;
        this.existenciaNuevaImo = BigDecimal.ZERO;
    }

    public InventarioMovimientos(int idImo, int idProImo, LocalDateTime fechaImo, BigDecimal cantidadImo, String tipoMovimientoImo, BigDecimal existenciaAnteriorImo, BigDecimal existenciaNuevaImo) {
        this.idImo = idImo;
        this.idProImo = idProImo;
        this.fechaImo = fechaImo;
        this.cantidadImo = cantidadImo;
        this.tipoMovimientoImo = tipoMovimientoImo;
        this.existenciaAnteriorImo = existenciaAnteriorImo != null ? existenciaAnteriorImo : BigDecimal.ZERO;
        this.existenciaNuevaImo = existenciaNuevaImo != null ? existenciaNuevaImo : BigDecimal.ZERO;
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

    public BigDecimal getExistenciaAnteriorImo() {
        return existenciaAnteriorImo;
    }

    public void setExistenciaAnteriorImo(BigDecimal existenciaAnteriorImo) {
        this.existenciaAnteriorImo = existenciaAnteriorImo;
    }

    public BigDecimal getExistenciaNuevaImo() {
        return existenciaNuevaImo;
    }

    public void setExistenciaNuevaImo(BigDecimal existenciaNuevaImo) {
        this.existenciaNuevaImo = existenciaNuevaImo;
    }

    public BigDecimal getExistenciaAnterior() {
        return existenciaAnteriorImo;
    }

    public void setExistenciaAnterior(BigDecimal existenciaAnterior) {
        this.existenciaAnteriorImo = existenciaAnterior;
    }

    public BigDecimal getExistenciaNueva() {
        return existenciaNuevaImo;
    }

    public void setExistenciaNueva(BigDecimal existenciaNueva) {
        this.existenciaNuevaImo = existenciaNueva;
    }
}
