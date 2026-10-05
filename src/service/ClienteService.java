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
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (cliente.getDpiCli() != null && !cliente.getDpiCli().isBlank()) {
            Cliente conDpi = buscarPorDpi(cliente.getDpiCli().trim());
            if (conDpi != null) {
                throw new IllegalStateException("Ya existe un cliente registrado con el DPI " + cliente.getDpiCli().trim() + ".");
            }
        }
        if (cliente.getCorreoCli() != null && !cliente.getCorreoCli().isBlank()) {
            Cliente conCorreo = buscarPorCorreo(cliente.getCorreoCli().trim());
            if (conCorreo != null) {
                throw new IllegalStateException("Ya existe un cliente registrado con el correo " + cliente.getCorreoCli().trim() + ".");
            }
        }
        clienteDao.insertar(cliente);
    }

    public void actualizar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        Cliente existente = buscarClientePorId(cliente.getIdCli());
        if (existente == null) {
            throw new IllegalStateException("No se puede modificar: el cliente con ID " + cliente.getIdCli() + " no existe.");
        }
        if (cliente.getDpiCli() != null && !cliente.getDpiCli().isBlank()) {
            Cliente conDpi = buscarPorDpi(cliente.getDpiCli().trim());
            if (conDpi != null && conDpi.getIdCli() != cliente.getIdCli()) {
                throw new IllegalStateException("El DPI " + cliente.getDpiCli().trim() + " ya está asignado a otro cliente.");
            }
        }
        if (cliente.getCorreoCli() != null && !cliente.getCorreoCli().isBlank()) {
            Cliente conCorreo = buscarPorCorreo(cliente.getCorreoCli().trim());
            if (conCorreo != null && conCorreo.getIdCli() != cliente.getIdCli()) {
                throw new IllegalStateException("El correo " + cliente.getCorreoCli().trim() + " ya está asignado a otro cliente.");
            }
        }
        clienteDao.actualizar(cliente);
    }

    public void eliminar(int id) {
        Cliente existente = buscarClientePorId(id);
        if (existente == null) {
            throw new IllegalStateException("No se puede eliminar: el cliente con ID " + id + " no existe.");
        }
        clienteDao.eliminar(id);
    }

    public void integridad(int id) {
        clienteDao.integridad(id);
    }

    public Cliente buscarPorDpi(String dpi) {
        if (dpi == null || dpi.isBlank()) {
            return null;
        }
        return clienteDao.buscarPorDpi(dpi);
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
