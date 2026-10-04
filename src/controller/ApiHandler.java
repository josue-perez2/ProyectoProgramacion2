package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Cliente;
import service.ClienteService;
import util.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ApiHandler implements HttpHandler {

    protected final ClienteService clienteService = new ClienteService();

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            ejecutar(exchange);
        } catch (IllegalArgumentException | IllegalStateException e) {
            error(exchange, 400, e.getMessage());
        } catch (Exception e) {
            error(exchange, 500, "Error interno del servidor");
        } finally {
            exchange.close();
        }
    }

    protected abstract void ejecutar(HttpExchange exchange) throws IOException;

    protected void permitirMetodos(HttpExchange exchange, String... metodos) throws IOException {
        for (String metodo : metodos) {
            if (metodo.equalsIgnoreCase(exchange.getRequestMethod())) {
                return;
            }
        }
        error(exchange, 405, "Metodo no permitido, use " + String.join(" o ", metodos));
    }

    protected Cliente clienteAutenticado(HttpExchange exchange) throws IOException {
        String cabecera = exchange.getRequestHeaders().getFirst("Authorization");
        if (cabecera == null || !cabecera.regionMatches(true, 0, "Bearer ", 0, 7)) {
            error(exchange, 401, "Falta el encabezado Authorization: Bearer <token>");
            return null;
        }
        Cliente cliente = clienteService.buscarPorToken(cabecera.substring(7).trim());
        if (cliente == null) {
            error(exchange, 401, "El token no es valido o la sesion no existe");
            return null;
        }
        return cliente;
    }

    protected Map<String, Object> cuerpo(HttpExchange exchange) throws IOException {
        try (InputStream entrada = exchange.getRequestBody()) {
            return Json.leerObjeto(new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    protected static void ok(HttpExchange exchange, Map<String, Object> data) throws IOException {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("ok", true);
        respuesta.putAll(data);
        responder(exchange, 200, respuesta);
    }

    protected static void error(HttpExchange exchange, int estado, String mensaje) throws IOException {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("ok", false);
        respuesta.put("error", mensaje);
        responder(exchange, estado, respuesta);
    }

    private static void responder(HttpExchange exchange, int estado, Map<String, Object> cuerpo) throws IOException {
        byte[] datos = Json.escribir(cuerpo).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(estado, datos.length);
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(datos);
        }
    }
}