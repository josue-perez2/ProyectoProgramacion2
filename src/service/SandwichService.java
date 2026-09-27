package service;

import dao.SandwichDao;
import dao.Impl.SandwichDaoImpl;
import model.Sandwich;

import java.util.List;

public class SandwichService {

    private final SandwichDao sandwichDao;

    public SandwichService() {
        this.sandwichDao = new SandwichDaoImpl();
    }

    public List<Sandwich> listar() {
        return sandwichDao.listar();
    }

    public List<Sandwich> listarActivos() {
        return sandwichDao.listarActivos();
    }

    public Sandwich buscarPorCodigo(String codigo) {
        return sandwichDao.buscarPorCodigo(codigo);
    }

    public void insertar(Sandwich sandwich) {
        sandwichDao.insertar(sandwich);
    }

    public void actualizar(Sandwich sandwich) {
        sandwichDao.actualizar(sandwich);
    }

    public void eliminar(int id) {
        sandwichDao.eliminar(id);
    }
}
