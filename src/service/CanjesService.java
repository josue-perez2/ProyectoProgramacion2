package service;

import config.Conexion;
import dao.CanjesDao;
import dao.ClienteDao;
import dao.DetalleMenuDao;
import dao.DetalleSandwichDao;
import dao.HistorialPuntosDao;
import dao.InventarioMovimientosDao;
import dao.ProductosDao;
import dao.Impl.CanjesDaoImpl;
import dao.Impl.ClienteDaoImpl;
import dao.Impl.DetalleMenuDaoImpl;
import dao.Impl.DetalleSandwichDaoImpl;
import dao.Impl.HistorialPuntosDaoImpl;
import dao.Impl.InventarioMovimientosDaoImpl;
import dao.Impl.ProductosDaoImpl;
import model.Canjes;
import model.Cliente;
import model.DetalleMenu;
import model.DetalleSandwich;
import model.HistorialPuntos;
import model.InventarioMovimientos;
import model.Productos;
import model.Recompensas;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanjesService {

    public static class InfoDisponibilidad {
        private final boolean disponible;
        private final String texto;

        public InfoDisponibilidad(boolean disponible, String texto) {
            this.disponible = disponible;
            this.texto = texto;
        }

        public boolean isDisponible() {
            return disponible;
        }

        public String getTexto() {
            return texto;
        }
    }

    private final CanjesDao canjesDao;
    private final ClienteDao clienteDao;
    private final HistorialPuntosDao historialPuntosDao;
    private final ProductosDao productosDao;
    private final InventarioMovimientosDao inventarioMovimientosDao;
    private final DetalleSandwichDao detalleSandwichDao;
    private final DetalleMenuDao detalleMenuDao;

    public CanjesService() {
        this.canjesDao = new CanjesDaoImpl();
        this.clienteDao = new ClienteDaoImpl();
        this.historialPuntosDao = new HistorialPuntosDaoImpl();
        this.productosDao = new ProductosDaoImpl();
        this.inventarioMovimientosDao = new InventarioMovimientosDaoImpl();
        this.detalleSandwichDao = new DetalleSandwichDaoImpl();
        this.detalleMenuDao = new DetalleMenuDaoImpl();
    }

    public List<Canjes> listar() {
        return canjesDao.listar();
    }

    public void insertar(Canjes canje) {
        canjesDao.insertar(canje);
    }

    public void actualizar(Canjes canje) {
        canjesDao.actualizar(canje);
    }

    public void eliminar(int id) {
        canjesDao.eliminar(id);
    }

    public Canjes buscarPorId(int id) {
        for (Canjes c : canjesDao.listar()) {
            if (c.getIdCan() == id) {
                return c;
            }
        }
        return null;
    }

    public Map<Integer, Productos> obtenerMapaProductos() {
        Map<Integer, Productos> mapa = new HashMap<>();
        try {
            for (Productos p : productosDao.listar()) {
                mapa.put(p.getIdPro(), p);
            }
        } catch (Exception ignored) {
        }
        return mapa;
    }

    public Canjes procesarCanje(Cliente cliente, Recompensas recompensa) {
        if (cliente == null || recompensa == null) {
            throw new IllegalArgumentException("Cliente y recompensa son obligatorios");
        }
        if (!"A".equalsIgnoreCase(cliente.getEstadoCli())) {
            throw new IllegalStateException("El cliente se encuentra inactivo");
        }
        if (!"A".equalsIgnoreCase(recompensa.getActivoRec())) {
            throw new IllegalStateException("La recompensa seleccionada no está activa");
        }
        int saldoActual = cliente.getSaldoPuntoCli() == null ? 0 : cliente.getSaldoPuntoCli().intValue();
        if (saldoActual < recompensa.getPuntosRequeridosRec()) {
            throw new IllegalStateException("Saldo de puntos insuficiente para canjear esta recompensa");
        }

        Connection conn = new Conexion().conectar();
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            descontarStockRecompensa(recompensa);

            BigDecimal puntosDescontar = new BigDecimal(recompensa.getPuntosRequeridosRec());
            cliente.setSaldoPuntoCli(cliente.getSaldoPuntoCli().subtract(puntosDescontar));
            clienteDao.actualizar(cliente);

            Canjes canje = new Canjes();
            canje.setIdCliCan(cliente.getIdCli());
            canje.setIdRecCan(recompensa.getIdRec());
            canje.setFechaCan(LocalDateTime.now());
            canje.setPuntosRecompensaCan(recompensa.getPuntosRequeridosRec());
            canjesDao.insertar(canje);

            HistorialPuntos his = new HistorialPuntos();
            his.setIdCliHis(cliente.getIdCli());
            his.setFechaHis(LocalDateTime.now());
            his.setTipoOperacionHis("S");
            his.setPuntosHis(-recompensa.getPuntosRequeridosRec());
            his.setReferenciaHis("Canje: " + recompensa.getNombreRec() + " (Canje #" + canje.getIdCan() + ")");
            historialPuntosDao.insertar(his);

            conn.commit();
            return canje;
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Error en transacción de canje: " + ex.getMessage(), ex);
        } finally {
            try {
                conn.setAutoCommit(autoCommitOriginal);
            } catch (SQLException ignored) {
            }
        }
    }

    public InfoDisponibilidad evaluarDisponibilidad(Recompensas recompensa, Map<Integer, Productos> mapaProductos) {
        if (recompensa == null) {
            return new InfoDisponibilidad(false, "No disponible");
        }
        if (!"A".equalsIgnoreCase(recompensa.getActivoRec())) {
            return new InfoDisponibilidad(false, "Inactiva");
        }
        Map<Integer, Productos> mapa = (mapaProductos != null) ? mapaProductos : obtenerMapaProductos();
        String tipo = recompensa.getTipoItemRec();
        int idItem = recompensa.getIdItemRec();

        if ("P".equalsIgnoreCase(tipo)) {
            Productos prod = mapa.get(idItem);
            if (prod == null || prod.getExistenciaPro() == null || prod.getExistenciaPro().compareTo(BigDecimal.ONE) < 0) {
                return new InfoDisponibilidad(false, "Agotado (0 disp.)");
            }
            return new InfoDisponibilidad(true, "Disponible (" + prod.getExistenciaPro().intValue() + " disp.)");
        } else if ("S".equalsIgnoreCase(tipo)) {
            List<DetalleSandwich> detalles = detalleSandwichDao.listarPorSandwich(idItem);
            if (detalles.isEmpty()) {
                return new InfoDisponibilidad(true, "Disponible");
            }
            for (DetalleSandwich det : detalles) {
                Productos prod = mapa.get(det.getIdProDet());
                if (prod == null || prod.getExistenciaPro() == null || prod.getExistenciaPro().compareTo(det.getCantidadDet()) < 0) {
                    return new InfoDisponibilidad(false, "Ingredientes insuficientes");
                }
            }
            return new InfoDisponibilidad(true, "Disponible");
        } else if ("M".equalsIgnoreCase(tipo)) {
            List<DetalleMenu> detalles = detalleMenuDao.listarPorMenu(idItem);
            if (detalles.isEmpty()) {
                return new InfoDisponibilidad(true, "Disponible");
            }
            for (DetalleMenu det : detalles) {
                if ("P".equalsIgnoreCase(det.getTipoItemDet())) {
                    Productos prod = mapa.get(det.getIdItemDet());
                    if (prod == null || prod.getExistenciaPro() == null || prod.getExistenciaPro().compareTo(det.getCantidadDet()) < 0) {
                        return new InfoDisponibilidad(false, "Componentes insuficientes");
                    }
                }
            }
            return new InfoDisponibilidad(true, "Disponible");
        }
        return new InfoDisponibilidad(true, "Disponible");
    }

    public boolean verificarDisponibilidad(Recompensas recompensa) {
        return evaluarDisponibilidad(recompensa, null).isDisponible();
    }

    public String obtenerDisponibilidadTexto(Recompensas recompensa) {
        return evaluarDisponibilidad(recompensa, null).getTexto();
    }

    private void descontarStockRecompensa(Recompensas recompensa) {
        String tipo = recompensa.getTipoItemRec();
        int idItem = recompensa.getIdItemRec();
        Map<Integer, Productos> mapa = obtenerMapaProductos();

        if ("P".equalsIgnoreCase(tipo)) {
            Productos prod = mapa.get(idItem);
            if (prod == null || prod.getExistenciaPro() == null || prod.getExistenciaPro().compareTo(BigDecimal.ONE) < 0) {
                throw new IllegalStateException("No hay existencia disponible del producto asociado a la recompensa");
            }
            prod.setExistenciaPro(prod.getExistenciaPro().subtract(BigDecimal.ONE));
            productosDao.actualizar(prod);

            InventarioMovimientos mov = new InventarioMovimientos();
            mov.setIdProImo(prod.getIdPro());
            mov.setFechaImo(LocalDateTime.now());
            mov.setCantidadImo(BigDecimal.ONE);
            mov.setTipoMovimientoImo("SALIDA POR CANJE");
            inventarioMovimientosDao.insertar(mov);
        } else if ("S".equalsIgnoreCase(tipo)) {
            List<DetalleSandwich> detalles = detalleSandwichDao.listarPorSandwich(idItem);
            for (DetalleSandwich det : detalles) {
                Productos prod = mapa.get(det.getIdProDet());
                if (prod != null && prod.getExistenciaPro() != null) {
                    if (prod.getExistenciaPro().compareTo(det.getCantidadDet()) < 0) {
                        throw new IllegalStateException("Existencia insuficiente del ingrediente: " + prod.getNombrePro());
                    }
                }
            }
            for (DetalleSandwich det : detalles) {
                Productos prod = mapa.get(det.getIdProDet());
                if (prod != null && prod.getExistenciaPro() != null) {
                    prod.setExistenciaPro(prod.getExistenciaPro().subtract(det.getCantidadDet()));
                    productosDao.actualizar(prod);

                    InventarioMovimientos mov = new InventarioMovimientos();
                    mov.setIdProImo(prod.getIdPro());
                    mov.setFechaImo(LocalDateTime.now());
                    mov.setCantidadImo(det.getCantidadDet());
                    mov.setTipoMovimientoImo("SALIDA POR CANJE");
                    inventarioMovimientosDao.insertar(mov);
                }
            }
        } else if ("M".equalsIgnoreCase(tipo)) {
            List<DetalleMenu> detalles = detalleMenuDao.listarPorMenu(idItem);
            for (DetalleMenu det : detalles) {
                if ("P".equalsIgnoreCase(det.getTipoItemDet())) {
                    Productos prod = mapa.get(det.getIdItemDet());
                    if (prod != null && prod.getExistenciaPro() != null) {
                        if (prod.getExistenciaPro().compareTo(det.getCantidadDet()) < 0) {
                            throw new IllegalStateException("Existencia insuficiente del componente: " + prod.getNombrePro());
                        }
                    }
                }
            }
            for (DetalleMenu det : detalles) {
                if ("P".equalsIgnoreCase(det.getTipoItemDet())) {
                    Productos prod = mapa.get(det.getIdItemDet());
                    if (prod != null && prod.getExistenciaPro() != null) {
                        prod.setExistenciaPro(prod.getExistenciaPro().subtract(det.getCantidadDet()));
                        productosDao.actualizar(prod);

                        InventarioMovimientos mov = new InventarioMovimientos();
                        mov.setIdProImo(prod.getIdPro());
                        mov.setFechaImo(LocalDateTime.now());
                        mov.setCantidadImo(det.getCantidadDet());
                        mov.setTipoMovimientoImo("SALIDA POR CANJE");
                        inventarioMovimientosDao.insertar(mov);
                    }
                }
            }
        }
    }
}
