package dao;

import model.Pagos;

import java.util.List;

public interface PagosDao {
    List<Pagos> listar();
    void insertar(Pagos pago);
    void actualizar(Pagos pago);
    void eliminar(int id);
}

