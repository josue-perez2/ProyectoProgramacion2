package dao.Impl;

import config.Conexion;
import dao.InventarioMovimientosDao;
import model.InventarioMovimientos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventarioMovimientosDaoImpl implements InventarioMovimientosDao {

    private final Conexion conexion;
    private static volatile Boolean soporteColumnasAuditoria = null;

    static {
        asegurarColumnasAuditoria();
    }

    private static void asegurarColumnasAuditoria() {
        Conexion con = new Conexion();
        try (Connection conn = con.conectar();
             Statement st = conn.createStatement()) {
            try {
                st.executeUpdate("ALTER TABLE INVENTARIO_MOVIMIENTOS MODIFY (TIPO_MOVIMIENTO_IMO VARCHAR2(100))");
            } catch (Exception ignored) {
            }
            st.executeUpdate("ALTER TABLE INVENTARIO_MOVIMIENTOS ADD (EXISTENCIA_ANTERIOR_IMO NUMBER(10,2) DEFAULT 0, EXISTENCIA_NUEVA_IMO NUMBER(10,2) DEFAULT 0)");
            soporteColumnasAuditoria = true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1430) {
                soporteColumnasAuditoria = true;
            }
        } catch (Exception ignored) {
        }
    }

    public InventarioMovimientosDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<InventarioMovimientos> listar() {
        List<InventarioMovimientos> lista = new ArrayList<>();
        String sqlCompleto = "SELECT ID_IMO, ID_PRO_IMO, FECHA_IMO, CANTIDAD_IMO, TIPO_MOVIMIENTO_IMO, EXISTENCIA_ANTERIOR_IMO, EXISTENCIA_NUEVA_IMO FROM INVENTARIO_MOVIMIENTOS ORDER BY ID_IMO DESC";
        String sqlBasico = "SELECT ID_IMO, ID_PRO_IMO, FECHA_IMO, CANTIDAD_IMO, TIPO_MOVIMIENTO_IMO FROM INVENTARIO_MOVIMIENTOS ORDER BY ID_IMO DESC";

        boolean usarAuditoria = soporteColumnasAuditoria == null || soporteColumnasAuditoria;
        String sql = usarAuditoria ? sqlCompleto : sqlBasico;

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            soporteColumnasAuditoria = usarAuditoria;
            while (rs.next()) {
                lista.add(mapear(rs, usarAuditoria));
            }
            return lista;
        } catch (SQLException e) {
            if (usarAuditoria && e.getErrorCode() == 904) {
                soporteColumnasAuditoria = false;
                try (Connection conn = conexion.conectar();
                     PreparedStatement ps = conn.prepareStatement(sqlBasico);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(rs, false));
                    }
                    return lista;
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertar(InventarioMovimientos inventarioMovimiento) {
        LocalDateTime fecha = inventarioMovimiento.getFechaImo() != null ? inventarioMovimiento.getFechaImo() : LocalDateTime.now();
        inventarioMovimiento.setFechaImo(fecha);

        boolean usarAuditoria = soporteColumnasAuditoria == null || soporteColumnasAuditoria;
        if (usarAuditoria) {
            String sqlCompleto = "INSERT INTO INVENTARIO_MOVIMIENTOS (ID_PRO_IMO, FECHA_IMO, CANTIDAD_IMO, TIPO_MOVIMIENTO_IMO, EXISTENCIA_ANTERIOR_IMO, EXISTENCIA_NUEVA_IMO) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = conexion.conectar();
                 PreparedStatement ps = conn.prepareStatement(sqlCompleto, new String[]{"ID_IMO"})) {
                ps.setInt(1, inventarioMovimiento.getIdProImo());
                ps.setTimestamp(2, Timestamp.valueOf(fecha));
                ps.setBigDecimal(3, inventarioMovimiento.getCantidadImo());
                ps.setString(4, inventarioMovimiento.getTipoMovimientoImo());
                ps.setBigDecimal(5, inventarioMovimiento.getExistenciaAnteriorImo());
                ps.setBigDecimal(6, inventarioMovimiento.getExistenciaNuevaImo());
                ps.executeUpdate();
                soporteColumnasAuditoria = true;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        inventarioMovimiento.setIdImo(rs.getInt(1));
                    }
                }
                return;
            } catch (SQLException e) {
                if (e.getErrorCode() == 904) {
                    soporteColumnasAuditoria = false;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        String sqlBasico = "INSERT INTO INVENTARIO_MOVIMIENTOS (ID_PRO_IMO, FECHA_IMO, CANTIDAD_IMO, TIPO_MOVIMIENTO_IMO) VALUES (?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sqlBasico, new String[]{"ID_IMO"})) {
            ps.setInt(1, inventarioMovimiento.getIdProImo());
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

    private InventarioMovimientos mapear(ResultSet rs, boolean conAuditoria) throws SQLException {
        InventarioMovimientos inventario = new InventarioMovimientos();
        inventario.setIdImo(rs.getInt("ID_IMO"));
        inventario.setIdProImo(rs.getInt("ID_PRO_IMO"));
        Timestamp fecha = rs.getTimestamp("FECHA_IMO");
        inventario.setFechaImo(fecha != null ? fecha.toLocalDateTime() : null);
        inventario.setCantidadImo(rs.getBigDecimal("CANTIDAD_IMO"));
        inventario.setTipoMovimientoImo(rs.getString("TIPO_MOVIMIENTO_IMO"));
        if (conAuditoria) {
            try {
                inventario.setExistenciaAnteriorImo(rs.getBigDecimal("EXISTENCIA_ANTERIOR_IMO"));
                inventario.setExistenciaNuevaImo(rs.getBigDecimal("EXISTENCIA_NUEVA_IMO"));
            } catch (SQLException ignored) {
            }
        }
        return inventario;
    }
}
