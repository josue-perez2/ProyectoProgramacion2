package dao;

import model.Productos;

import java.util.List;

public interface ProductosDao {
    List<Productos> listar();
    List<Productos> listarActivosPorCategoria(int idCatPro);
    void insertar(Productos producto);
    void actualizar(Productos producto);
    void eliminar(int id);
}
