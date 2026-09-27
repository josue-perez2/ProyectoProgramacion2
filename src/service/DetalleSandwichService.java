package service;

import dao.DetalleSandwichDao;
import dao.Impl.DetalleSandwichDaoImpl;
import model.DetalleSandwich;

import java.util.List;

public class DetalleSandwichService {

    private final DetalleSandwichDao detalleSandwichDao;

    public DetalleSandwichService() {
        this.detalleSandwichDao = new DetalleSandwichDaoImpl();
    }

    public List<DetalleSandwich> listarPorSandwich(int idSanDet) {
        return detalleSandwichDao.listarPorSandwich(idSanDet);
    }

    public void insertar(DetalleSandwich detalleSandwich) {
        detalleSandwichDao.insertar(detalleSandwich);
    }

    public void actualizar(DetalleSandwich detalleSandwich) {
        detalleSandwichDao.actualizar(detalleSandwich);
    }

    public void eliminar(int id) {
        detalleSandwichDao.eliminar(id);
    }
}
