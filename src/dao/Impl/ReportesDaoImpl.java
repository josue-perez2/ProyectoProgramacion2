package dao.Impl;

import config.Conexion;
import dao.ReportesDao;
import model.reportes.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportesDaoImpl implements ReportesDao {

    private final Conexion conexion;

    public ReportesDaoImpl() {
        this.conexion = new Conexion();
    }

    @Override
    public List<VentaPorPeriodoDTO> listarVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<VentaPorPeriodoDTO> lista = new ArrayList<>();
        String sql = "SELECT p.FECHA_PAG AS FECHA, " +
                "e.ID_PED AS ID_PEDIDO, " +
                "NVL(c.NOMBRE_CLI, 'Consumidor Final') AS NOMBRE_CLIENTE, " +
                "p.METODO_PAGO_PAG AS METODO_PAGO, " +
                "(p.MONTO_RECIBIDO_PAG - NVL(p.CAMBIO_PAG, 0)) AS TOTAL_PAGADO, " +
                "NVL(e.PUNTOS_OBTENIDOS_PED, 0) AS PUNTOS " +
                "FROM PAGOS p " +
                "JOIN PEDIDOS e ON e.ID_PED = p.ID_PED_PAG " +
                "LEFT JOIN CLIENTES c ON c.ID_CLI = e.ID_CLI_PED " +
                "WHERE p.ESTADO_PAGO_PAG IN ('P', 'C') " +
                "AND p.FECHA_PAG >= ? AND p.FECHA_PAG < ? + INTERVAL '1' DAY " +
                "ORDER BY p.FECHA_PAG DESC, e.ID_PED DESC";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaPorPeriodoDTO dto = new VentaPorPeriodoDTO();
                    Timestamp ts = rs.getTimestamp("FECHA");
                    if (ts != null) {
                        dto.setFecha(ts.toLocalDateTime().toLocalDate());
                        dto.setFechaHora(ts.toLocalDateTime());
                    }
                    dto.setIdPedido(rs.getInt("ID_PEDIDO"));
                    dto.setNombreCliente(rs.getString("NOMBRE_CLIENTE"));
                    dto.setMetodoPago(rs.getString("METODO_PAGO"));
                    dto.setTotalPagado(rs.getBigDecimal("TOTAL_PAGADO"));
                    dto.setPuntos(rs.getInt("PUNTOS"));
                    dto.setTotalVenta(dto.getTotalPagado());
                    String metDto = dto.getMetodoPago() != null ? dto.getMetodoPago().toUpperCase().trim() : "";
                    if ("E".equals(metDto) || "EF".equals(metDto) || metDto.contains("EFECTIVO")) {
                        dto.setTotalEfectivo(dto.getTotalPagado());
                        dto.setTotalTarjeta(BigDecimal.ZERO);
                    } else {
                        dto.setTotalEfectivo(BigDecimal.ZERO);
                        dto.setTotalTarjeta(dto.getTotalPagado());
                    }
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public List<ProductoVendidoDTO> listarProductosMasVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<ProductoVendidoDTO> lista = new ArrayList<>();
        String sql = "SELECT dp.ID_ITEM_DET AS ID_ITEM, dp.TIPO_ITEM_DET AS TIPO_ITEM, " +
                "NVL(pr.NOMBRE_PRO, NVL(s.NOMBRE_SAN, m.NOMBRE_MEN)) AS NOMBRE_ITEM, " +
                "SUM(dp.CANTIDAD_DET) AS CANTIDAD_VENDIDA, " +
                "DECODE(dp.TIPO_ITEM_DET, 'P', 'PRODUCTO', 'S', 'SANDWICH', 'M', 'MENU') AS ORIGEN " +
                "FROM DETALLES_PEDIDO dp " +
                "JOIN PEDIDOS ped ON ped.ID_PED = dp.ID_PED_DET " +
                "JOIN PAGOS pag ON pag.ID_PED_PAG = ped.ID_PED " +
                "LEFT JOIN PRODUCTOS pr ON dp.TIPO_ITEM_DET = 'P' AND pr.ID_PRO = dp.ID_ITEM_DET " +
                "LEFT JOIN SANDWICH s ON dp.TIPO_ITEM_DET = 'S' AND s.ID_SAN = dp.ID_ITEM_DET " +
                "LEFT JOIN MENUS m ON dp.TIPO_ITEM_DET = 'M' AND m.ID_MEN = dp.ID_ITEM_DET " +
                "WHERE pag.ESTADO_PAGO_PAG IN ('P', 'C') " +
                "AND pag.FECHA_PAG >= ? AND pag.FECHA_PAG < ? + INTERVAL '1' DAY " +
                "GROUP BY dp.ID_ITEM_DET, dp.TIPO_ITEM_DET, pr.NOMBRE_PRO, s.NOMBRE_SAN, m.NOMBRE_MEN " +
                "ORDER BY CANTIDAD_VENDIDA DESC, NOMBRE_ITEM";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoVendidoDTO dto = new ProductoVendidoDTO();
                    dto.setIdItem(rs.getInt("ID_ITEM"));
                    dto.setTipoItem(rs.getString("TIPO_ITEM"));
                    dto.setNombreItem(rs.getString("NOMBRE_ITEM"));
                    dto.setCantidadVendida(rs.getBigDecimal("CANTIDAD_VENDIDA"));
                    dto.setOrigen(rs.getString("ORIGEN"));
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public List<ProductoInventarioDTO> listarProductosBajoInventario() {
        List<ProductoInventarioDTO> lista = new ArrayList<>();
        String sql = "SELECT ID_PRO, CODIGO_PRO, NOMBRE_PRO, EXISTENCIA_PRO, " +
                "CASE WHEN EXISTENCIA_PRO <= 0 THEN 'AGOTADO' WHEN EXISTENCIA_PRO <= 5 THEN 'BAJO INVENTARIO' ELSE 'OK' END AS ESTADO_INVENTARIO " +
                "FROM PRODUCTOS " +
                "WHERE ACTIVO_PRO = 'A' AND EXISTENCIA_PRO <= 5 " +
                "ORDER BY EXISTENCIA_PRO ASC, NOMBRE_PRO";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductoInventarioDTO dto = new ProductoInventarioDTO();
                dto.setIdPro(rs.getInt("ID_PRO"));
                dto.setCodigoPro(rs.getString("CODIGO_PRO"));
                dto.setNombrePro(rs.getString("NOMBRE_PRO"));
                dto.setExistenciaPro(rs.getBigDecimal("EXISTENCIA_PRO"));
                dto.setEstadoInventario(rs.getString("ESTADO_INVENTARIO"));
                lista.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public List<PuntosClienteDTO> listarPuntosPorCliente() {
        List<PuntosClienteDTO> lista = new ArrayList<>();
        String sql = "SELECT ID_CLI, NOMBRE_CLI, DPI_CLI, SALDO_PUNTO_CLI " +
                "FROM CLIENTES " +
                "WHERE ESTADO_CLI = 'A' OR ESTADO_CLI = '1' " +
                "ORDER BY SALDO_PUNTO_CLI DESC, NOMBRE_CLI";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PuntosClienteDTO dto = new PuntosClienteDTO();
                dto.setIdCli(rs.getInt("ID_CLI"));
                dto.setNombreCli(rs.getString("NOMBRE_CLI"));
                dto.setDpiCli(rs.getString("DPI_CLI"));
                dto.setSaldoPuntoCli(rs.getInt("SALDO_PUNTO_CLI"));
                lista.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public List<HistorialCanjeDTO> listarHistorialCanjes(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<HistorialCanjeDTO> lista = new ArrayList<>();
        String sql = "SELECT c.ID_CAN, c.FECHA_CAN, cl.NOMBRE_CLI, r.NOMBRE_REC, c.PUNTOS_RECOMPENSA_CAN " +
                "FROM CANJES c " +
                "JOIN CLIENTES cl ON cl.ID_CLI = c.ID_CLI_CAN " +
                "JOIN RECOMPENSAS r ON r.ID_REC = c.ID_REC_CAN " +
                "WHERE c.FECHA_CAN >= ? AND c.FECHA_CAN < ? + INTERVAL '1' DAY " +
                "ORDER BY c.FECHA_CAN DESC";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialCanjeDTO dto = new HistorialCanjeDTO();
                    dto.setIdCan(rs.getInt("ID_CAN"));
                    if (rs.getTimestamp("FECHA_CAN") != null) {
                        dto.setFechaCan(rs.getTimestamp("FECHA_CAN").toLocalDateTime());
                    }
                    dto.setNombreCliente(rs.getString("NOMBRE_CLI"));
                    dto.setNombreRecompensa(rs.getString("NOMBRE_REC"));
                    dto.setPuntosUtilizados(rs.getInt("PUNTOS_RECOMPENSA_CAN"));
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public List<MenuVendidoDTO> listarVentasMenusCompletos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<MenuVendidoDTO> lista = new ArrayList<>();
        String sql = "SELECT m.ID_MEN, m.CODIGO_MEN, m.NOMBRE_MEN, " +
                "SUM(d.CANTIDAD_DET) AS CANTIDAD_VENDIDA, " +
                "SUM(d.SUBTOTAL_DET) AS TOTAL_GENERADO " +
                "FROM DETALLES_PEDIDO d " +
                "JOIN MENUS m ON m.ID_MEN = d.ID_ITEM_DET " +
                "JOIN PEDIDOS p ON p.ID_PED = d.ID_PED_DET " +
                "JOIN PAGOS pg ON pg.ID_PED_PAG = p.ID_PED " +
                "WHERE d.TIPO_ITEM_DET = 'M' AND pg.ESTADO_PAGO_PAG IN ('P', 'C') " +
                "AND pg.FECHA_PAG >= ? AND pg.FECHA_PAG < ? + INTERVAL '1' DAY " +
                "GROUP BY m.ID_MEN, m.CODIGO_MEN, m.NOMBRE_MEN " +
                "ORDER BY TOTAL_GENERADO DESC";
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            ps.setTimestamp(2, Timestamp.valueOf(fechaFin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MenuVendidoDTO dto = new MenuVendidoDTO();
                    dto.setIdMen(rs.getInt("ID_MEN"));
                    dto.setCodigoMen(rs.getString("CODIGO_MEN"));
                    dto.setNombreMen(rs.getString("NOMBRE_MEN"));
                    dto.setCantidadVendida(rs.getBigDecimal("CANTIDAD_VENDIDA"));
                    dto.setTotalGenerado(rs.getBigDecimal("TOTAL_GENERADO"));
                    lista.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }
}