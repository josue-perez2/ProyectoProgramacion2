package dao.Impl;

import dao.HistorialPuntosDao;
import model.DetalleSandwich;
import model.HistorialPuntos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class HistorialPuntosDaoImpl implements HistorialPuntosDao {
    @Override
    public List<HistorialPuntos> listar() {
        return List.of();
    }

    @Override
    public void insertar(HistorialPuntos cliente) {

    }

    @Override
    public void actualizar(HistorialPuntos cliente) {

    }

    @Override
    public void eliminar(int id) {

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
