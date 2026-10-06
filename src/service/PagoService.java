package service;

import dao.ClienteDao;
import dao.DetalleMenuDao;
import dao.DetalleSandwichDao;
import dao.DetallesPedidoDao;
import dao.HistorialPuntosDao;
import dao.InventarioMovimientosDao;
import dao.PagosDao;
import dao.PedidosDao;
import dao.ProductosDao;
import dao.Impl.ClienteDaoImpl;
import dao.Impl.DetalleMenuDaoImpl;
import dao.Impl.DetalleSandwichDaoImpl;
import dao.Impl.DetallePedidoDaoImpl;
import dao.Impl.HistorialPuntosDaoImpl;
import dao.Impl.InventarioMovimientosDaoImpl;
import dao.Impl.PagosDaoImpl;
import dao.Impl.PedidosDaoImpl;
import dao.Impl.ProductosDaoImpl;
import model.Cliente;
import model.DetalleMenu;
import model.DetalleSandwich;
import model.DetallesPedido;
import model.HistorialPuntos;
import model.InventarioMovimientos;
import model.Pagos;
import model.Pedidos;
import model.Productos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PagoService {

    private final PagosDao pagosDao;
    private final PedidosDao pedidosDao;
    private final ClienteDao clienteDao;
    private final HistorialPuntosDao historialPuntosDao;
    private final DetallesPedidoDao detallesPedidoDao;
    private final DetalleMenuDao detalleMenuDao;
    private final DetalleSandwichDao detalleSandwichDao;
    private final ProductosDao productosDao;
    private final InventarioMovimientosDao inventarioMovimientosDao;

    public PagoService() {
        this.pagosDao = new PagosDaoImpl();
        this.pedidosDao = new PedidosDaoImpl();
        this.clienteDao = new ClienteDaoImpl();
        this.historialPuntosDao = new HistorialPuntosDaoImpl();
        this.detallesPedidoDao = new DetallePedidoDaoImpl();
        this.detalleMenuDao = new DetalleMenuDaoImpl();
        this.detalleSandwichDao = new DetalleSandwichDaoImpl();
        this.productosDao = new ProductosDaoImpl();
        this.inventarioMovimientosDao = new InventarioMovimientosDaoImpl();
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
        if (pago == null) {
            throw new IllegalStateException("No se puede eliminar: el registro de pago con ID " + id + " no existe.");
        }
        Pedidos pedido = pedidosDao.buscarPorId(pago.getIdPedPag());
        if (pedido != null) {
            restaurarStockPedido(pedido.getIdPed());
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

    public void procesarPago(Pagos pago, Pedidos pedido, Cliente cliente) {
        if (pago == null || pedido == null) {
            throw new IllegalArgumentException("El pago y el pedido son obligatorios.");
        }
        if ("C".equalsIgnoreCase(pedido.getEstadoPed())) {
            throw new IllegalStateException("El pedido #" + pedido.getIdPed() + " ya se encuentra pagado/cobrado.");
        }
        Pagos pagoExistente = buscarPorPedido(pedido.getIdPed());
        if (pagoExistente != null) {
            throw new IllegalStateException("El pedido #" + pedido.getIdPed() + " ya cuenta con un pago registrado (ID: " + pagoExistente.getIdPad() + ").");
        }

        validarStockPedido(pedido.getIdPed());
        descontarStockPedido(pedido.getIdPed());

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

    private void validarStockPedido(int idPedido) {
        List<DetallesPedido> detalles = detallesPedidoDao.listarPorPedido(idPedido);
        for (DetallesPedido d : detalles) {
            String tipo = d.getTipoItemDet();
            int idItem = d.getIdItemDet();
            BigDecimal cant = d.getCantidadDet() != null ? d.getCantidadDet() : BigDecimal.ONE;

            if ("P".equalsIgnoreCase(tipo)) {
                Productos prod = productosDao.buscarPorId(idItem);
                if (prod == null) {
                    throw new IllegalStateException("Producto #" + idItem + " no encontrado.");
                }
                BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
                if (exist.compareTo(cant) < 0) {
                    throw new IllegalStateException("Stock insuficiente para: " + prod.getNombrePro()
                            + " (Disponible: " + exist + ", Requerido: " + cant + ")");
                }
            } else if ("S".equalsIgnoreCase(tipo)) {
                List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(idItem);
                for (DetalleSandwich ing : ingList) {
                    Productos prod = productosDao.buscarPorId(ing.getIdProDet());
                    if (prod != null) {
                        BigDecimal requerido = ing.getCantidadDet().multiply(cant);
                        BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
                        if (exist.compareTo(requerido) < 0) {
                            throw new IllegalStateException("Stock insuficiente del ingrediente: " + prod.getNombrePro()
                                    + " (Disponible: " + exist + ", Requerido: " + requerido + ")");
                        }
                    }
                }
            } else if ("M".equalsIgnoreCase(tipo)) {
                List<DetalleMenu> compList = detalleMenuDao.listarPorMenu(idItem);
                for (DetalleMenu comp : compList) {
                    BigDecimal cantComp = comp.getCantidadDet().multiply(cant);
                    if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                        Productos prod = productosDao.buscarPorId(comp.getIdItemDet());
                        if (prod != null) {
                            BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
                            if (exist.compareTo(cantComp) < 0) {
                                throw new IllegalStateException("Stock insuficiente del producto en combo: " + prod.getNombrePro()
                                        + " (Disponible: " + exist + ", Requerido: " + cantComp + ")");
                            }
                        }
                    } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                        List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(comp.getIdItemDet());
                        for (DetalleSandwich ing : ingList) {
                            Productos prod = productosDao.buscarPorId(ing.getIdProDet());
                            if (prod != null) {
                                BigDecimal requerido = ing.getCantidadDet().multiply(cantComp);
                                BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
                                if (exist.compareTo(requerido) < 0) {
                                    throw new IllegalStateException("Stock insuficiente del ingrediente en combo: " + prod.getNombrePro()
                                            + " (Disponible: " + exist + ", Requerido: " + requerido + ")");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void descontarStockPedido(int idPedido) {
        List<DetallesPedido> detalles = detallesPedidoDao.listarPorPedido(idPedido);
        for (DetallesPedido d : detalles) {
            String tipo = d.getTipoItemDet();
            int idItem = d.getIdItemDet();
            BigDecimal cant = d.getCantidadDet() != null ? d.getCantidadDet() : BigDecimal.ONE;

            if ("P".equalsIgnoreCase(tipo)) {
                aplicarSalidaStock(idItem, cant);
            } else if ("S".equalsIgnoreCase(tipo)) {
                List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(idItem);
                for (DetalleSandwich ing : ingList) {
                    BigDecimal requerido = ing.getCantidadDet().multiply(cant);
                    aplicarSalidaStock(ing.getIdProDet(), requerido);
                }
            } else if ("M".equalsIgnoreCase(tipo)) {
                List<DetalleMenu> compList = detalleMenuDao.listarPorMenu(idItem);
                for (DetalleMenu comp : compList) {
                    BigDecimal cantComp = comp.getCantidadDet().multiply(cant);
                    if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                        aplicarSalidaStock(comp.getIdItemDet(), cantComp);
                    } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                        List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(comp.getIdItemDet());
                        for (DetalleSandwich ing : ingList) {
                            BigDecimal requerido = ing.getCantidadDet().multiply(cantComp);
                            aplicarSalidaStock(ing.getIdProDet(), requerido);
                        }
                    }
                }
            }
        }
    }

    private void restaurarStockPedido(int idPedido) {
        List<DetallesPedido> detalles = detallesPedidoDao.listarPorPedido(idPedido);
        for (DetallesPedido d : detalles) {
            String tipo = d.getTipoItemDet();
            int idItem = d.getIdItemDet();
            BigDecimal cant = d.getCantidadDet() != null ? d.getCantidadDet() : BigDecimal.ONE;

            if ("P".equalsIgnoreCase(tipo)) {
                aplicarEntradaStock(idItem, cant);
            } else if ("S".equalsIgnoreCase(tipo)) {
                List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(idItem);
                for (DetalleSandwich ing : ingList) {
                    BigDecimal requerido = ing.getCantidadDet().multiply(cant);
                    aplicarEntradaStock(ing.getIdProDet(), requerido);
                }
            } else if ("M".equalsIgnoreCase(tipo)) {
                List<DetalleMenu> compList = detalleMenuDao.listarPorMenu(idItem);
                for (DetalleMenu comp : compList) {
                    BigDecimal cantComp = comp.getCantidadDet().multiply(cant);
                    if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                        aplicarEntradaStock(comp.getIdItemDet(), cantComp);
                    } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                        List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(comp.getIdItemDet());
                        for (DetalleSandwich ing : ingList) {
                            BigDecimal requerido = ing.getCantidadDet().multiply(cantComp);
                            aplicarEntradaStock(ing.getIdProDet(), requerido);
                        }
                    }
                }
            }
        }
    }

    private void aplicarSalidaStock(int idProducto, BigDecimal cantidad) {
        Productos prod = productosDao.buscarPorId(idProducto);
        if (prod != null) {
            BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
            BigDecimal nuevoStock = exist.subtract(cantidad);
            if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                nuevoStock = BigDecimal.ZERO;
            }
            prod.setExistenciaPro(nuevoStock);
            productosDao.actualizar(prod);

            InventarioMovimientos mov = new InventarioMovimientos();
            mov.setIdProImo(idProducto);
            mov.setFechaImo(LocalDateTime.now());
            mov.setCantidadImo(cantidad);
            mov.setTipoMovimientoImo("VENTA");
            inventarioMovimientosDao.insertar(mov);
        }
    }

    private void aplicarEntradaStock(int idProducto, BigDecimal cantidad) {
        Productos prod = productosDao.buscarPorId(idProducto);
        if (prod != null) {
            BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
            prod.setExistenciaPro(exist.add(cantidad));
            productosDao.actualizar(prod);

            InventarioMovimientos mov = new InventarioMovimientos();
            mov.setIdProImo(idProducto);
            mov.setFechaImo(LocalDateTime.now());
            mov.setCantidadImo(cantidad);
            mov.setTipoMovimientoImo("REVERSION VENTA");
            inventarioMovimientosDao.insertar(mov);
        }
    }
}
