package service;

import dao.DetallesPedidoDao;
import dao.Impl.DetallePedidoDaoImpl;
import model.DetallesPedido;

import java.util.List;

public class DetallePedidoService {

    private final DetallesPedidoDao detallesPedidoDao;

    public DetallePedidoService() {
        this.detallesPedidoDao = new DetallePedidoDaoImpl();
    }

    public List<DetallesPedido> listar() {
        return detallesPedidoDao.listar();
    }

    public List<DetallesPedido> listarPorPedido(int idPedDet) {
        return detallesPedidoDao.listarPorPedido(idPedDet);
    }

    public void insertar(DetallesPedido detallesPedido) {
        detallesPedidoDao.insertar(detallesPedido);
    }

    public void actualizar(DetallesPedido detallesPedido) {
        detallesPedidoDao.actualizar(detallesPedido);
    }

    public void eliminar(int id) {
        detallesPedidoDao.eliminar(id);
    }

    public void eliminarPorPedido(int idPedDet) {
        detallesPedidoDao.eliminarPorPedido(idPedDet);
    }
}
