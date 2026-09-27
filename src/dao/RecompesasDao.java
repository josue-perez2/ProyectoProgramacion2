package dao;

import model.Recompensas;

import java.util.List;

public interface RecompesasDao {
    List<Recompensas> listar();
    void insertar(Recompensas recompensa);
    void actualizar(Recompensas recompensa);
    void eliminar(int id);
}
