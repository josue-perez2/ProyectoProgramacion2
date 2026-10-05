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
        if (sandwich == null) {
            throw new IllegalArgumentException("El sándwich no puede ser nulo.");
        }
        String codigo = sandwich.getCodigoSan() != null ? sandwich.getCodigoSan().trim() : "";
        if (buscarPorCodigo(codigo) != null) {
            throw new IllegalStateException("Ya existe un sándwich con el código " + codigo + ".");
        }
        sandwichDao.insertar(sandwich);
    }

    public void actualizar(Sandwich sandwich) {
        if (sandwich == null) {
            throw new IllegalArgumentException("El sándwich no puede ser nulo.");
        }
        boolean existe = false;
        for (Sandwich s : listar()) {
            if (s.getIdSan() == sandwich.getIdSan()) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede modificar: el sándwich con ID " + sandwich.getIdSan() + " no existe.");
        }
        String codigo = sandwich.getCodigoSan() != null ? sandwich.getCodigoSan().trim() : "";
        Sandwich otro = buscarPorCodigo(codigo);
        if (otro != null && otro.getIdSan() != sandwich.getIdSan()) {
            throw new IllegalStateException("El código " + codigo + " ya está asignado a otro sándwich.");
        }
        sandwichDao.actualizar(sandwich);
    }

    public void eliminar(int id) {
        boolean existe = false;
        for (Sandwich s : listar()) {
            if (s.getIdSan() == id) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede eliminar: el sándwich con ID " + id + " no existe.");
        }
        sandwichDao.eliminar(id);
    }
}
