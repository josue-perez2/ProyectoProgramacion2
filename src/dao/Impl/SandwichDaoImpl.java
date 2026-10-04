package dao.Impl;

import config.Conexion;
import dao.SandwichDao;
import model.Sandwich;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SandwichDaoImpl implements SandwichDao {

    private final Conexion conexion;

    public SandwichDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<Sandwich> listar() {
        return consultar(null, false);
    }

    @Override
    public List<Sandwich> listarActivos() {
        return consultar(null, true);
    }

    private List<Sandwich> consultar(String codigo, boolean soloActivos) {
        List<Sandwich> sandwiches = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID_SAN, CODIGO_SAN, NOMBRE_SAN, PRECIO_SAN, ACTIVO_SAN FROM SANDWICH WHERE 1 = 1");
        if (codigo != null) {
            sql.append(" AND CODIGO_SAN = ?");
        }
        if (soloActivos) {
            sql.append(" AND ACTIVO_SAN = 'A'");
        }
        sql.append(" ORDER BY ID_SAN");

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (codigo != null) {
                ps.setString(1, codigo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sandwiches.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return sandwiches;
    }

    @Override
    public Sandwich buscarPorCodigo(String codigo) {
        List<Sandwich> encontrados = consultar(codigo, false);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    @Override
    public void insertar(Sandwich sandwich) {
        String sql = "INSERT INTO SANDWICH (CODIGO_SAN, NOMBRE_SAN, PRECIO_SAN, ACTIVO_SAN) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_SAN"})) {
            ps.setString(1, sandwich.getCodigoSan());
            ps.setString(2, sandwich.getNombreSan());
            ps.setBigDecimal(3, sandwich.getPrecioSan());
            ps.setString(4, sandwich.getActivoSan());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    sandwich.setIdSan(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Sandwich sandwich) {
        String sql = "UPDATE SANDWICH SET CODIGO_SAN = ?, NOMBRE_SAN = ?, PRECIO_SAN = ?, ACTIVO_SAN = ? WHERE ID_SAN = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sandwich.getCodigoSan());
            ps.setString(2, sandwich.getNombreSan());
            ps.setBigDecimal(3, sandwich.getPrecioSan());
            ps.setString(4, sandwich.getActivoSan());
            ps.setInt(5, sandwich.getIdSan());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM SANDWICH WHERE ID_SAN = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Sandwich mapear(ResultSet rs) throws SQLException {
        Sandwich sandwich = new Sandwich();
        sandwich.setIdSan(rs.getInt("ID_SAN"));
        sandwich.setCodigoSan(rs.getString("CODIGO_SAN"));
        sandwich.setNombreSan(rs.getString("NOMBRE_SAN"));
        sandwich.setPrecioSan(rs.getBigDecimal("PRECIO_SAN"));
        sandwich.setActivoSan(rs.getString("ACTIVO_SAN"));
        return sandwich;
    }
}
