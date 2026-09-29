package dao.Impl;

import config.Conexion;
import dao.PedidosDao;
import model.Pedidos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidosDaoImpl implements PedidosDao {

    private final Conexion conexion;

    public PedidosDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<Pedidos> listar() {
        return consultar(null, null);
    }

    @Override
    public List<Pedidos> listarPorCliente(int idCliPed) {
        return consultar(null, idCliPed);
    }

    @Override
    public Pedidos buscarPorId(int id) {
        List<Pedidos> lista = consultar(id, null);
        return lista.isEmpty() ? null : lista.get(0);
    }

    private List<Pedidos> consultar(Integer idPed, Integer idCliPed) {
        List<Pedidos> pedidos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID_PED, ID_CLI_PED, FECHA_PED, TOTAL_PED, PUNTOS_OBTENIDOS_PED, ESTADO_PED FROM PEDIDOS WHERE 1 = 1");
        if (idPed != null) {
            sql.append(" AND ID_PED = ?");
        }
        if (idCliPed != null) {
            sql.append(" AND ID_CLI_PED = ?");
        }
        sql.append(" ORDER BY ID_PED DESC");

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int indice = 1;
            if (idPed != null) {
                ps.setInt(indice++, idPed);
            }
            if (idCliPed != null) {
                ps.setInt(indice++, idCliPed);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pedidos;
    }

    @Override
    public void insertar(Pedidos pedido) {
        String sql = "INSERT INTO PEDIDOS (ID_CLI_PED, FECHA_PED, TOTAL_PED, PUNTOS_OBTENIDOS_PED, ESTADO_PED) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PED"})) {
            ps.setInt(1, pedido.getIdCliPed());
            LocalDateTime fecha = pedido.getFechaPed() != null ? pedido.getFechaPed() : LocalDateTime.now();
            pedido.setFechaPed(fecha);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            ps.setBigDecimal(3, pedido.getTotalPed());
            ps.setInt(4, pedido.getPuntosObtenidosPed());
            ps.setString(5, pedido.getEstadoPed() != null ? pedido.getEstadoPed() : "P");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setIdPed(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Pedidos pedido) {
        String sql = "UPDATE PEDIDOS SET ID_CLI_PED = ?, FECHA_PED = ?, TOTAL_PED = ?, PUNTOS_OBTENIDOS_PED = ?, ESTADO_PED = ? WHERE ID_PED = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedido.getIdCliPed());
            ps.setTimestamp(2, Timestamp.valueOf(pedido.getFechaPed()));
            ps.setBigDecimal(3, pedido.getTotalPed());
            ps.setInt(4, pedido.getPuntosObtenidosPed());
            ps.setString(5, pedido.getEstadoPed());
            ps.setInt(6, pedido.getIdPed());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM PEDIDOS WHERE ID_PED = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
