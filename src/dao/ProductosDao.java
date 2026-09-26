package dao;

import model.Productos;

import java.util.List;

public interface ProductosDao {
    List<Productos> listar();
    void insertar(Productos cliente);
    void actualizar(Productos cliente);
    void eliminar(int id);
}
