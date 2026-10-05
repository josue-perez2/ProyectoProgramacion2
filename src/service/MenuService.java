package service;

import dao.MenusDao;
import dao.Impl.MenusDaoImpl;
import model.Menus;

import java.util.List;

public class MenuService {

    private final MenusDao menusDao;

    public MenuService() {
        this.menusDao = new MenusDaoImpl();
    }

    public List<Menus> listar() {
        return menusDao.listar();
    }

    public List<Menus> listarActivos() {
        return menusDao.listarActivos();
    }

    public Menus buscarPorCodigo(String codigo) {
        return menusDao.buscarPorCodigo(codigo);
    }

    public void insertar(Menus menu) {
        if (menu == null) {
            throw new IllegalArgumentException("El menú no puede ser nulo.");
        }
        String codigo = menu.getCodigoMen() != null ? menu.getCodigoMen().trim() : "";
        if (buscarPorCodigo(codigo) != null) {
            throw new IllegalStateException("Ya existe un combo con el código " + codigo + ".");
        }
        menusDao.insertar(menu);
    }

    public void actualizar(Menus menu) {
        if (menu == null) {
            throw new IllegalArgumentException("El menú no puede ser nulo.");
        }
        boolean existe = false;
        for (Menus m : listar()) {
            if (m.getIdMen() == menu.getIdMen()) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede modificar: el combo con ID " + menu.getIdMen() + " no existe.");
        }
        String codigo = menu.getCodigoMen() != null ? menu.getCodigoMen().trim() : "";
        Menus otro = buscarPorCodigo(codigo);
        if (otro != null && otro.getIdMen() != menu.getIdMen()) {
            throw new IllegalStateException("El código " + codigo + " ya está asignado a otro combo.");
        }
        menusDao.actualizar(menu);
    }

    public void eliminar(int id) {
        boolean existe = false;
        for (Menus m : listar()) {
            if (m.getIdMen() == id) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede eliminar: el combo con ID " + id + " no existe.");
        }
        menusDao.eliminar(id);
    }
}
