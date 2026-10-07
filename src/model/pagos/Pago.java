package model.pagos;

import model.Pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Pago {

    private int idPago;
    private int idPedido;
    private LocalDateTime fecha;
    private BigDecimal monto;
    private String estado;
    private String referencia;

    public Pago() {
        this.fecha = LocalDateTime.now();
        this.monto = BigDecimal.ZERO;
        this.estado = "P";
    }

    public Pago(int idPago, int idPedido, LocalDateTime fecha, BigDecimal monto, String estado, String referencia) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.estado = estado != null ? estado : "P";
        this.referencia = referencia;
    }

    public abstract String getMetodo();

    public abstract boolean procesar();

    public void confirmar() {
        this.estado = "P";
    }

    public void rechazar() {
        this.estado = "A";
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto != null ? monto : BigDecimal.ZERO;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public abstract Pagos aModeloGenerico();

    public static Pago desdeModeloGenerico(Pagos p) {
        if (p == null) {
            return null;
        }
        String met = p.getMetodoPagoPag();
        if ("TC".equalsIgnoreCase(met) || "TF".equalsIgnoreCase(met) || "TARJETA".equalsIgnoreCase(met)) {
            PagoTarjeta pt = new PagoTarjeta(
                    p.getIdPad(),
                    p.getIdPedPag(),
                    p.getFechaPag(),
                    p.getMontoRecibidoPag(),
                    p.getEstadoPagoPag(),
                    p.getNumeroReferenciaPag(),
                    p.getNumeroReferenciaPag()
            );
            return pt;
        } else {
            PagoEfectivo pe = new PagoEfectivo(
                    p.getIdPad(),
                    p.getIdPedPag(),
                    p.getFechaPag(),
                    p.getMontoRecibidoPag() != null && p.getCambioPag() != null ? p.getMontoRecibidoPag().subtract(p.getCambioPag()) : p.getMontoRecibidoPag(),
                    p.getEstadoPagoPag(),
                    p.getNumeroReferenciaPag(),
                    p.getMontoRecibidoPag(),
                    p.getCambioPag()
            );
            return pe;
        }
    }
}
