package dao;

import model.reportes.*;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportesDao {
    List<VentaPorPeriodoDTO> listarVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<ProductoVendidoDTO> listarProductosMasVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<ProductoInventarioDTO> listarProductosBajoInventario();
    List<PuntosClienteDTO> listarPuntosPorCliente();
    List<HistorialCanjeDTO> listarHistorialCanjes(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<MenuVendidoDTO> listarVentasMenusCompletos(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}