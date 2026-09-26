package dao;

import model.Pedidos;

import java.util.List;

public interface PedidosDao {
    List<Pedidos> listar();
    void insertar(Pedidos cliente);
    void actualizar(Pedidos cliente);
    void eliminar(int id);
}
