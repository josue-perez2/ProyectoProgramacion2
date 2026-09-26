package dao;

import model.InventarioMovimientos;

import java.util.List;

public interface InventarioMovimientosDao {
    List<InventarioMovimientos> listar();
    void insertar(InventarioMovimientos cliente);
    void actualizar(InventarioMovimientos cliente);
    void eliminar(int id);
}
