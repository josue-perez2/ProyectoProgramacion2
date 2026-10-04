package controller;

import com.sun.net.httpserver.HttpExchange;
import model.Cliente;
import model.Recompensas;
import service.CanjesService;
import service.RecompensaService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class CanjeController extends ApiHandler {

    private final CanjesService canjesService = new CanjesService();
    private final RecompensaService recompensaService = new RecompensaService();

    @Override
    protected void ejecutar(HttpExchange exchange) throws IOException {
        permitirMetodos(exchange, "POST");

        Cliente cliente = clienteAutenticado(exchange);
        if (cliente == null) {
            return;
        }

        Map<String, Object> body = cuerpo(exchange);
        Object idRecObj = body.get("idRec");
        if (idRecObj == null) {
            error(exchange, 400, "El campo idRec es obligatorio");
            return;
        }

        int idRec;
        try {
            idRec = idRecObj instanceof Number ? ((Number) idRecObj).intValue() : Integer.parseInt(idRecObj.toString().trim());
        } catch (NumberFormatException e) {
            error(exchange, 400, "El campo idRec debe ser un numero entero");
            return;
        }

        Recompensas recompensa = null;
        for (Recompensas r : recompensaService.listar()) {
            if (r.getIdRec() == idRec) {
                recompensa = r;
                break;
            }
        }

        if (recompensa == null) {
            error(exchange, 404, "La recompensa con ID " + idRec + " no fue encontrada o no esta activa");
            return;
        }

        BigDecimal saldoAnterior = cliente.getSaldoPuntoCli() != null ? cliente.getSaldoPuntoCli() : BigDecimal.ZERO;
        if (saldoAnterior.intValue() < recompensa.getPuntosRequeridosRec()) {
            error(exchange, 400, "Puntos insuficientes. Tiene " + saldoAnterior.intValue() + " puntos y requiere " + recompensa.getPuntosRequeridosRec());
            return;
        }

        try {
            canjesService.procesarCanje(cliente, recompensa);
        } catch (IllegalStateException e) {
            error(exchange, 400, e.getMessage());
            return;
        }

        Cliente clienteActualizado = clienteService.buscarClientePorId(cliente.getIdCli());
        BigDecimal nuevoSaldo = clienteActualizado != null ? clienteActualizado.getSaldoPuntoCli() : saldoAnterior.subtract(new BigDecimal(recompensa.getPuntosRequeridosRec()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mensaje", "Canje realizado con exito");
        data.put("idRec", recompensa.getIdRec());
        data.put("nombreRec", recompensa.getNombreRec());
        data.put("puntosCanjeados", recompensa.getPuntosRequeridosRec());
        data.put("saldoAnterior", saldoAnterior);
        data.put("saldoActual", nuevoSaldo);
        ok(exchange, data);
    }
}
