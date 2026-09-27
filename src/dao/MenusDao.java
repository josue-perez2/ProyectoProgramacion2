package dao;

import model.Menus;

import java.util.List;

public interface MenusDao {
    List<Menus> listar();
    void insertar(Menus menu);
    void actualizar(Menus menu);
    void eliminar(int id);
}
