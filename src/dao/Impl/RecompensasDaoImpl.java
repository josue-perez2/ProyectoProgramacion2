package dao.Impl;

import dao.RecompesasDao;
import model.Recompensas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RecompensasDaoImpl implements RecompesasDao {
    @Override
    public List<Recompensas> listar() {
        return List.of();
    }

    @Override
    public void insertar(Recompensas cliente) {

    }

    @Override
    public void actualizar(Recompensas cliente) {

    }

    @Override
    public void eliminar(int id) {

    }

    private Recompensas mapear(ResultSet rs) throws SQLException {
        Recompensas recompensas = new Recompensas();
        recompensas.setIdRec(rs.getInt("ID_REC"));
        recompensas.setNombreRec(rs.getString("NOMBRE_REC"));
        recompensas.setPuntosRequeridosRec(rs.getInt("PUNTOS_REQUERIDOS_REC"));
        recompensas.setTipoItemRec(rs.getString("TIPO_ITEM_REC"));
        recompensas.setIdItemRec(rs.getInt("ID_ITEM_REC"));
        recompensas.setActivoRec(rs.getString("ACTIVO_REC"));

        return recompensas;
    }
}
