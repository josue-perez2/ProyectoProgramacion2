package dao.Impl;

import config.Conexion;
import dao.CategoriaDao;
import model.Categorias;
import model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriasDaoImpl implements CategoriaDao {
    private final Conexion conexion;

    public CategoriasDaoImpl() {
        this.conexion = new Conexion();
    }
    @Override
    public List<Categorias> listar() {
        List<Categorias> categorias = new ArrayList<>();
        String sql = "SELECT ID_CAT,NOMBRE_CAT FROM CATEGORIAS";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categorias;
    }

    @Override
    public void insertar(Categorias categoria) {
        String sql = "INSERT INTO CATEGORIAS (NOMBRE_CAT) " +
                "VALUES (?)";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombreCat());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void actualizar(Categorias categoria) {
        String sql = "UPDATE CATEGORIAS SET  NOMBRE_CAT = ? WHERE  ID_CAT = ? " ;
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombreCat());
            ps.setInt(2, categoria.getIdCat());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM CATEGORIAS WHERE ID_CAT = ?";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private Categorias mapear(ResultSet rs) throws SQLException {
        Categorias categorias = new Categorias();
        categorias.setIdCat(rs.getInt("ID_CAT"));
        categorias.setNombreCat(rs.getString("NOMBRE_CAT"));
        return categorias;
    }
}
