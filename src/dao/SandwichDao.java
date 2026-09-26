package dao;

import model.Sandwich;

import java.util.List;

public interface SandwichDao {
    List<Sandwich> listar();
    void insertar(Sandwich cliente);
    void actualizar(Sandwich cliente);
    void eliminar(int id);
}
