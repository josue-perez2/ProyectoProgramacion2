package dao.Impl;

import dao.ProductosDao;
import model.Productos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ProductosDaoImpl implements ProductosDao {
    @Override
    public List<Productos> listar() {
        return List.of();
    }

    @Override
    public void insertar(Productos cliente) {

    }

    @Override
    public void actualizar(Productos cliente) {

    }

    @Override
    public void eliminar(int id) {

    }
    private Productos mapear(ResultSet rs) throws SQLException {
        Productos productos = new Productos();
        productos.setIdPro(rs.getInt("ID_PRO"));
        productos.setCodigoPro(rs.getString("CODIGO_PRO"));
        productos.setIdCatPro(rs.getInt("ID_CAT_PRO"));
        productos.setNombrePro(rs.getString("NOMBRE_PRO"));
        productos.setPrecioPro(rs.getBigDecimal("PRECIO_PRO"));
        productos.setExistenciaPro(rs.getBigDecimal("EXISTENCIA_PRO"));
        productos.setActivoPro(rs.getString("ACTIVO_PRO"));
        return productos;
    }
}
