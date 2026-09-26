package dao.Impl;

import dao.InventarioMovimientosDao;
import model.InventarioMovimientos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class InventarioMovimientosDaoImpl implements InventarioMovimientosDao {
    @Override
    public List<InventarioMovimientos> listar() {
        return List.of();
    }

    @Override
    public void insertar(InventarioMovimientos cliente) {

    }

    @Override
    public void actualizar(InventarioMovimientos cliente) {

    }

    @Override
    public void eliminar(int id) {

    }
    private InventarioMovimientos mapear(ResultSet rs) throws SQLException {
        InventarioMovimientos inventarioSandwich = new InventarioMovimientos();
        inventarioSandwich.setIdImo(rs.getInt("ID_IMO"));
        inventarioSandwich.setIdProImo(rs.getInt("ID_PRO_IMO"));
        Timestamp fecha = rs.getTimestamp("FECHA_IMO");
        inventarioSandwich.setFechaImo(fecha != null ? fecha.toLocalDateTime() : null);
        inventarioSandwich.setCantidadImo(rs.getBigDecimal("CANTIDAD_IMO"));
        inventarioSandwich.setTipoMovimientoImo(rs.getString("TIPO_MOVIMIENTO_IMO"));
        return inventarioSandwich;
    }
}
