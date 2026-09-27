package dao;

import model.InventarioMovimientos;

import java.util.List;

public interface InventarioMovimientosDao {
    List<InventarioMovimientos> listar();
    void insertar(InventarioMovimientos inventarioMovimiento);
    void actualizar(InventarioMovimientos inventarioMovimiento);
    void eliminar(int id);
}
