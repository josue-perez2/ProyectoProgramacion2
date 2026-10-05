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
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser nulo.");
        }
        if (pedido.getIdCliPed() <= 0) {
            throw new IllegalArgumentException("El pedido debe tener un cliente válido asignado.");
        }
        pedidosDao.insertar(pedido);
    }

    public void actualizar(Pedidos pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser nulo.");
        }
        Pedidos existente = buscarPorId(pedido.getIdPed());
        if (existente == null) {
            throw new IllegalStateException("No se puede modificar: el pedido con ID " + pedido.getIdPed() + " no existe.");
        }
        pedidosDao.actualizar(pedido);
    }

    public void eliminar(int id) {
        Pedidos existente = buscarPorId(id);
        if (existente == null) {
            throw new IllegalStateException("No se puede eliminar: el pedido con ID " + id + " no existe.");
        }
        pedidosDao.eliminar(id);
    }
}
