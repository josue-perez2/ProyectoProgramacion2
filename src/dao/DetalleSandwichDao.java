package dao;

import model.DetalleSandwich;

import java.util.List;

public interface DetalleSandwichDao {
    List<DetalleSandwich> listar();
    List<DetalleSandwich> listarPorSandwich(int idSanDet);
    void insertar(DetalleSandwich detalleSandwich);
    void actualizar(DetalleSandwich detalleSandwich);
    void eliminar(int id);
}
