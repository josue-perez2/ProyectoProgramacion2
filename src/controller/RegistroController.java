package controller;

import com.sun.net.httpserver.HttpExchange;
import model.Cliente;
import util.FormatoTexto;
import util.Json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistroController extends ApiHandler {

    private static final int LARGO_DPI = 13;

    @Override
    protected void ejecutar(HttpExchange exchange) throws IOException {
        permitirMetodos(exchange, "POST");

        Map<String, Object> cuerpo = cuerpo(exchange);
        String dpi = Json.texto(cuerpo, "dpi");
        String correo = Json.texto(cuerpo, "correo");
        String password = Json.texto(cuerpo, "password");

        if (dpi == null) {
            error(exchange, 400, "El campo dpi es obligatorio");
            return;
        }
        if (correo == null) {
            error(exchange, 400, "El campo correo es obligatorio");
            return;
        }
        if (password == null) {
            error(exchange, 400, "El campo password es obligatorio");
            return;
        }

        String digitosDpi = FormatoTexto.soloDigitos(dpi);
        if (digitosDpi.length() != LARGO_DPI) {
            error(exchange, 400, "El DPI debe tener 13 digitos");
            return;
        }

        Cliente cliente = clienteService.buscarPorDpiYCcorreo(digitosDpi, correo);
        if (cliente == null) {
            error(exchange, 404, "El cliente no existe, el DPI "
                    + FormatoTexto.formatearDpi(digitosDpi) + " y el correo "
                    + correo + " no coinciden");
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
        data.put("dpiCli", cliente.getDpiCli());
        data.put("correoCli", cliente.getCorreoCli());
        ok(exchange, data);
    }
}
