package service;

import dao.PedidosDao;
import dao.Impl.PedidosDaoImpl;
import model.Pedidos;

import java.util.List;

public class PedidoService {

    private final PedidosDao pedidosDao;

    public PedidoService() {
        this.pedidosDao = new PedidosDaoImpl();
    }

    public List<Pedidos> listar() {
        return pedidosDao.listar();
    }

    public List<Pedidos> listarPorCliente(int idCliPed) {
        return pedidosDao.listarPorCliente(idCliPed);
    }

    public Pedidos buscarPorId(int id) {
        return pedidosDao.buscarPorId(id);
    }

    public void insertar(Pedidos pedido) {
        pedidosDao.insertar(pedido);
    }

    public void actualizar(Pedidos pedido) {
        pedidosDao.actualizar(pedido);
    }

    public void eliminar(int id) {
        pedidosDao.eliminar(id);
    }
}
