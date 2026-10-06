package model;

import java.math.BigDecimal;

public class MenuCompleto extends ItemVenta {

    public MenuCompleto() {
        super();
    }

    public MenuCompleto(int codigo, String nombre, BigDecimal precioUnitario, BigDecimal cantidad) {
        super(codigo, nombre, precioUnitario, cantidad);
    }

    @Override
    public int calcularPuntos() {
        return getCantidad() != null ? getCantidad().intValue() * 8 : 0;
    }
}
