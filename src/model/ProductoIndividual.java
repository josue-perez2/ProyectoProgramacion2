package model;

import java.math.BigDecimal;

public class ProductoIndividual extends ItemVenta {

    private String tipoProducto;

    public ProductoIndividual() {
        super();
        this.tipoProducto = "P";
    }

    public ProductoIndividual(int codigo, String nombre, BigDecimal precioUnitario, BigDecimal cantidad, String tipoProducto) {
        super(codigo, nombre, precioUnitario, cantidad);
        this.tipoProducto = tipoProducto != null ? tipoProducto : "P";
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    @Override
    public int calcularPuntos() {
        if ("S".equalsIgnoreCase(tipoProducto)) {
            return getCantidad() != null ? getCantidad().intValue() * 2 : 0;
        }
        return 0;
    }
}
