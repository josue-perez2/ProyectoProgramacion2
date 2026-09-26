package dao.Impl;

import dao.DetalleSandwichDao;
import model.DetalleSandwich;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DetalleSandwichDaoImpl implements DetalleSandwichDao {
    @Override
    public List<DetalleSandwichDao> listar() {
        return List.of();
    }

    @Override
    public void insertar(DetalleSandwichDao cliente) {

    }

    @Override
    public void actualizar(DetalleSandwichDao cliente) {

    }

    @Override
    public void eliminar(int id) {

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
