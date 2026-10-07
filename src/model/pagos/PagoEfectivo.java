package model.pagos;

import model.Pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoEfectivo extends Pago {

    private BigDecimal montoRecibido;
    private BigDecimal cambio;

    public PagoEfectivo() {
        super();
        this.montoRecibido = BigDecimal.ZERO;
        this.cambio = BigDecimal.ZERO;
    }

    public PagoEfectivo(int idPago, int idPedido, LocalDateTime fecha, BigDecimal monto, String estado, String referencia, BigDecimal montoRecibido, BigDecimal cambio) {
        super(idPago, idPedido, fecha, monto, estado, referencia);
        this.montoRecibido = montoRecibido != null ? montoRecibido : BigDecimal.ZERO;
        this.cambio = cambio != null ? cambio : BigDecimal.ZERO;
    }

    public PagoEfectivo(int idPedido, BigDecimal monto, BigDecimal montoRecibido) {
        super(0, idPedido, LocalDateTime.now(), monto, "P", null);
        this.montoRecibido = montoRecibido != null ? montoRecibido : BigDecimal.ZERO;
        this.cambio = calcularCambio();
    }

    public BigDecimal calcularCambio() {
        if (montoRecibido == null || getMonto() == null) {
            this.cambio = BigDecimal.ZERO;
            return BigDecimal.ZERO;
        }
        BigDecimal c = montoRecibido.subtract(getMonto());
        this.cambio = c.compareTo(BigDecimal.ZERO) >= 0 ? c : BigDecimal.ZERO;
        return this.cambio;
    }

    @Override
    public String getMetodo() {
        return "E";
    }

    @Override
    public boolean procesar() {
        if (getMonto() == null || getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (montoRecibido == null || montoRecibido.compareTo(getMonto()) < 0) {
            return false;
        }
        calcularCambio();
        confirmar();
        return true;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido != null ? montoRecibido : BigDecimal.ZERO;
        calcularCambio();
    }

    public BigDecimal getCambio() {
        return cambio;
    }

    public void setCambio(BigDecimal cambio) {
        this.cambio = cambio != null ? cambio : BigDecimal.ZERO;
    }

    @Override
    public Pagos aModeloGenerico() {
        Pagos p = new Pagos();
        p.setIdPad(getIdPago());
        p.setIdPedPag(getIdPedido());
        p.setFechaPag(getFecha());
        p.setMetodoPagoPag(getMetodo());
        p.setMontoRecibidoPag(montoRecibido);
        p.setCambioPag(cambio);
        p.setNumeroReferenciaPag(getReferencia());
        p.setEstadoPagoPag(getEstado());
        return p;
    }
}
