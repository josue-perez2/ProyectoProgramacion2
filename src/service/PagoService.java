package service;

import dao.ClienteDao;
import dao.HistorialPuntosDao;
import dao.PagosDao;
import dao.PedidosDao;
import dao.Impl.ClienteDaoImpl;
import dao.Impl.HistorialPuntosDaoImpl;
import dao.Impl.PagosDaoImpl;
import dao.Impl.PedidosDaoImpl;
import model.Cliente;
import model.HistorialPuntos;
import model.Pagos;
import model.Pedidos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PagoService {

    private final PagosDao pagosDao;
    private final PedidosDao pedidosDao;
    private final ClienteDao clienteDao;
    private final HistorialPuntosDao historialPuntosDao;

    public PagoService() {
        this.pagosDao = new PagosDaoImpl();
        this.pedidosDao = new PedidosDaoImpl();
        this.clienteDao = new ClienteDaoImpl();
        this.historialPuntosDao = new HistorialPuntosDaoImpl();
    }

    public List<Pagos> listar() {
        return pagosDao.listar();
    }

    public Pagos buscarPorPedido(int idPedPag) {
        return pagosDao.buscarPorPedido(idPedPag);
    }

    public Pagos buscarPorId(int id) {
        return pagosDao.buscarPorId(id);
    }

    public void insertar(Pagos pago) {
        pagosDao.insertar(pago);
    }

    public void actualizar(Pagos pago) {
        pagosDao.actualizar(pago);
    }

    public void eliminar(int id) {
        revertirPago(id);
    }

    public void revertirPago(int id) {
        Pagos pago = pagosDao.buscarPorId(id);
        if (pago != null) {
            Pedidos pedido = pedidosDao.buscarPorId(pago.getIdPedPag());
            if (pedido != null) {
                pedido.setEstadoPed("P");
                pedidosDao.actualizar(pedido);
                if (pedido.getPuntosObtenidosPed() > 0) {
                    Cliente cliente = clienteDao.buscarClientePorId(pedido.getIdCliPed());
                    if (cliente != null && cliente.getSaldoPuntoCli() != null) {
                        BigDecimal puntosRestar = new BigDecimal(pedido.getPuntosObtenidosPed());
                        BigDecimal nuevoSaldo = cliente.getSaldoPuntoCli().subtract(puntosRestar);
                        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                            nuevoSaldo = BigDecimal.ZERO;
                        }
                        cliente.setSaldoPuntoCli(nuevoSaldo);
                        clienteDao.actualizar(cliente);

                        HistorialPuntos his = new HistorialPuntos();
                        his.setIdCliHis(cliente.getIdCli());
                        his.setFechaHis(LocalDateTime.now());
                        his.setTipoOperacionHis("S");
                        his.setPuntosHis(-pedido.getPuntosObtenidosPed());
                        his.setReferenciaHis("Anulacion de Pago Pedido #" + pedido.getIdPed());
                        historialPuntosDao.insertar(his);
                    }
                }
            }
            pagosDao.eliminar(id);
        }
    }

    public void procesarPago(Pagos pago, Pedidos pedido, Cliente cliente) {
        pagosDao.insertar(pago);

        pedido.setEstadoPed("C");
        pedidosDao.actualizar(pedido);

        if (cliente != null && pedido.getPuntosObtenidosPed() > 0) {
            BigDecimal puntosNuevos = new BigDecimal(pedido.getPuntosObtenidosPed());
            cliente.setSaldoPuntoCli(cliente.getSaldoPuntoCli().add(puntosNuevos));
            clienteDao.actualizar(cliente);

            HistorialPuntos his = new HistorialPuntos();
            his.setIdCliHis(cliente.getIdCli());
            his.setFechaHis(LocalDateTime.now());
            his.setTipoOperacionHis("E");
            his.setPuntosHis(pedido.getPuntosObtenidosPed());
            his.setReferenciaHis("Pago Pedido #" + pedido.getIdPed());
            historialPuntosDao.insertar(his);
        }
    }
}
