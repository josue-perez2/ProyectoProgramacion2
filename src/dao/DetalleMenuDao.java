package dao;

import model.DetalleMenu;

import java.util.List;

public interface DetalleMenuDao {
    List<DetalleMenu> listar();
    List<DetalleMenu> listarPorMenu(int idMenDet);
    void insertar(DetalleMenu detalleMenu);
    void actualizar(DetalleMenu detalleMenu);
    void eliminar(int id);
}
