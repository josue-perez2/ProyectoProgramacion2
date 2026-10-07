package model.pagos;

import model.Pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

public class PagoTarjeta extends Pago {

    private String autorizacion;
    private String tipoTarjeta;
    private String ultimosCuatroDigitos;

    public PagoTarjeta() {
        super();
        this.tipoTarjeta = "VISA";
    }

    public PagoTarjeta(int idPago, int idPedido, LocalDateTime fecha, BigDecimal monto, String estado, String referencia, String autorizacion) {
        super(idPago, idPedido, fecha, monto, estado, referencia);
        this.autorizacion = autorizacion;
        this.tipoTarjeta = "VISA";
    }

    public PagoTarjeta(int idPedido, BigDecimal monto, String autorizacion, String tipoTarjeta, String ultimosCuatroDigitos) {
        super(0, idPedido, LocalDateTime.now(), monto, "P", autorizacion);
        this.autorizacion = autorizacion;
        this.tipoTarjeta = tipoTarjeta != null ? tipoTarjeta : "VISA";
        this.ultimosCuatroDigitos = ultimosCuatroDigitos;
    }

    public String simularAutorizacion() {
        if (autorizacion == null || autorizacion.trim().isEmpty()) {
            Random r = new Random();
            int num = 100000 + r.nextInt(900000);
            this.autorizacion = "AUTH-" + num;
            setReferencia(this.autorizacion);
        }
        return this.autorizacion;
    }

    @Override
    public String getMetodo() {
        return "TC";
    }

    @Override
    public boolean procesar() {
        if (getMonto() == null || getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        simularAutorizacion();
        confirmar();
        return true;
    }

    public String getAutorizacion() {
        return autorizacion;
    }

    public void setAutorizacion(String autorizacion) {
        this.autorizacion = autorizacion;
        if (getReferencia() == null || getReferencia().isEmpty()) {
            setReferencia(autorizacion);
        }
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getUltimosCuatroDigitos() {
        return ultimosCuatroDigitos;
    }

    public void setUltimosCuatroDigitos(String ultimosCuatroDigitos) {
        this.ultimosCuatroDigitos = ultimosCuatroDigitos;
    }

    @Override
    public Pagos aModeloGenerico() {
        Pagos p = new Pagos();
        p.setIdPad(getIdPago());
        p.setIdPedPag(getIdPedido());
        p.setFechaPag(getFecha());
        p.setMetodoPagoPag(getMetodo());
        p.setMontoRecibidoPag(getMonto());
        p.setCambioPag(BigDecimal.ZERO);
        p.setNumeroReferenciaPag(autorizacion != null ? autorizacion : getReferencia());
        p.setEstadoPagoPag(getEstado());
        return p;
    }
}
