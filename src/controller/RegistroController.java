package controller;

import com.sun.net.httpserver.HttpExchange;
import model.Cliente;
import util.Json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistroController extends ApiHandler {

    @Override
    protected void ejecutar(HttpExchange exchange) throws IOException {
        permitirMetodos(exchange, "POST");

        Map<String, Object> cuerpo = cuerpo(exchange);
        String correo = Json.texto(cuerpo, "correo");
        String password = Json.texto(cuerpo, "password");

        if (correo == null) {
            error(exchange, 400, "El campo correo es obligatorio");
            return;
        }
        if (password == null) {
            error(exchange, 400, "El campo password es obligatorio");
            return;
        }

        Cliente cliente = clienteService.buscarPorCorreo(correo);
        if (cliente == null) {
            error(exchange, 404, "El correo " + correo + " no esta registrado en la tabla CLIENTES");
            return;
        }
        if (cliente.getPasswordCli() != null && !cliente.getPasswordCli().isBlank()) {
            error(exchange, 409, "El cliente ya tiene una password registrada");
            return;
        }

        clienteService.registrarAcceso(cliente.getIdCli(), password);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mensaje", "Registro exitoso, ahora puede iniciar sesion");
        data.put("idCli", cliente.getIdCli());
        data.put("nombreCli", cliente.getNombreCli());
        data.put("correoCli", cliente.getCorreoCli());
        ok(exchange, data);
    }
}