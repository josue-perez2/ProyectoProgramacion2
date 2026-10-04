package dao.Impl;

import config.Conexion;
import dao.RecompesasDao;
import model.Recompensas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecompensasDaoImpl implements RecompesasDao {

    private final Conexion conexion;

    public RecompensasDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<Recompensas> listar() {
        List<Recompensas> recompensas = new ArrayList<>();
        String sql = "SELECT ID_REC, NOMBRE_REC, PUNTOS_REQUERIDOS_REC, TIPO_ITEM_REC, ID_ITEM_REC, ACTIVO_REC " +
                "FROM RECOMPENSAS WHERE ACTIVO_REC = 'A' ORDER BY PUNTOS_REQUERIDOS_REC, ID_REC";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                recompensas.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return recompensas;
    }

    @Override
    public void insertar(Recompensas recompensa) {
        String sql = "INSERT INTO RECOMPENSAS (NOMBRE_REC, PUNTOS_REQUERIDOS_REC, TIPO_ITEM_REC, ID_ITEM_REC, ACTIVO_REC) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_REC"})) {
            ps.setString(1, recompensa.getNombreRec());
            ps.setInt(2, recompensa.getPuntosRequeridosRec());
            ps.setString(3, recompensa.getTipoItemRec());
            ps.setInt(4, recompensa.getIdItemRec());
            ps.setString(5, recompensa.getActivoRec() != null ? recompensa.getActivoRec() : "A");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    recompensa.setIdRec(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Recompensas recompensa) {
        String sql = "UPDATE RECOMPENSAS SET NOMBRE_REC = ?, PUNTOS_REQUERIDOS_REC = ?, TIPO_ITEM_REC = ?, ID_ITEM_REC = ?, ACTIVO_REC = ? WHERE ID_REC = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, recompensa.getNombreRec());
            ps.setInt(2, recompensa.getPuntosRequeridosRec());
            ps.setString(3, recompensa.getTipoItemRec());
            ps.setInt(4, recompensa.getIdItemRec());
            ps.setString(5, recompensa.getActivoRec() != null ? recompensa.getActivoRec() : "A");
            ps.setInt(6, recompensa.getIdRec());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM RECOMPENSAS WHERE ID_REC = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Recompensas mapear(ResultSet rs) throws SQLException {
        Recompensas recompensas = new Recompensas();
        recompensas.setIdRec(rs.getInt("ID_REC"));
        recompensas.setNombreRec(rs.getString("NOMBRE_REC"));
        recompensas.setPuntosRequeridosRec(rs.getInt("PUNTOS_REQUERIDOS_REC"));
        recompensas.setTipoItemRec(rs.getString("TIPO_ITEM_REC"));
        recompensas.setIdItemRec(rs.getInt("ID_ITEM_REC"));
        recompensas.setActivoRec(rs.getString("ACTIVO_REC"));
        return recompensas;
    }
}
