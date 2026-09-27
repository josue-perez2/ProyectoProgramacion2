package dao;

import model.DetallesPedido;

import java.util.List;

public interface DetallesPedidoDao {
    List<DetallesPedido> listar();
    void insertar(DetallesPedido detallesPedido);
    void actualizar(DetallesPedido detallesPedido);
    void eliminar(int id);
}
