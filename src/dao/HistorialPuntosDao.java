package dao;

import model.HistorialPuntos;

import java.util.List;

public interface HistorialPuntosDao {
    List<HistorialPuntos> listar();
    List<HistorialPuntos> listarPorCliente(int idCliHis);
    void insertar(HistorialPuntos historialPunto);
    void actualizar(HistorialPuntos historialPunto);
    void eliminar(int id);
}
