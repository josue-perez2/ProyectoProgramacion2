package model.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VentaPorPeriodoDTO {
    private LocalDate fecha;
    private BigDecimal totalVenta;
    private BigDecimal totalEfectivo;
    private BigDecimal totalTarjeta;

    public VentaPorPeriodoDTO() {
    }

    public VentaPorPeriodoDTO(LocalDate fecha, BigDecimal totalVenta, BigDecimal totalEfectivo, BigDecimal totalTarjeta) {
        this.fecha = fecha;
        this.totalVenta = totalVenta;
        this.totalEfectivo = totalEfectivo;
        this.totalTarjeta = totalTarjeta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public BigDecimal getTotalEfectivo() {
        return totalEfectivo;
    }

    public void setTotalEfectivo(BigDecimal totalEfectivo) {
        this.totalEfectivo = totalEfectivo;
    }

    public BigDecimal getTotalTarjeta() {
        return totalTarjeta;
    }

    public void setTotalTarjeta(BigDecimal totalTarjeta) {
        this.totalTarjeta = totalTarjeta;
    }
}