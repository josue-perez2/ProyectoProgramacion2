package dao.Impl;

import dao.MenusDao;
import model.Menus;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MenusDaoImpl implements MenusDao {
    @Override
    public List<Menus> listar() {
        return List.of();
    }

    @Override
    public void insertar(Menus cliente) {

    }

    @Override
    public void actualizar(Menus cliente) {

    }

    @Override
    public void eliminar(int id) {

    }
    private Menus mapear(ResultSet rs) throws SQLException {
        Menus detalleSandwich = new Menus();
        detalleSandwich.setIdMen(rs.getInt("ID_MEN"));
        detalleSandwich.setCodigoMen(rs.getString("CODIGO_MEN"));
        detalleSandwich.setNombreMen(rs.getString("NOMBRE_MEN"));
        detalleSandwich.setPrecioMen(rs.getBigDecimal("PRECIO_MEN"));
        detalleSandwich.setActivoMen(rs.getString("ACTIVO_MEN"));
        return detalleSandwich;
    }
}
