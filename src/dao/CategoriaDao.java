package dao;

import model.Categorias;
import java.util.List;

public interface CategoriaDao {
    List<Categorias> listar();
    void insertar(Categorias categoria);
    void actualizar(Categorias categoria);
    void eliminar(int id);
}
