package service;

import dao.ClienteDao;
import dao.Impl.ClienteDaoImpl;
import model.Cliente;

import java.util.List;

public class ClienteService {

    private final ClienteDao clienteDao;

    public ClienteService() {
        this.clienteDao = new ClienteDaoImpl();
    }

    public List<Cliente> listar() {
        return clienteDao.listar();
    }

    public Cliente buscarClientePorId(int id) {
        return clienteDao.buscarClientePorId(id);
    }

    public void insertar(Cliente cliente) {
        clienteDao.insertar(cliente);
    }

    public void actualizar(Cliente cliente) {
        clienteDao.actualizar(cliente);
    }

    public void eliminar(int id) {
        clienteDao.eliminar(id);
    }

    public void integridad(int id) {
        clienteDao.integridad(id);
    }
}