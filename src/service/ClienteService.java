package service;

import dao.ClienteDao;
import dao.Impl.ClienteDaoImpl;
import model.Cliente;
import util.Clave;

import java.util.List;
import java.util.UUID;

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

    public Cliente buscarPorCorreo(String correo) {
        return clienteDao.buscarPorCorreo(correo);
    }

    public Cliente buscarPorDpiYCcorreo(String dpi, String correo) {
        return clienteDao.buscarPorDpiYCcorreo(dpi, correo);
    }

    public Cliente buscarPorToken(String token) {
        return clienteDao.buscarPorToken(token);
    }

    public void registrarAcceso(int id, String passwordHash) {
        clienteDao.registrarAcceso(id, passwordHash);
    }

    public boolean verificarClave(String passwordHash, String hashAlmacenado) {
        return Clave.coincide(passwordHash, hashAlmacenado);
    }

    public String generarToken(int id) {
        String token = UUID.randomUUID().toString();
        clienteDao.actualizarToken(id, token);
        return token;
    }

    public void cerrarSesion(Cliente cliente) {
        clienteDao.actualizarToken(cliente.getIdCli(), null);
    }
}
