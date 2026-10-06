package dao;

import model.Productos;

import java.util.List;

public interface ProductosDao {
    List<Productos> listar();
    List<Productos> listarActivosPorCategoria(int idCatPro);
    Productos buscarPorId(int id);
    void insertar(Productos producto);
    void actualizar(Productos producto);
    void eliminar(int id);
}
