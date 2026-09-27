package dao;

import model.Pedidos;

import java.util.List;

public interface PedidosDao {
    List<Pedidos> listar();
    void insertar(Pedidos pedido);
    void actualizar(Pedidos pedido);
    void eliminar(int id);
}
