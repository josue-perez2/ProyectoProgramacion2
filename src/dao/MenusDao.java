package dao;

import model.Menus;

import java.util.List;

public interface MenusDao {
    List<Menus> listar();
    void insertar(Menus cliente);
    void actualizar(Menus cliente);
    void eliminar(int id);
}
