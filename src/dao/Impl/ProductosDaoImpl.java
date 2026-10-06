package dao.Impl;

import config.Conexion;
import dao.ProductosDao;
import model.Productos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductosDaoImpl implements ProductosDao {

    private final Conexion conexion;

    public ProductosDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<Productos> listar() {
        List<Productos> productos = new ArrayList<>();
        String sql = "SELECT ID_PRO, CODIGO_PRO, ID_CAT_PRO, NOMBRE_PRO, PRECIO_PRO, EXISTENCIA_PRO, ACTIVO_PRO FROM PRODUCTOS";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productos;
    }

    @Override
    public List<Productos> listarActivosPorCategoria(int idCatPro) {
        List<Productos> productos = new ArrayList<>();
        String sql = "SELECT ID_PRO, CODIGO_PRO, ID_CAT_PRO, NOMBRE_PRO, PRECIO_PRO, EXISTENCIA_PRO, ACTIVO_PRO " +
                "FROM PRODUCTOS WHERE ID_CAT_PRO = ? AND ACTIVO_PRO = 'A' ORDER BY NOMBRE_PRO";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCatPro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productos;
    }

    @Override
    public Productos buscarPorId(int id) {
        String sql = "SELECT ID_PRO, CODIGO_PRO, ID_CAT_PRO, NOMBRE_PRO, PRECIO_PRO, EXISTENCIA_PRO, ACTIVO_PRO FROM PRODUCTOS WHERE ID_PRO = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void insertar(Productos producto) {
        String sql = "INSERT INTO PRODUCTOS (CODIGO_PRO, ID_CAT_PRO, NOMBRE_PRO, PRECIO_PRO, EXISTENCIA_PRO, ACTIVO_PRO) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigoPro());
            ps.setInt(2, producto.getIdCatPro());
            ps.setString(3, producto.getNombrePro());
            ps.setBigDecimal(4, producto.getPrecioPro());
            ps.setBigDecimal(5, producto.getExistenciaPro());
            ps.setString(6, producto.getActivoPro());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Productos producto) {
        String sql = "UPDATE PRODUCTOS SET CODIGO_PRO = ?, ID_CAT_PRO = ?, NOMBRE_PRO = ?, PRECIO_PRO = ?, " +
                "EXISTENCIA_PRO = ?, ACTIVO_PRO = ? WHERE ID_PRO = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigoPro());
            ps.setInt(2, producto.getIdCatPro());
            ps.setString(3, producto.getNombrePro());
            ps.setBigDecimal(4, producto.getPrecioPro());
            ps.setBigDecimal(5, producto.getExistenciaPro());
            ps.setString(6, producto.getActivoPro());
            ps.setInt(7, producto.getIdPro());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM PRODUCTOS WHERE ID_PRO = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Productos mapear(ResultSet rs) throws SQLException {
        Productos productos = new Productos();
        productos.setIdPro(rs.getInt("ID_PRO"));
        productos.setCodigoPro(rs.getString("CODIGO_PRO"));
        productos.setIdCatPro(rs.getInt("ID_CAT_PRO"));
        productos.setNombrePro(rs.getString("NOMBRE_PRO"));
        productos.setPrecioPro(rs.getBigDecimal("PRECIO_PRO"));
        productos.setExistenciaPro(rs.getBigDecimal("EXISTENCIA_PRO"));
        productos.setActivoPro(rs.getString("ACTIVO_PRO"));
        return productos;
    }
}
