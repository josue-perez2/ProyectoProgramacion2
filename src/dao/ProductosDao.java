package dao;

import java.util.List;

public interface ProductosDao {
    List<ProductosDao> listar();
    void insertar(ProductosDao cliente);
    void actualizar(ProductosDao cliente);
    void eliminar(int id);
}
