package dao;

import model.Pagos;

import java.util.List;

public interface PagosDao {
    List<Pagos> listar();
    void insertar(Pagos cliente);
    void actualizar(Pagos cliente);
    void eliminar(int id);
}

