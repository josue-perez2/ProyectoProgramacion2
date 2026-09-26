package dao.Impl;

import dao.PagosDao;

import model.Pagos;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class PagosDaoImpl implements PagosDao {
    @Override
    public List<Pagos> listar() {
        return List.of();
    }

    @Override
    public void insertar(Pagos cliente) {

    }

    @Override
    public void actualizar(Pagos cliente) {

    }

    @Override
    public void eliminar(int id) {

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
