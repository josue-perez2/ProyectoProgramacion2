package controller;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiServer {

    private static final int PUERTO = 8080;

    public void iniciar() throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidor.createContext("/api/registro", new RegistroController());
        servidor.createContext("/api/login", new LoginController());
        servidor.createContext("/api/puntos/historial", new HistorialPuntosController());
        servidor.createContext("/api/puntos/recompensas", new RecompensasController());
        servidor.createContext("/api/puntos/canjear", new CanjeController());
        servidor.createContext("/", new NoEncontradoController());
        servidor.setExecutor(Executors.newFixedThreadPool(5));
        servidor.start();
        System.out.println("API escuchando en http://localhost:" + PUERTO);
    }

    public static void main(String[] args) throws IOException {
        new ApiServer().iniciar();
    }
}