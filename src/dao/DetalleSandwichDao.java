package dao;

import java.util.List;

public interface DetalleSandwichDao {
    List<DetalleSandwichDao> listar();
    void insertar(DetalleSandwichDao cliente);
    void actualizar(DetalleSandwichDao cliente);
    void eliminar(int id);
}
