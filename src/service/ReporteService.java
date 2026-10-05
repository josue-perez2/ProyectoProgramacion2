package service;

import dao.ReportesDao;
import dao.Impl.ReportesDaoImpl;
import model.reportes.*;

import java.time.LocalDateTime;
import java.util.List;

public class ReporteService {
    private final ReportesDao reportesDao;

    public ReporteService() {
        this.reportesDao = new ReportesDaoImpl();
    }

    public List<VentaPorPeriodoDTO> listarVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reportesDao.listarVentasPorPeriodo(fechaInicio, fechaFin);
    }

    public List<ProductoVendidoDTO> listarProductosMasVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reportesDao.listarProductosMasVendidos(fechaInicio, fechaFin);
    }

    public List<ProductoInventarioDTO> listarProductosBajoInventario() {
        return reportesDao.listarProductosBajoInventario();
    }

    public List<PuntosClienteDTO> listarPuntosPorCliente() {
        return reportesDao.listarPuntosPorCliente();
    }

    public List<HistorialCanjeDTO> listarHistorialCanjes(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reportesDao.listarHistorialCanjes(fechaInicio, fechaFin);
    }

    public List<MenuVendidoDTO> listarVentasMenusCompletos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reportesDao.listarVentasMenusCompletos(fechaInicio, fechaFin);
    }
}