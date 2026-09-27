package dao;

import model.Canjes;

import java.util.List;

public interface CanjesDao {
    List<Canjes> listar();
    void insertar(Canjes canje);
    void actualizar(Canjes canje);
    void eliminar(int id);


}
