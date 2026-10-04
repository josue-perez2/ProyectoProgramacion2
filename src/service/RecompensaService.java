package service;

import dao.RecompesasDao;
import dao.Impl.RecompensasDaoImpl;
import model.Recompensas;

import java.util.List;

public class RecompensaService {

    private final RecompesasDao recompesasDao;

    public RecompensaService() {
        this.recompesasDao = new RecompensasDaoImpl();
    }

    public List<Recompensas> listar() {
        return recompesasDao.listar();
    }

    public List<Recompensas> listarCanjeables(int saldoPuntos) {
        return listar().stream()
                .filter(recompensa -> recompensa.getPuntosRequeridosRec() <= saldoPuntos)
                .toList();
    }

    public void insertar(Recompensas recompensa) {
        recompesasDao.insertar(recompensa);
    }

    public void actualizar(Recompensas recompensa) {
        recompesasDao.actualizar(recompensa);
    }

    public void eliminar(int id) {
        recompesasDao.eliminar(id);
    }
}