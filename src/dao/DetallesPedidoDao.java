package dao;

import model.DetallesPedido;

import java.util.List;

public interface DetallesPedidoDao {
    List<DetallesPedido> listar();
    void insertar(DetallesPedido cliente);
    void actualizar(DetallesPedido cliente);
    void eliminar(int id);
}
