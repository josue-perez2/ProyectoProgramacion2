package dao.Impl;

import dao.CanjesDao;
import model.Canjes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class CanjesDaoImpl implements CanjesDao {
    @Override
    public List<Canjes> listar() {
        return List.of();
    }

    @Override
    public void insertar(Canjes canjes) {

    }

    @Override
    public void actualizar(Canjes canjes) {

    }

    @Override
    public void eliminar(int id) {

    }


    private Canjes mapear(ResultSet rs) throws SQLException {
        Canjes canjes = new Canjes();
        canjes.setIdCan(rs.getInt("ID_CAN"));
        canjes.setIdCliCan(rs.getInt("ID_CLI_CAN"));
        canjes.setIdRecCan(rs.getInt("ID_REC_CAN"));
        Timestamp fecha = rs.getTimestamp("FECHA_CAN");
        canjes.setFechaCan(fecha != null ? fecha.toLocalDateTime() : null);
        canjes.setPuntosRecompensaCan(rs.getInt("PUNTOS_RECOMPENSA_CAN"));
        return canjes;
    }
}
