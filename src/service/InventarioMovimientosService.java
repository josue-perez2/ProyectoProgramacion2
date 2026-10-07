package service;

import dao.InventarioMovimientosDao;
import dao.ProductosDao;
import dao.Impl.InventarioMovimientosDaoImpl;
import dao.Impl.ProductosDaoImpl;
import model.InventarioMovimientos;
import model.Productos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InventarioMovimientosService {

    private final InventarioMovimientosDao inventarioMovimientosDao;
    private final ProductosDao productosDao;

    public InventarioMovimientosService() {
        this.inventarioMovimientosDao = new InventarioMovimientosDaoImpl();
        this.productosDao = new ProductosDaoImpl();
    }

    public List<InventarioMovimientos> listar() {
        return inventarioMovimientosDao.listar();
    }

    public void insertar(InventarioMovimientos movimiento) {
        inventarioMovimientosDao.insertar(movimiento);
    }

    public void actualizar(InventarioMovimientos movimiento) {
        inventarioMovimientosDao.actualizar(movimiento);
    }

    public void eliminar(int id) {
        inventarioMovimientosDao.eliminar(id);
    }

    public void abastecer(int idProducto, BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a abastecer debe ser mayor que cero.");
        }
        Productos producto = productosDao.buscarPorId(idProducto);
        if (producto == null) {
            throw new IllegalStateException("El producto con ID " + idProducto + " no existe.");
        }
        if (!"A".equalsIgnoreCase(producto.getActivoPro())) {
            throw new IllegalStateException("No se puede abastecer un producto inactivo.");
        }
        BigDecimal actual = producto.getExistenciaPro() != null ? producto.getExistenciaPro() : BigDecimal.ZERO;
        BigDecimal nueva = actual.add(cantidad);
        producto.setExistenciaPro(nueva);
        productosDao.actualizar(producto);

        InventarioMovimientos mov = new InventarioMovimientos();
        mov.setIdProImo(idProducto);
        mov.setFechaImo(LocalDateTime.now());
        mov.setCantidadImo(cantidad);
        mov.setTipoMovimientoImo("ABASTECIMIENTO");
        mov.setExistenciaAnteriorImo(actual);
        mov.setExistenciaNuevaImo(nueva);
        inventarioMovimientosDao.insertar(mov);
    }

    public void descontarStockVenta(int idProducto, BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que cero.");
        }
        Productos producto = productosDao.buscarPorId(idProducto);
        if (producto == null) {
            throw new IllegalStateException("El producto con ID " + idProducto + " no existe.");
        }
        BigDecimal actual = producto.getExistenciaPro() != null ? producto.getExistenciaPro() : BigDecimal.ZERO;
        if (actual.compareTo(cantidad) < 0) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombrePro()
                    + ". Existencia actual: " + actual + ", requerida: " + cantidad);
        }
        BigDecimal nueva = actual.subtract(cantidad);
        producto.setExistenciaPro(nueva);
        productosDao.actualizar(producto);

        InventarioMovimientos mov = new InventarioMovimientos();
        mov.setIdProImo(idProducto);
        mov.setFechaImo(LocalDateTime.now());
        mov.setCantidadImo(cantidad);
        mov.setTipoMovimientoImo("VENTA");
        mov.setExistenciaAnteriorImo(actual);
        mov.setExistenciaNuevaImo(nueva);
        inventarioMovimientosDao.insertar(mov);
    }

    public void validarStockDisponible(int idProducto, BigDecimal cantidadRequerida) {
        if (cantidadRequerida == null || cantidadRequerida.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Productos producto = productosDao.buscarPorId(idProducto);
        if (producto == null) {
            throw new IllegalStateException("El producto con ID " + idProducto + " no existe.");
        }
        if (!"A".equalsIgnoreCase(producto.getActivoPro())) {
            throw new IllegalStateException("El producto " + producto.getNombrePro() + " se encuentra inactivo.");
        }
        BigDecimal actual = producto.getExistenciaPro() != null ? producto.getExistenciaPro() : BigDecimal.ZERO;
        if (actual.compareTo(cantidadRequerida) < 0) {
            throw new IllegalStateException("Stock insuficiente para: " + producto.getNombrePro()
                    + " (Disponible: " + actual + ", Requerido: " + cantidadRequerida + ")");
        }
    }
}
