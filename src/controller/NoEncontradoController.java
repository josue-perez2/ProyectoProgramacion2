package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class NoEncontradoController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String mensaje = "{\"ok\":false,\"error\":\"Ruta no encontrada: " + exchange.getRequestURI().getPath() + "\"}";
        byte[] datos = mensaje.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, datos.length);
        exchange.getResponseBody().write(datos);
        exchange.close();
    }
}