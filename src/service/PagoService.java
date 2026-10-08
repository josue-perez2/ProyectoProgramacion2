package service;

import config.Conexion;
import dao.ClienteDao;
import dao.DetalleMenuDao;
import dao.DetalleSandwichDao;
import dao.DetallesPedidoDao;
import dao.HistorialPuntosDao;
import dao.InventarioMovimientosDao;
import dao.MenusDao;
import dao.PagosDao;
import dao.PedidosDao;
import dao.ProductosDao;
import dao.SandwichDao;
import dao.Impl.ClienteDaoImpl;
import dao.Impl.DetalleMenuDaoImpl;
import dao.Impl.DetalleSandwichDaoImpl;
import dao.Impl.DetallePedidoDaoImpl;
import dao.Impl.HistorialPuntosDaoImpl;
import dao.Impl.InventarioMovimientosDaoImpl;
import dao.Impl.MenusDaoImpl;
import dao.Impl.PagosDaoImpl;
import dao.Impl.PedidosDaoImpl;
import dao.Impl.ProductosDaoImpl;
import dao.Impl.SandwichDaoImpl;
import model.Cliente;
import model.DetalleMenu;
import model.DetalleSandwich;
import model.DetallesPedido;
import model.HistorialPuntos;
import model.InventarioMovimientos;
import model.Menus;
import model.Pagos;
import model.pagos.Pago;
import model.Pedidos;
import model.Productos;
import model.Sandwich;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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
    private final SandwichDao sandwichDao;
    private final MenusDao menusDao;

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
        this.sandwichDao = new SandwichDaoImpl();
        this.menusDao = new MenusDaoImpl();
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
        Connection conn = new Conexion().conectar();
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

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
            conn.commit();
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error en reversión de pago: " + ex.getMessage(), ex);
        } finally {
            try {
                conn.setAutoCommit(autoCommitOriginal);
            } catch (SQLException ignored) {
            }
        }
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

        Connection conn = new Conexion().conectar();
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

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

            conn.commit();
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error en transacción de cobro: " + ex.getMessage(), ex);
        } finally {
            try {
                conn.setAutoCommit(autoCommitOriginal);
            } catch (SQLException ignored) {
            }
        }
    }

    public void procesarPago(Pago pago, Pedidos pedido, Cliente cliente) {
        if (pago == null) {
            throw new IllegalArgumentException("El objeto de pago no puede ser nulo.");
        }
        if (!pago.procesar()) {
            throw new IllegalStateException("El procesamiento del pago no fue exitoso.");
        }
        Pagos modeloPagos = pago.aModeloGenerico();
        procesarPago(modeloPagos, pedido, cliente);
        pago.setIdPago(modeloPagos.getIdPad());
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
        String motivo = "VENTA PEDIDO #" + idPedido;
        for (DetallesPedido d : detalles) {
            String tipo = d.getTipoItemDet();
            int idItem = d.getIdItemDet();
            BigDecimal cant = d.getCantidadDet() != null ? d.getCantidadDet() : BigDecimal.ONE;

            if ("P".equalsIgnoreCase(tipo)) {
                aplicarSalidaStock(idItem, cant, motivo);
            } else if ("S".equalsIgnoreCase(tipo)) {
                List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(idItem);
                for (DetalleSandwich ing : ingList) {
                    BigDecimal requerido = ing.getCantidadDet().multiply(cant);
                    aplicarSalidaStock(ing.getIdProDet(), requerido, motivo);
                }
            } else if ("M".equalsIgnoreCase(tipo)) {
                List<DetalleMenu> compList = detalleMenuDao.listarPorMenu(idItem);
                for (DetalleMenu comp : compList) {
                    BigDecimal cantComp = comp.getCantidadDet().multiply(cant);
                    if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                        aplicarSalidaStock(comp.getIdItemDet(), cantComp, motivo);
                    } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                        List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(comp.getIdItemDet());
                        for (DetalleSandwich ing : ingList) {
                            BigDecimal requerido = ing.getCantidadDet().multiply(cantComp);
                            aplicarSalidaStock(ing.getIdProDet(), requerido, motivo);
                        }
                    }
                }
            }
        }
    }

    private void restaurarStockPedido(int idPedido) {
        List<DetallesPedido> detalles = detallesPedidoDao.listarPorPedido(idPedido);
        String motivo = "REVERSIÓN PEDIDO #" + idPedido;
        for (DetallesPedido d : detalles) {
            String tipo = d.getTipoItemDet();
            int idItem = d.getIdItemDet();
            BigDecimal cant = d.getCantidadDet() != null ? d.getCantidadDet() : BigDecimal.ONE;

            if ("P".equalsIgnoreCase(tipo)) {
                aplicarEntradaStock(idItem, cant, motivo);
            } else if ("S".equalsIgnoreCase(tipo)) {
                List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(idItem);
                for (DetalleSandwich ing : ingList) {
                    BigDecimal requerido = ing.getCantidadDet().multiply(cant);
                    aplicarEntradaStock(ing.getIdProDet(), requerido, motivo);
                }
            } else if ("M".equalsIgnoreCase(tipo)) {
                List<DetalleMenu> compList = detalleMenuDao.listarPorMenu(idItem);
                for (DetalleMenu comp : compList) {
                    BigDecimal cantComp = comp.getCantidadDet().multiply(cant);
                    if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                        aplicarEntradaStock(comp.getIdItemDet(), cantComp, motivo);
                    } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                        List<DetalleSandwich> ingList = detalleSandwichDao.listarPorSandwich(comp.getIdItemDet());
                        for (DetalleSandwich ing : ingList) {
                            BigDecimal requerido = ing.getCantidadDet().multiply(cantComp);
                            aplicarEntradaStock(ing.getIdProDet(), requerido, motivo);
                        }
                    }
                }
            }
        }
    }

    private String obtenerNombreSandwich(int idItem) {
        for (Sandwich s : sandwichDao.listar()) {
            if (s.getIdSan() == idItem) {
                return s.getNombreSan();
            }
        }
        return "Sándwich #" + idItem;
    }

    private String obtenerNombreMenu(int idItem) {
        for (Menus m : menusDao.listar()) {
            if (m.getIdMen() == idItem) {
                return m.getNombreMen();
            }
        }
        return "Combo #" + idItem;
    }

    private void aplicarSalidaStock(int idProducto, BigDecimal cantidad, String motivo) {
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
            mov.setTipoMovimientoImo(motivo);
            mov.setExistenciaAnteriorImo(exist);
            mov.setExistenciaNuevaImo(nuevoStock);
            inventarioMovimientosDao.insertar(mov);
        }
    }

    private void aplicarEntradaStock(int idProducto, BigDecimal cantidad, String motivo) {
        Productos prod = productosDao.buscarPorId(idProducto);
        if (prod != null) {
            BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
            BigDecimal nuevoStock = exist.add(cantidad);
            prod.setExistenciaPro(nuevoStock);
            productosDao.actualizar(prod);

            InventarioMovimientos mov = new InventarioMovimientos();
            mov.setIdProImo(idProducto);
            mov.setFechaImo(LocalDateTime.now());
            mov.setCantidadImo(cantidad);
            mov.setTipoMovimientoImo(motivo);
            mov.setExistenciaAnteriorImo(exist);
            mov.setExistenciaNuevaImo(nuevoStock);
            inventarioMovimientosDao.insertar(mov);
        }
    }
}
