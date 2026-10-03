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
}