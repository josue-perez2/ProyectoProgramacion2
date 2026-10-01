package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class NoEncontradoController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        String mensaje = "{\"ok\":false,\"error\":\"Ruta no encontrada: " + exchange.getRequestURI().getPath() + "\"}";
        byte[] datos = mensaje.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, datos.length);
        exchange.getResponseBody().write(datos);
        exchange.close();
    }
}