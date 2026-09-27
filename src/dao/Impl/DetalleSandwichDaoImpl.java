package dao.Impl;

import config.Conexion;
import dao.DetalleSandwichDao;
import model.Cliente;
import model.DetalleSandwich;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleSandwichDaoImpl implements DetalleSandwichDao {
    private final Conexion conexion;
    public DetalleSandwichDaoImpl() {
        this.conexion = new Conexion();
    }
    @Override
    public List<DetalleSandwich> listar() {
        List<DetalleSandwich> detalleSandwiches = new ArrayList<>();
        String sql = "SELECT ID_DET_SAN, ID_DET_SAN, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET FROM DETALLE_SANDWICH";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                detalleSandwiches.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return detalleSandwiches;
    }

    @Override
    public void insertar(DetalleSandwich detalleSandwich) {
        String sql = "INSERT INTO DETALLE_SANDWICH (ID_DET_SAN, ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalleSandwich.getIdDetSan());
            ps.setInt(2, detalleSandwich.getIdSanDet());
            ps.setInt(3, detalleSandwich.getIdProDet());
            ps.setBigDecimal(4, detalleSandwich.getCantidadDet());
            ps.setString(5, detalleSandwich.getObligatorioDet());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(DetalleSandwich detalleSandwich) {
        String sql = "UPDATE DETALLE_SANDWICH SET  ID_SAN_DET = ?, ID_PRO_DET = ?, CANTIDAD_DET = ?, OBLIGATORIO_DET = ? WHERE ID_DET_SAN = ?" ;
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalleSandwich.getIdSanDet());
            ps.setInt(2, detalleSandwich.getIdProDet());
            ps.setBigDecimal(3, detalleSandwich.getCantidadDet());
            ps.setString(4, detalleSandwich.getObligatorioDet());
            ps.setInt(5, detalleSandwich.getIdDetSan());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM DETALLE_SANDWICH WHERE ID_DET_MEN = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DetalleSandwich mapear(ResultSet rs) throws SQLException {
        DetalleSandwich detalleSandwich = new DetalleSandwich();
        detalleSandwich.setIdDetSan(rs.getInt("ID_DET_SAN"));
        detalleSandwich.setIdSanDet(rs.getInt("ID_SAN_DET"));
        detalleSandwich.setIdProDet(rs.getInt("ID_PRO_DET"));
        detalleSandwich.setCantidadDet(rs.getBigDecimal("CANTIDAD_DET"));
        detalleSandwich.setObligatorioDet(rs.getString("OBLIGATORIO_DET"));
        return detalleSandwich;
    }
}
