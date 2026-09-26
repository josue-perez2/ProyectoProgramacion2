package dao.Impl;

import dao.CategoriaDao;
import model.Categorias;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class CategoriasDaoImpl implements CategoriaDao {
    @Override
    public List<Categorias> listar() {
        return List.of();
    }

    @Override
    public void insertar(Categorias cliente) {

    }

    @Override
    public void actualizar(Categorias cliente) {

    }

    @Override
    public void eliminar(int id) {

    }
    private Categorias mapear(ResultSet rs) throws SQLException {
        Categorias categorias = new Categorias();
        categorias.setIdCat(rs.getInt("ID_CAT"));
        categorias.setNombreCat(rs.getString("NOMBRE_CAT"));
        return categorias;
    }
}
