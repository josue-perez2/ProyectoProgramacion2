package dao.Impl;

import dao.SandwichDao;
import model.Sandwich;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SandwichDaoImpl implements SandwichDao {
    @Override
    public List<Sandwich> listar() {
        return List.of();
    }

    @Override
    public void insertar(Sandwich cliente) {

    }

    @Override
    public void actualizar(Sandwich cliente) {

    }

    @Override
    public void eliminar(int id) {

    }

    private Sandwich mapear(ResultSet rs) throws SQLException {
        Sandwich sandwich = new Sandwich();
        sandwich.setIdSan(rs.getInt("ID_SAN"));
        sandwich.setCodigoSan(rs.getString("CODIGO_SAN"));
        sandwich.setNombreSan(rs.getString("NOMBRE_SAN"));
        sandwich.setPrecioSan(rs.getBigDecimal("PRECIO_SAN"));
        sandwich.setActivoSan(rs.getString("ACTIVO_SAN"));
        return sandwich;
    }
}
