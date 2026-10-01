package controller;

import com.sun.net.httpserver.HttpExchange;
import model.Cliente;
import model.Recompensas;
import service.RecompensaService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecompensasController extends ApiHandler {

    private final RecompensaService recompensaService = new RecompensaService();

    @Override
    protected void ejecutar(HttpExchange exchange) throws IOException {
        permitirMetodos(exchange, "GET");

        Cliente cliente = clienteAutenticado(exchange);
        if (cliente == null) {
            return;
        }

        int saldoPuntos = cliente.getSaldoPuntoCli() == null ? 0 : cliente.getSaldoPuntoCli().intValue();
        List<Object> recompensas = new ArrayList<>();
        int totalCanjeables = 0;
        int totalPendientes = 0;

        for (Recompensas recompensa : recompensaService.listar()) {
            boolean canjeable = recompensa.getPuntosRequeridosRec() <= saldoPuntos;
            if (canjeable) {
                totalCanjeables++;
            } else {
                totalPendientes++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idRec", recompensa.getIdRec());
            item.put("nombreRec", recompensa.getNombreRec());
            item.put("puntosRequeridosRec", recompensa.getPuntosRequeridosRec());
            item.put("tipoItemRec", recompensa.getTipoItemRec());
            item.put("idItemRec", recompensa.getIdItemRec());
            item.put("canjeable", canjeable);
            item.put("puntosFaltantes", canjeable ? 0 : recompensa.getPuntosRequeridosRec() - saldoPuntos);
            recompensas.add(item);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("saldoPuntoCli", cliente.getSaldoPuntoCli());
        resumen.put("totalCanjeables", totalCanjeables);
        resumen.put("totalPendientes", totalPendientes);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("idCli", cliente.getIdCli());
        data.put("nombreCli", cliente.getNombreCli());
        data.put("resumen", resumen);
        data.put("recompensas", recompensas);
        ok(exchange, data);
    }
}