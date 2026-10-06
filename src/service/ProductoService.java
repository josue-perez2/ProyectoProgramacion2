package service;

import dao.ProductosDao;
import dao.Impl.ProductosDaoImpl;
import model.Productos;

import java.math.BigDecimal;
import java.util.List;

public class ProductoService {

    private final ProductosDao productosDao;

    public ProductoService() {
        this.productosDao = new ProductosDaoImpl();
    }

    public List<Productos> listar() {
        return productosDao.listar();
    }

    public List<Productos> listarActivosPorCategoria(int idCatPro) {
        return productosDao.listarActivosPorCategoria(idCatPro);
    }

    public Productos buscarPorId(int id) {
        return productosDao.buscarPorId(id);
    }

    public void insertar(Productos producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (producto.getPrecioPro() == null || producto.getPrecioPro().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del producto no puede ser negativo.");
        }
        if (producto.getExistenciaPro() == null || producto.getExistenciaPro().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La existencia del producto no puede ser negativa.");
        }
        String codigo = producto.getCodigoPro() != null ? producto.getCodigoPro().trim() : "";
        for (Productos p : listar()) {
            if (p.getCodigoPro() != null && p.getCodigoPro().equalsIgnoreCase(codigo)) {
                throw new IllegalStateException("Ya existe un producto con el código " + codigo + ".");
            }
        }
        productosDao.insertar(producto);
    }

    public void actualizar(Productos producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (producto.getPrecioPro() == null || producto.getPrecioPro().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del producto no puede ser negativo.");
        }
        if (producto.getExistenciaPro() == null || producto.getExistenciaPro().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La existencia del producto no puede ser negativa.");
        }
        boolean existe = false;
        String codigo = producto.getCodigoPro() != null ? producto.getCodigoPro().trim() : "";
        for (Productos p : listar()) {
            if (p.getIdPro() == producto.getIdPro()) {
                existe = true;
            } else if (p.getCodigoPro() != null && p.getCodigoPro().equalsIgnoreCase(codigo)) {
                throw new IllegalStateException("El código " + codigo + " ya está en uso por otro producto.");
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede modificar: el producto con ID " + producto.getIdPro() + " no existe.");
        }
        productosDao.actualizar(producto);
    }

    public void eliminar(int id) {
        boolean existe = false;
        for (Productos p : listar()) {
            if (p.getIdPro() == id) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede eliminar: el producto con ID " + id + " no existe.");
        }
        productosDao.eliminar(id);
    }
}
