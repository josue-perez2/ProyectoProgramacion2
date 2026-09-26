package dao;

import model.HistorialPuntos;

import java.util.List;

public interface HistorialPuntosDao {
    List<HistorialPuntos> listar();
    void insertar(HistorialPuntos cliente);
    void actualizar(HistorialPuntos cliente);
    void eliminar(int id);
}
