package dao;

import model.Sandwich;

import java.util.List;

public interface SandwichDao {
    List<Sandwich> listar();
    List<Sandwich> listarActivos();
    Sandwich buscarPorCodigo(String codigo);
    void insertar(Sandwich sandwich);
    void actualizar(Sandwich sandwich);
    void eliminar(int id);
}
