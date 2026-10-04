package dao.Impl;

import config.Conexion;
import dao.InventarioMovimientosDao;
import model.InventarioMovimientos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventarioMovimientosDaoImpl implements InventarioMovimientosDao {

    private final Conexion conexion;

    public InventarioMovimientosDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<InventarioMovimientos> listar() {
        List<InventarioMovimientos> lista = new ArrayList<>();
        String sql = "SELECT ID_IMO, ID_PRO_IMO, FECHA_IMO, CANTIDAD_IMO, TIPO_MOVIMIENTO_IMO FROM INVENTARIO_MOVIMIENTOS ORDER BY ID_IMO DESC";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public void insertar(InventarioMovimientos inventarioMovimiento) {
        String sql = "INSERT INTO INVENTARIO_MOVIMIENTOS (ID_PRO_IMO, FECHA_IMO, CANTIDAD_IMO, TIPO_MOVIMIENTO_IMO) VALUES (?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_IMO"})) {
            ps.setInt(1, inventarioMovimiento.getIdProImo());
            LocalDateTime fecha = inventarioMovimiento.getFechaImo() != null ? inventarioMovimiento.getFechaImo() : LocalDateTime.now();
            inventarioMovimiento.setFechaImo(fecha);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            ps.setBigDecimal(3, inventarioMovimiento.getCantidadImo());
            ps.setString(4, inventarioMovimiento.getTipoMovimientoImo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    inventarioMovimiento.setIdImo(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(InventarioMovimientos inventarioMovimiento) {
        String sql = "UPDATE INVENTARIO_MOVIMIENTOS SET ID_PRO_IMO = ?, FECHA_IMO = ?, CANTIDAD_IMO = ?, TIPO_MOVIMIENTO_IMO = ? WHERE ID_IMO = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inventarioMovimiento.getIdProImo());
            LocalDateTime fecha = inventarioMovimiento.getFechaImo() != null ? inventarioMovimiento.getFechaImo() : LocalDateTime.now();
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            ps.setBigDecimal(3, inventarioMovimiento.getCantidadImo());
            ps.setString(4, inventarioMovimiento.getTipoMovimientoImo());
            ps.setInt(5, inventarioMovimiento.getIdImo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM INVENTARIO_MOVIMIENTOS WHERE ID_IMO = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private InventarioMovimientos mapear(ResultSet rs) throws SQLException {
        InventarioMovimientos inventario = new InventarioMovimientos();
        inventario.setIdImo(rs.getInt("ID_IMO"));
        inventario.setIdProImo(rs.getInt("ID_PRO_IMO"));
        Timestamp fecha = rs.getTimestamp("FECHA_IMO");
        inventario.setFechaImo(fecha != null ? fecha.toLocalDateTime() : null);
        inventario.setCantidadImo(rs.getBigDecimal("CANTIDAD_IMO"));
        inventario.setTipoMovimientoImo(rs.getString("TIPO_MOVIMIENTO_IMO"));
        return inventario;
    }
}
