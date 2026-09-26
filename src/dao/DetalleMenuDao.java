package dao;

import model.DetalleMenu;

import java.util.List;

public interface DetalleMenuDao {
    List<DetalleMenu> listar();
    void insertar(DetalleMenu cliente);
    void actualizar(DetalleMenu cliente);
    void eliminar(int id);
}
