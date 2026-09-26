package dao;

import model.Recompensas;

import java.util.List;

public interface RecompesasDao {
    List<Recompensas> listar();
    void insertar(Recompensas cliente);
    void actualizar(Recompensas cliente);
    void eliminar(int id);
}
