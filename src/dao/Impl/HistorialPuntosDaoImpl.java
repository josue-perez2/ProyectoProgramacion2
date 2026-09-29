package dao.Impl;

import config.Conexion;
import dao.HistorialPuntosDao;
import model.HistorialPuntos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistorialPuntosDaoImpl implements HistorialPuntosDao {

    private final Conexion conexion;

    public HistorialPuntosDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<HistorialPuntos> listar() {
        return consultar(null);
    }

    @Override
    public List<HistorialPuntos> listarPorCliente(int idCliHis) {
        return consultar(idCliHis);
    }

    private List<HistorialPuntos> consultar(Integer idCliHis) {
        List<HistorialPuntos> historialPuntos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID_HIS, ID_CLI_HIS, FECHA_HIS, TIPO_OPERACION_HIS, PUNTOS_HIS, REFERENCIA_HIS FROM HISTORIAL_PUNTOS WHERE 1 = 1");
        if (idCliHis != null) {
            sql.append(" AND ID_CLI_HIS = ?");
        }
        sql.append(" ORDER BY ID_HIS DESC");

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (idCliHis != null) {
                ps.setInt(1, idCliHis);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historialPuntos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return historialPuntos;
    }

    @Override
    public void insertar(HistorialPuntos historialPunto) {
        String sql = "INSERT INTO HISTORIAL_PUNTOS (ID_CLI_HIS, FECHA_HIS, TIPO_OPERACION_HIS, PUNTOS_HIS, REFERENCIA_HIS) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_HIS"})) {
            ps.setInt(1, historialPunto.getIdCliHis());
            LocalDateTime fecha = historialPunto.getFechaHis() != null ? historialPunto.getFechaHis() : LocalDateTime.now();
            historialPunto.setFechaHis(fecha);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            ps.setString(3, historialPunto.getTipoOperacionHis());
            ps.setInt(4, historialPunto.getPuntosHis());
            ps.setString(5, historialPunto.getReferenciaHis());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    historialPunto.setIdHis(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(HistorialPuntos historialPunto) {
        String sql = "UPDATE HISTORIAL_PUNTOS SET ID_CLI_HIS = ?, FECHA_HIS = ?, TIPO_OPERACION_HIS = ?, PUNTOS_HIS = ?, REFERENCIA_HIS = ? WHERE ID_HIS = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, historialPunto.getIdCliHis());
            ps.setTimestamp(2, Timestamp.valueOf(historialPunto.getFechaHis()));
            ps.setString(3, historialPunto.getTipoOperacionHis());
            ps.setInt(4, historialPunto.getPuntosHis());
            ps.setString(5, historialPunto.getReferenciaHis());
            ps.setInt(6, historialPunto.getIdHis());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM HISTORIAL_PUNTOS WHERE ID_HIS = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private HistorialPuntos mapear(ResultSet rs) throws SQLException {
        HistorialPuntos historialPuntos = new HistorialPuntos();
        historialPuntos.setIdHis(rs.getInt("ID_HIS"));
        historialPuntos.setIdCliHis(rs.getInt("ID_CLI_HIS"));
        Timestamp fecha = rs.getTimestamp("FECHA_HIS");
        historialPuntos.setFechaHis(fecha != null ? fecha.toLocalDateTime() : null);
        historialPuntos.setTipoOperacionHis(rs.getString("TIPO_OPERACION_HIS"));
        historialPuntos.setPuntosHis(rs.getInt("PUNTOS_HIS"));
        historialPuntos.setReferenciaHis(rs.getString("REFERENCIA_HIS"));
        return historialPuntos;
    }
}
