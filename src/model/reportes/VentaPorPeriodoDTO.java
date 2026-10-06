package model.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VentaPorPeriodoDTO {

    private LocalDate fecha;
    private LocalDateTime fechaHora;
    private int idPedido;
    private String nombreCliente;
    private String metodoPago;
    private BigDecimal totalPagado;
    private int puntos;
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
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