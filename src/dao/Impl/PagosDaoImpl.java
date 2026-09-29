package dao.Impl;

import config.Conexion;
import dao.PagosDao;
import model.Pagos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagosDaoImpl implements PagosDao {

    private final Conexion conexion;

    public PagosDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<Pagos> listar() {
        return consultar(null, null);
    }

    @Override
    public Pagos buscarPorPedido(int idPedPag) {
        List<Pagos> lista = consultar(null, idPedPag);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Pagos buscarPorId(int id) {
        List<Pagos> lista = consultar(id, null);
        return lista.isEmpty() ? null : lista.get(0);
    }

    private List<Pagos> consultar(Integer idPag, Integer idPedPag) {
        List<Pagos> pagos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID_PAG, ID_PED_PAG, FECHA_PAG, METODO_PAGO_PAG, MONTO_RECIBIDO_PAG, CAMBIO_PAG, NUMERO_REFERENCIA_PAG, ESTADO_PAGO_PAG FROM PAGOS WHERE 1 = 1");
        if (idPag != null) {
            sql.append(" AND ID_PAG = ?");
        }
        if (idPedPag != null) {
            sql.append(" AND ID_PED_PAG = ?");
        }
        sql.append(" ORDER BY ID_PAG DESC");

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int indice = 1;
            if (idPag != null) {
                ps.setInt(indice++, idPag);
            }
            if (idPedPag != null) {
                ps.setInt(indice++, idPedPag);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pagos;
    }

    @Override
    public void insertar(Pagos pago) {
        String sql = "INSERT INTO PAGOS (ID_PED_PAG, FECHA_PAG, METODO_PAGO_PAG, MONTO_RECIBIDO_PAG, CAMBIO_PAG, NUMERO_REFERENCIA_PAG, ESTADO_PAGO_PAG) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PAG"})) {
            ps.setInt(1, pago.getIdPedPag());
            LocalDateTime fecha = pago.getFechaPag() != null ? pago.getFechaPag() : LocalDateTime.now();
            pago.setFechaPag(fecha);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            ps.setString(3, pago.getMetodoPagoPag());
            ps.setBigDecimal(4, pago.getMontoRecibidoPag());
            ps.setBigDecimal(5, pago.getCambioPag());
            ps.setString(6, pago.getNumeroReferenciaPag());
            ps.setString(7, pago.getEstadoPagoPag() != null ? pago.getEstadoPagoPag() : "P");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pago.setIdPad(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Pagos pago) {
        String sql = "UPDATE PAGOS SET ID_PED_PAG = ?, FECHA_PAG = ?, METODO_PAGO_PAG = ?, MONTO_RECIBIDO_PAG = ?, CAMBIO_PAG = ?, NUMERO_REFERENCIA_PAG = ?, ESTADO_PAGO_PAG = ? WHERE ID_PAG = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pago.getIdPedPag());
            ps.setTimestamp(2, Timestamp.valueOf(pago.getFechaPag()));
            ps.setString(3, pago.getMetodoPagoPag());
            ps.setBigDecimal(4, pago.getMontoRecibidoPag());
            ps.setBigDecimal(5, pago.getCambioPag());
            ps.setString(6, pago.getNumeroReferenciaPag());
            ps.setString(7, pago.getEstadoPagoPag());
            ps.setInt(8, pago.getIdPad());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM PAGOS WHERE ID_PAG = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Pagos mapear(ResultSet rs) throws SQLException {
        Pagos pagos = new Pagos();
        pagos.setIdPad(rs.getInt("ID_PAG"));
        pagos.setIdPedPag(rs.getInt("ID_PED_PAG"));
        Timestamp fecha = rs.getTimestamp("FECHA_PAG");
        pagos.setFechaPag(fecha != null ? fecha.toLocalDateTime() : null);
        pagos.setMetodoPagoPag(rs.getString("METODO_PAGO_PAG"));
        pagos.setMontoRecibidoPag(rs.getBigDecimal("MONTO_RECIBIDO_PAG"));
        pagos.setCambioPag(rs.getBigDecimal("CAMBIO_PAG"));
        pagos.setNumeroReferenciaPag(rs.getString("NUMERO_REFERENCIA_PAG"));
        pagos.setEstadoPagoPag(rs.getString("ESTADO_PAGO_PAG"));
        return pagos;
    }
}
