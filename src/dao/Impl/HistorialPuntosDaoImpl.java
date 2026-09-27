package dao.Impl;

import config.Conexion;
import dao.HistorialPuntosDao;
import model.Cliente;
import model.DetalleSandwich;
import model.HistorialPuntos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistorialPuntosDaoImpl implements HistorialPuntosDao {

    private final Conexion conexion;

    public  HistorialPuntosDaoImpl(){
        this.conexion = new Conexion();
    }
    @Override
    public List<HistorialPuntos> listar() {
        List<HistorialPuntos> historialPuntos = new ArrayList<>();
        String sql = "SELECT ID_HIS, ID_CLI_HIS, FECHA_HIS, TIPO_OPERACION_HIS, PUNTOS_HIS, REFERENCIA_HIS FROM HISTORIAL_PUNTOS";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                historialPuntos.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return historialPuntos;
    }

    @Override
    public void insertar(HistorialPuntos historialPunto) {
        String sql = "INSERT INTO HISTORIAL_PUNTOS (ID_HIS, ID_CLI_HIS, FECHA_HIS, TIPO_OPERACION_HIS, PUNTOS_HIS, REFERENCIA_HIS) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, historialPunto.getIdHis());
            ps.setInt(2, historialPunto.getIdCliHis());
            ps.setTimestamp(3, Timestamp.valueOf(historialPunto.getFechaHis()));
            ps.setString(4, historialPunto.getTipoOperacionHis());
            ps.setInt(5, historialPunto.getPuntosHis());
            ps.setString(6, historialPunto.getReferenciaHis());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(HistorialPuntos historialPunto) {
        String sql = "UPDATE HISTORIAL_PUNTOS SET  ID_CLI_HIS = ?, FECHA_HIS = ?, TIPO_OPERACION_HIS = ?, PUNTOS_HIS = ?, REFERENCIA_HIS = ? WHERE ID_HIS = ?" ;
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
        historialPuntos.setReferenciaHis(rs.getString("REFERICIA_HIS"));
        return historialPuntos;
    }
}
