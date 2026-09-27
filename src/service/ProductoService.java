package service;

import dao.ProductosDao;
import dao.Impl.ProductosDaoImpl;
import model.Productos;

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

    public void insertar(Productos producto) {
        productosDao.insertar(producto);
    }

    public void actualizar(Productos producto) {
        productosDao.actualizar(producto);
    }

    public void eliminar(int id) {
        productosDao.eliminar(id);
    }
}
