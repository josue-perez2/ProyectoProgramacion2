package dao.Impl;

import config.Conexion;
import dao.CanjesDao;
import model.Canjes;
import model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CanjesDaoImpl implements CanjesDao {
    private final Conexion conexion;

    public CanjesDaoImpl(){
        this.conexion = new Conexion();
    }
    @Override
    public List<Canjes> listar() {
        List<Canjes> canjes = new ArrayList<>();
        String sql = "SELECT ID_CAN, ID_CLI_CAN, ID_REC_CAN, FECHA_CAN, PUNTOS_RECOMPENSA_CAN FROM CANJES";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                canjes.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return canjes;
    }

    @Override
    public void insertar(Canjes canjes) {
        String sql = "INSERT INTO CANJES (ID_CLI_CAN, ID_REC_CAN, FECHA_CAN, PUNTOS_RECOMPENSA_CAN) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_CAN"})) {
            ps.setInt(1, canjes.getIdCliCan());
            ps.setInt(2, canjes.getIdRecCan());
            java.time.LocalDateTime fecha = canjes.getFechaCan() != null ? canjes.getFechaCan() : java.time.LocalDateTime.now();
            canjes.setFechaCan(fecha);
            ps.setTimestamp(3, Timestamp.valueOf(fecha));
            ps.setInt(4, canjes.getPuntosRecompensaCan());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    canjes.setIdCan(rs.getInt(1));
                }
            } catch (Exception ignored) {
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (canjes.getIdCan() == 0) {
            String sqlMax = "SELECT NVL(MAX(ID_CAN), 1) FROM CANJES WHERE ID_CLI_CAN = ?";
            try (Connection conn = conexion.conectar();
                 PreparedStatement psMax = conn.prepareStatement(sqlMax)) {
                psMax.setInt(1, canjes.getIdCliCan());
                try (ResultSet rsMax = psMax.executeQuery()) {
                    if (rsMax.next()) {
                        canjes.setIdCan(rsMax.getInt(1));
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void actualizar(Canjes canjes) {
        String sql = "UPDATE CANJES SET ID_CLI_CAN = ?, ID_REC_CAN = ?, FECHA_CAN = ?, PUNTOS_RECOMPENSA_CAN = ? WHERE ID_CAN = ? " ;
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, canjes.getIdCliCan());
            ps.setInt(2, canjes.getIdRecCan());
            ps.setTimestamp(3, Timestamp.valueOf(canjes.getFechaCan()));
            ps.setInt(4, canjes.getPuntosRecompensaCan());
            ps.setInt(5, canjes.getIdCan());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM CANJES WHERE ID_CAN = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private Canjes mapear(ResultSet rs) throws SQLException {
        Canjes canjes = new Canjes();
        canjes.setIdCan(rs.getInt("ID_CAN"));
        canjes.setIdCliCan(rs.getInt("ID_CLI_CAN"));
        canjes.setIdRecCan(rs.getInt("ID_REC_CAN"));
        Timestamp fecha = rs.getTimestamp("FECHA_CAN");
        canjes.setFechaCan(fecha != null ? fecha.toLocalDateTime() : null);
        canjes.setPuntosRecompensaCan(rs.getInt("PUNTOS_RECOMPENSA_CAN"));
        return canjes;
    }
}
