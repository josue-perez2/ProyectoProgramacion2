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
    Cliente buscarPorDpi(String dpi);
    Cliente buscarPorCorreo(String correo);
    Cliente buscarPorDpiYCcorreo(String dpi, String correo);
    Cliente buscarPorToken(String token);
    void registrarAcceso(int id, String passwordHash);
    void actualizarToken(int id, String token);


}
