package dao.Impl;

import dao.PedidosDao;
import model.Pedidos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class PedidosDaoImpl implements PedidosDao {

    @Override
    public List<Pedidos> listar() {
        return List.of();
    }

    @Override
    public void insertar(Pedidos cliente) {

    }

    @Override
    public void actualizar(Pedidos cliente) {

    }

    @Override
    public void eliminar(int id) {

    }
    private Pedidos mapear(ResultSet rs) throws SQLException {
        Pedidos pedidos = new Pedidos();
        pedidos.setIdPed(rs.getInt("ID_PED"));
        pedidos.setIdCliPed(rs.getInt("ID_CLI_PED"));

        Timestamp fecha = rs.getTimestamp("FECHA_PED");
        pedidos.setFechaPed(fecha != null ? fecha.toLocalDateTime() : null);
        pedidos.setTotalPed(rs.getBigDecimal("TOTAL_PED"));
        pedidos.setPuntosObtenidosPed(rs.getInt("PUNTOS_OBTENIDOS_PED"));
        pedidos.setEstadoPed(rs.getString("ESTADO_PED"));
        return pedidos;
    }
}
