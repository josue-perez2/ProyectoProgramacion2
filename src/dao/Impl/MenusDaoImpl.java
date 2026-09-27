package dao.Impl;

import config.Conexion;
import dao.MenusDao;
import model.Menus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenusDaoImpl implements MenusDao {

    private final Conexion conexion;

    public MenusDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<Menus> listar() {
        return consultar(null, false);
    }

    @Override
    public List<Menus> listarActivos() {
        return consultar(null, true);
    }

    @Override
    public Menus buscarPorCodigo(String codigo) {
        List<Menus> encontrados = consultar(codigo, false);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    private List<Menus> consultar(String codigo, boolean soloActivos) {
        List<Menus> menus = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID_MEN, CODIGO_MEN, NOMBRE_MEN, PRECIO_MEN, ACTIVO_MEN FROM MENUS WHERE 1 = 1");
        if (codigo != null) {
            sql.append(" AND CODIGO_MEN = ?");
        }
        if (soloActivos) {
            sql.append(" AND ACTIVO_MEN = 'A'");
        }
        sql.append(" ORDER BY ID_MEN");

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (codigo != null) {
                ps.setString(1, codigo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    menus.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return menus;
    }

    @Override
    public void insertar(Menus menu) {
        String sql = "INSERT INTO MENUS (CODIGO_MEN, NOMBRE_MEN, PRECIO_MEN, ACTIVO_MEN) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, menu.getCodigoMen());
            ps.setString(2, menu.getNombreMen());
            ps.setBigDecimal(3, menu.getPrecioMen());
            ps.setString(4, menu.getActivoMen());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Menus menu) {
        String sql = "UPDATE MENUS SET CODIGO_MEN = ?, NOMBRE_MEN = ?, PRECIO_MEN = ?, ACTIVO_MEN = ? WHERE ID_MEN = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, menu.getCodigoMen());
            ps.setString(2, menu.getNombreMen());
            ps.setBigDecimal(3, menu.getPrecioMen());
            ps.setString(4, menu.getActivoMen());
            ps.setInt(5, menu.getIdMen());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM MENUS WHERE ID_MEN = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Menus mapear(ResultSet rs) throws SQLException {
        Menus menu = new Menus();
        menu.setIdMen(rs.getInt("ID_MEN"));
        menu.setCodigoMen(rs.getString("CODIGO_MEN"));
        menu.setNombreMen(rs.getString("NOMBRE_MEN"));
        menu.setPrecioMen(rs.getBigDecimal("PRECIO_MEN"));
        menu.setActivoMen(rs.getString("ACTIVO_MEN"));
        return menu;
    }
}
