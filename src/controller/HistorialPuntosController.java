package controller;

import com.sun.net.httpserver.HttpExchange;
import model.Cliente;
import model.HistorialPuntos;
import service.HistorialPuntosService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HistorialPuntosController extends ApiHandler {

    private final HistorialPuntosService historialPuntosService = new HistorialPuntosService();

    @Override
    protected void ejecutar(HttpExchange exchange) throws IOException {
        permitirMetodos(exchange, "GET");

        Cliente cliente = clienteAutenticado(exchange);
        if (cliente == null) {
            return;
        }

        List<HistorialPuntos> historial = historialPuntosService.listarPorCliente(cliente.getIdCli());
        List<Object> movimientos = new ArrayList<>();
        int puntosObtenidos = 0;
        int puntosCanjeados = 0;

        for (HistorialPuntos punto : historial) {
            Map<String, Object> movimiento = new LinkedHashMap<>();
            movimiento.put("idHis", punto.getIdHis());
            movimiento.put("fechaHis", punto.getFechaHis());
            movimiento.put("tipoOperacionHis", punto.getTipoOperacionHis());
            movimiento.put("puntosHis", punto.getPuntosHis());
            movimiento.put("referenciaHis", punto.getReferenciaHis());
            movimientos.add(movimiento);

            if (punto.getPuntosHis() >= 0) {
                puntosObtenidos += punto.getPuntosHis();
            } else {
                puntosCanjeados += Math.abs(punto.getPuntosHis());
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("idCli", cliente.getIdCli());
        data.put("nombreCli", cliente.getNombreCli());
        data.put("saldoPuntoCli", cliente.getSaldoPuntoCli());
        data.put("totalMovimientos", movimientos.size());
        data.put("puntosObtenidos", puntosObtenidos);
        data.put("puntosCanjeados", puntosCanjeados);
        data.put("historial", movimientos);
        ok(exchange, data);
    }
}