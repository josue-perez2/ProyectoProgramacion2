package dao;

import model.Sandwich;

import java.util.List;

public interface SandwichDao {
    List<Sandwich> listar();
    void insertar(Sandwich sandwich);
    void actualizar(Sandwich sandwich);
    void eliminar(int id);
}
