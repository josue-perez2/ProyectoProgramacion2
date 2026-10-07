package dao.Impl;

import config.Conexion;
import dao.DetalleSandwichDao;
import model.Cliente;
import model.DetalleSandwich;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DetalleSandwichDaoImpl implements DetalleSandwichDao {
    private final Conexion conexion;

    static {
        asegurarRecetasBase();
    }

    private static void asegurarRecetasBase() {
        Conexion con = new Conexion();
        try (Connection conn = con.conectar();
             Statement st = conn.createStatement()) {
            int conteo = 0;
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM DETALLE_SANDWICH")) {
                if (rs.next()) {
                    conteo = rs.getInt(1);
                }
            }
            if (conteo == 0) {
                st.executeUpdate("INSERT INTO DETALLE_SANDWICH (ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) VALUES (1, 3, 1, 'S')");
                st.executeUpdate("INSERT INTO DETALLE_SANDWICH (ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) VALUES (1, 7, 1, 'S')");
                st.executeUpdate("INSERT INTO DETALLE_SANDWICH (ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) VALUES (1, 8, 1, 'S')");
                st.executeUpdate("INSERT INTO DETALLE_SANDWICH (ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) VALUES (2, 4, 1, 'S')");
                st.executeUpdate("INSERT INTO DETALLE_SANDWICH (ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) VALUES (2, 7, 2, 'S')");
                st.executeUpdate("INSERT INTO DETALLE_SANDWICH (ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) VALUES (2, 8, 1, 'S')");
            }
        } catch (Exception ignored) {
        }
    }

    public DetalleSandwichDaoImpl() {
        this.conexion = new Conexion();
    }
    @Override
    public List<DetalleSandwich> listar() {
        return consultar(null);
    }

    @Override
    public List<DetalleSandwich> listarPorSandwich(int idSanDet) {
        return consultar(idSanDet);
    }

    private List<DetalleSandwich> consultar(Integer idSanDet) {
        List<DetalleSandwich> detalleSandwiches = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID_DET_SAN, ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET FROM DETALLE_SANDWICH WHERE 1 = 1");
        if (idSanDet != null) {
            sql.append(" AND ID_SAN_DET = ?");
        }
        sql.append(" ORDER BY ID_DET_SAN");

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (idSanDet != null) {
                ps.setInt(1, idSanDet);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalleSandwiches.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return detalleSandwiches;
    }

    @Override
    public void insertar(DetalleSandwich detalleSandwich) {
        String sql = "INSERT INTO DETALLE_SANDWICH (ID_SAN_DET, ID_PRO_DET, CANTIDAD_DET, OBLIGATORIO_DET) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalleSandwich.getIdSanDet());
            ps.setInt(2, detalleSandwich.getIdProDet());
            ps.setBigDecimal(3, detalleSandwich.getCantidadDet());
            ps.setString(4, detalleSandwich.getObligatorioDet());
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
        String sql = "DELETE FROM DETALLE_SANDWICH WHERE ID_DET_SAN = ?";
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
