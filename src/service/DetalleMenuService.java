package service;

import dao.DetalleMenuDao;
import dao.Impl.DetalleMenuDaoImpl;
import model.DetalleMenu;

import java.util.List;

public class DetalleMenuService {

    private final DetalleMenuDao detalleMenuDao;

    public DetalleMenuService() {
        this.detalleMenuDao = new DetalleMenuDaoImpl();
    }

    public List<DetalleMenu> listarPorMenu(int idMenDet) {
        return detalleMenuDao.listarPorMenu(idMenDet);
    }

    public void insertar(DetalleMenu detalleMenu) {
        detalleMenuDao.insertar(detalleMenu);
    }

    public void actualizar(DetalleMenu detalleMenu) {
        detalleMenuDao.actualizar(detalleMenu);
    }

    public void eliminar(int id) {
        detalleMenuDao.eliminar(id);
    }
}
