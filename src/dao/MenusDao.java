package dao;

import model.Menus;

import java.util.List;

public interface MenusDao {
    List<Menus> listar();
    List<Menus> listarActivos();
    Menus buscarPorCodigo(String codigo);
    void insertar(Menus menu);
    void actualizar(Menus menu);
    void eliminar(int id);
}
