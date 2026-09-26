package dao;

import model.Categorias;
import java.util.List;

public interface CategoriaDao {
    List<Categorias> listar();
    void insertar(Categorias cliente);
    void actualizar(Categorias cliente);
    void eliminar(int id);
}
