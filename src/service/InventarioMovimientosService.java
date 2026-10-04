package service;

import dao.InventarioMovimientosDao;
import dao.Impl.InventarioMovimientosDaoImpl;
import model.InventarioMovimientos;

import java.util.List;

public class InventarioMovimientosService {

    private final InventarioMovimientosDao inventarioMovimientosDao;

    public InventarioMovimientosService() {
        this.inventarioMovimientosDao = new InventarioMovimientosDaoImpl();
    }

    public List<InventarioMovimientos> listar() {
        return inventarioMovimientosDao.listar();
    }

    public void insertar(InventarioMovimientos movimiento) {
        inventarioMovimientosDao.insertar(movimiento);
    }

    public void actualizar(InventarioMovimientos movimiento) {
        inventarioMovimientosDao.actualizar(movimiento);
    }

    public void eliminar(int id) {
        inventarioMovimientosDao.eliminar(id);
    }
}
