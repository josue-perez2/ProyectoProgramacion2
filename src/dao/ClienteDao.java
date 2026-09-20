package dao;

import model.Cliente;

import java.util.List;

public interface ClienteDao {

    List<Cliente> listar();
    Cliente buscarClientePorId(int id);
    void insertar(Cliente cliente);
    void actualizar(Cliente cliente);
    void eliminar(int id);
    void integridad(int id);


}
