package dao.Impl;

import config.Conexion;
import dao.DetallesPedidoDao;
import model.DetallesPedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetallePedidoDaoImpl implements DetallesPedidoDao {

    private final Conexion conexion;

    public DetallePedidoDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<DetallesPedido> listar() {
        return consultar(null);
    }

    @Override
    public List<DetallesPedido> listarPorPedido(int idPedDet) {
        return consultar(idPedDet);
    }

    private List<DetallesPedido> consultar(Integer idPedDet) {
        List<DetallesPedido> detalles = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID_DET, ID_PED_DET, TIPO_ITEM_DET, ID_ITEM_DET, CANTIDAD_DET, PRECIO_UNITARIO_DET, SUBTOTAL_DET, PUNTOS_GENERADOS_DET FROM DETALLES_PEDIDO WHERE 1 = 1");
        if (idPedDet != null) {
            sql.append(" AND ID_PED_DET = ?");
        }
        sql.append(" ORDER BY ID_DET");

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (idPedDet != null) {
                ps.setInt(1, idPedDet);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return detalles;
    }

    @Override
    public void insertar(DetallesPedido detallesPedido) {
        String sql = "INSERT INTO DETALLES_PEDIDO (ID_PED_DET, TIPO_ITEM_DET, ID_ITEM_DET, CANTIDAD_DET, PRECIO_UNITARIO_DET, SUBTOTAL_DET, PUNTOS_GENERADOS_DET) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_DET"})) {
            ps.setInt(1, detallesPedido.getIdPedDet());
            ps.setString(2, detallesPedido.getTipoItemDet());
            ps.setInt(3, detallesPedido.getIdItemDet());
            ps.setBigDecimal(4, detallesPedido.getCantidadDet());
            ps.setBigDecimal(5, detallesPedido.getPrecioUnitarioDet());
            ps.setBigDecimal(6, detallesPedido.getSubTotalDet());
            ps.setInt(7, detallesPedido.getPuntosGeneradosDet());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    detallesPedido.setIdDet(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(DetallesPedido detallesPedido) {
        String sql = "UPDATE DETALLES_PEDIDO SET ID_PED_DET = ?, TIPO_ITEM_DET = ?, ID_ITEM_DET = ?, CANTIDAD_DET = ?, PRECIO_UNITARIO_DET = ?, SUBTOTAL_DET = ?, PUNTOS_GENERADOS_DET = ? WHERE ID_DET = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detallesPedido.getIdPedDet());
            ps.setString(2, detallesPedido.getTipoItemDet());
            ps.setInt(3, detallesPedido.getIdItemDet());
            ps.setBigDecimal(4, detallesPedido.getCantidadDet());
            ps.setBigDecimal(5, detallesPedido.getPrecioUnitarioDet());
            ps.setBigDecimal(6, detallesPedido.getSubTotalDet());
            ps.setInt(7, detallesPedido.getPuntosGeneradosDet());
            ps.setInt(8, detallesPedido.getIdDet());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM DETALLES_PEDIDO WHERE ID_DET = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminarPorPedido(int idPedDet) {
        String sql = "DELETE FROM DETALLES_PEDIDO WHERE ID_PED_DET = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedDet);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DetallesPedido mapear(ResultSet rs) throws SQLException {
        DetallesPedido detallesPedido = new DetallesPedido();
        detallesPedido.setIdDet(rs.getInt("ID_DET"));
        detallesPedido.setIdPedDet(rs.getInt("ID_PED_DET"));
        detallesPedido.setTipoItemDet(rs.getString("TIPO_ITEM_DET"));
        detallesPedido.setIdItemDet(rs.getInt("ID_ITEM_DET"));
        detallesPedido.setCantidadDet(rs.getBigDecimal("CANTIDAD_DET"));
        detallesPedido.setPrecioUnitarioDet(rs.getBigDecimal("PRECIO_UNITARIO_DET"));
        detallesPedido.setSubTotalDet(rs.getBigDecimal("SUBTOTAL_DET"));
        detallesPedido.setPuntosGeneradosDet(rs.getInt("PUNTOS_GENERADOS_DET"));
        return detallesPedido;
    }
}
