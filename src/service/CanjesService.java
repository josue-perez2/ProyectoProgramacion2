package service;

import dao.CanjesDao;
import dao.ClienteDao;
import dao.HistorialPuntosDao;
import dao.Impl.CanjesDaoImpl;
import dao.Impl.ClienteDaoImpl;
import dao.Impl.HistorialPuntosDaoImpl;
import model.Canjes;
import model.Cliente;
import model.HistorialPuntos;
import model.Recompensas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CanjesService {

    private final CanjesDao canjesDao;
    private final ClienteDao clienteDao;
    private final HistorialPuntosDao historialPuntosDao;

    public CanjesService() {
        this.canjesDao = new CanjesDaoImpl();
        this.clienteDao = new ClienteDaoImpl();
        this.historialPuntosDao = new HistorialPuntosDaoImpl();
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

    public void procesarCanje(Cliente cliente, Recompensas recompensa) {
        if (cliente == null || recompensa == null) {
            throw new IllegalArgumentException("Cliente y recompensa son obligatorios");
        }
        int saldoActual = cliente.getSaldoPuntoCli() == null ? 0 : cliente.getSaldoPuntoCli().intValue();
        if (saldoActual < recompensa.getPuntosRequeridosRec()) {
            throw new IllegalStateException("Saldo de puntos insuficiente para canjear esta recompensa");
        }

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
        his.setReferenciaHis("Canje de recompensa: " + recompensa.getNombreRec());
        historialPuntosDao.insertar(his);
    }
}
