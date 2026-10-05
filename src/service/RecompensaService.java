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
        if (recompensa == null) {
            throw new IllegalArgumentException("La recompensa no puede ser nula.");
        }
        String nombre = recompensa.getNombreRec() != null ? recompensa.getNombreRec().trim() : "";
        for (Recompensas r : listar()) {
            if (r.getNombreRec() != null && r.getNombreRec().equalsIgnoreCase(nombre)) {
                throw new IllegalStateException("Ya existe una recompensa con el nombre '" + nombre + "'.");
            }
        }
        recompesasDao.insertar(recompensa);
    }

    public void actualizar(Recompensas recompensa) {
        if (recompensa == null) {
            throw new IllegalArgumentException("La recompensa no puede ser nula.");
        }
        boolean existe = false;
        String nombre = recompensa.getNombreRec() != null ? recompensa.getNombreRec().trim() : "";
        for (Recompensas r : listar()) {
            if (r.getIdRec() == recompensa.getIdRec()) {
                existe = true;
            } else if (r.getNombreRec() != null && r.getNombreRec().equalsIgnoreCase(nombre)) {
                throw new IllegalStateException("El nombre '" + nombre + "' ya está en uso por otra recompensa.");
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede modificar: la recompensa con ID " + recompensa.getIdRec() + " no existe.");
        }
        recompesasDao.actualizar(recompensa);
    }

    public void eliminar(int id) {
        boolean existe = false;
        for (Recompensas r : listar()) {
            if (r.getIdRec() == id) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede eliminar: la recompensa con ID " + id + " no existe.");
        }
        recompesasDao.eliminar(id);
    }
}