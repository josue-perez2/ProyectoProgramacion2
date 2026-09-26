package dao;

import model.Canjes;

import java.util.List;

public interface CanjesDao {
    List<Canjes> listar();
    void insertar(Canjes cliente);
    void actualizar(Canjes cliente);
    void eliminar(int id);


}
