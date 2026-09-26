package dao.Impl;

import dao.DetallesPedidoDao;
import model.DetallesPedido;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DetallePedidoDaoImpl implements DetallesPedidoDao {
    @Override
    public List<DetallesPedido> listar() {
        return List.of();
    }

    @Override
    public void insertar(DetallesPedido detallesPedido) {

    }

    @Override
    public void actualizar(DetallesPedido detallesPedido) {

    }

    @Override
    public void eliminar(int id) {

    }

    private DetallesPedido mapear(ResultSet rs) throws SQLException {
        DetallesPedido detallesPedido = new DetallesPedido();
        detallesPedido.setIdDet(rs.getInt("ID_DET"));
        detallesPedido.setIdPedDet(rs.getInt("ID_PED_DET"));
        detallesPedido.setTipoItemDet(rs.getString("TIPO_ITEM_DET"));
        detallesPedido.setIdItemDet(rs.getInt("ID_ITEM_DET"));
        detallesPedido.setCantidadDet(rs.getBigDecimal("CANTIDAD_DET"));
        detallesPedido.setPrecioUnitarioDet(rs.getBigDecimal("PRECIO_UNITARIO_DET"));
        detallesPedido.setSubTotalDet(rs.getBigDecimal("SUBTOTAL_DET"));
        detallesPedido.setPuntosGeneradosDet(rs.getInt("PUNTOS_GENERADOS_DET"));
        return detallesPedido;
    }
}
