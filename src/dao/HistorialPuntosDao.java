package dao;

import model.HistorialPuntos;

import java.util.List;

public interface HistorialPuntosDao {
    List<HistorialPuntos> listar();
    void insertar(HistorialPuntos historialPunto);
    void actualizar(HistorialPuntos historialPunto);
    void eliminar(int id);
}
