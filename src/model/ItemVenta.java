package model;

import java.math.BigDecimal;

public abstract class ItemVenta {

    private int codigo;
    private String nombre;
    private BigDecimal precioUnitario;
    private BigDecimal cantidad;

    public ItemVenta() {
        this.precioUnitario = BigDecimal.ZERO;
        this.cantidad = BigDecimal.ONE;
    }

    public ItemVenta(int codigo, String nombre, BigDecimal precioUnitario, BigDecimal cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
    }

    public BigDecimal calcularSubtotal() {
        if (precioUnitario == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(cantidad);
    }

    public abstract int calcularPuntos();
}
