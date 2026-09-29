package dao;

import model.DetallesPedido;

import java.util.List;

public interface DetallesPedidoDao {
    List<DetallesPedido> listar();
    List<DetallesPedido> listarPorPedido(int idPedDet);
    void insertar(DetallesPedido detallesPedido);
    void actualizar(DetallesPedido detallesPedido);
    void eliminar(int id);
    void eliminarPorPedido(int idPedDet);
}
