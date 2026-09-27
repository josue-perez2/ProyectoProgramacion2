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
        menusDao.insertar(menu);
    }

    public void actualizar(Menus menu) {
        menusDao.actualizar(menu);
    }

    public void eliminar(int id) {
        menusDao.eliminar(id);
    }
}
