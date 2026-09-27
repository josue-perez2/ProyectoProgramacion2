package dao.Impl;

import config.Conexion;
import dao.DetalleMenuDao;
import model.Cliente;
import model.DetalleMenu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleMenuDaoImpl implements DetalleMenuDao {
    private final Conexion conexion;

    public  DetalleMenuDaoImpl() {
        this.conexion = new Conexion();
    }
    @Override
    public List<DetalleMenu> listar() {
        List<DetalleMenu> detalleMenu = new ArrayList<>();
        String sql = "SELECT ID_DET_MEN, ID_MEN_DET, TIPO_ITEM_DET, ID_ITEM_DET, CANTIDAD_DET FROM DETALLE_MENU";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                detalleMenu.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return detalleMenu;
    }

    @Override
    public void insertar(DetalleMenu detalleMenu) {
        String sql = "INSERT INTO CLIENTE (ID_DET_MEN, ID_MEN_DET, TIPO_ITEM_DET, ID_ITEM_DET, CANTIDAD_DET) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalleMenu.getIdDetMen());
            ps.setInt(2, detalleMenu.getIdMenDet());
            ps.setString(3, detalleMenu.getTipoItemDet());
            ps.setBigDecimal(4, detalleMenu.getCantidadDet());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void actualizar(DetalleMenu detalleMenu) {
        String sql = "UPDATE DETALLE_MENU SET  ID_MEN_DET = ?, TIPO_ITEM_DET = ?, ID_ITEM_DET = ?, CANTIDAD_DET = ? WHERE ID_DET_MEN = ? " ;
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalleMenu.getIdMenDet());
            ps.setString(2, detalleMenu.getTipoItemDet());
            ps.setBigDecimal(3, detalleMenu.getCantidadDet());
            ps.setInt(4, detalleMenu.getIdDetMen());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM DETALLE_MENU WHERE ID_DET_MEN = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DetalleMenu mapear(ResultSet rs) throws SQLException {
        DetalleMenu detalleMenu = new DetalleMenu();
        detalleMenu.setIdDetMen(rs.getInt("ID_DET_MEN"));
        detalleMenu.setIdMenDet(rs.getInt("ID_MEN_DET"));
        detalleMenu.setTipoItemDet(rs.getString("TIPO_ITEM_DET"));
        detalleMenu.setIdItemDet(rs.getInt("ID_ITEM_DET"));
        detalleMenu.setCantidadDet(rs.getBigDecimal("CANTIDAD_DET"));
        return detalleMenu;
    }
}
