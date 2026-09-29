package service;

import dao.HistorialPuntosDao;
import dao.Impl.HistorialPuntosDaoImpl;
import model.HistorialPuntos;

import java.util.List;

public class HistorialPuntosService {

    private final HistorialPuntosDao historialPuntosDao;

    public HistorialPuntosService() {
        this.historialPuntosDao = new HistorialPuntosDaoImpl();
    }

    public List<HistorialPuntos> listar() {
        return historialPuntosDao.listar();
    }

    public List<HistorialPuntos> listarPorCliente(int idCliHis) {
        return historialPuntosDao.listarPorCliente(idCliHis);
    }

    public void insertar(HistorialPuntos historialPunto) {
        historialPuntosDao.insertar(historialPunto);
    }

    public void actualizar(HistorialPuntos historialPunto) {
        historialPuntosDao.actualizar(historialPunto);
    }

    public void eliminar(int id) {
        historialPuntosDao.eliminar(id);
    }
}
