package dao.Impl;

import dao.DetalleMenuDao;
import model.DetalleMenu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DetalleMenuDaoImpl implements DetalleMenuDao {
    @Override
    public List<DetalleMenu> listar() {
        return List.of();
    }

    @Override
    public void insertar(DetalleMenu detalleMenu) {

    }

    @Override
    public void actualizar(DetalleMenu detalleMenu) {

    }

    @Override
    public void eliminar(int id) {

    }
    private DetalleMenu mapear(ResultSet rs) throws SQLException {
        DetalleMenu detalleMenu = new DetalleMenu();
        detalleMenu.setIdDetMen(rs.getInt("ID_DET_MEN"));
        detalleMenu.setIdMenDet(rs.getInt("ID_MEN_DET"));
        detalleMenu.setTipoItemDet(rs.getString("TIPO_ITEM_DET"));
        detalleMenu.setIdItemDet(rs.getInt("ID_ITEM_DET"));
        detalleMenu.setCantidadDet(rs.getBigDecimal("CANTIDAD_DET"));
        return detalleMenu;
    }
}
