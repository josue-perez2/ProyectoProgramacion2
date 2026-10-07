package vista;

import model.reportes.ProductoVendidoDTO;
import service.ReporteService;
import util.ExportadorCSV;
import vista.util.FabricaDaisyUI;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ProductosMasVendidosDialog extends JDialog {

    private final Window parent;
    private final ReporteService reporteService;

    private final JSpinner spFechaInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner spFechaFin = new JSpinner(new SpinnerDateModel());
    private final JButton btnGenerar = new JButton("Generar Reporte");
    private final JButton btnExportar = FabricaDaisyUI.crearBotonExportarCsv(e -> exportarCsv());
    private final JButton btnCerrar = new JButton("Cerrar");

    private final JTable tabla = new JTable();
    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ProductosMasVendidosDialog(Window parent, ReporteService reporteService) {
        super(parent, "Productos más vendidos", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.reporteService = reporteService;
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(860, 560);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedor = new JPanel(new BorderLayout(12, 12));
        panelContenedor.setBorder(new EmptyBorder(14, 18, 14, 18));
        panelContenedor.setOpaque(false);

        spFechaInicio.setEditor(new JSpinner.DateEditor(spFechaInicio, "dd/MM/yyyy"));
        spFechaFin.setEditor(new JSpinner.DateEditor(spFechaFin, "dd/MM/yyyy"));
        spFechaInicio.setPreferredSize(new Dimension(140, 36));
        spFechaFin.setPreferredSize(new Dimension(140, 36));
        FabricaDaisyUI.estilizarSpinner(spFechaInicio);
        FabricaDaisyUI.estilizarSpinner(spFechaFin);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        panelFiltros.setOpaque(false);
        panelFiltros.add(FabricaDaisyUI.crearCampoConEtiqueta("Fecha Inicio:", spFechaInicio));
        panelFiltros.add(FabricaDaisyUI.crearCampoConEtiqueta("Fecha Fin:", spFechaFin));

        btnGenerar.setPreferredSize(new Dimension(160, 38));
        btnExportar.setPreferredSize(new Dimension(150, 38));
        btnCerrar.setPreferredSize(new Dimension(120, 38));
        FabricaDaisyUI.aplicarBotonPrimario(btnGenerar);
        FabricaDaisyUI.aplicarBotonNeutral(btnCerrar);
        btnGenerar.addActionListener(e -> generarReporte());
        btnCerrar.addActionListener(e -> dispose());

        JPanel panelBotonesFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panelBotonesFiltros.setOpaque(false);
        panelBotonesFiltros.add(btnGenerar);
        panelBotonesFiltros.add(btnExportar);
        panelBotonesFiltros.add(btnCerrar);

        JPanel panelCabecera = new JPanel(new BorderLayout(10, 4));
        panelCabecera.setOpaque(false);
        panelCabecera.add(panelFiltros, BorderLayout.CENTER);
        panelCabecera.add(panelBotonesFiltros, BorderLayout.SOUTH);

        JPanel tarjetaFiltros = FabricaDaisyUI.crearTarjetaSeccion("Filtros", panelCabecera);
        panelContenedor.add(tarjetaFiltros, BorderLayout.NORTH);

        String[] columnas = {"ID Item", "Tipo", "Nombre", "Cantidad Vendida", "Origen"};
        modeloTabla.setColumnIdentifiers(columnas);
        tabla.setModel(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tabla);
        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(80);
        colModel.getColumn(1).setPreferredWidth(100);
        colModel.getColumn(2).setPreferredWidth(320);
        colModel.getColumn(3).setPreferredWidth(150);
        colModel.getColumn(4).setPreferredWidth(130);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccion("Resultado", scrollTabla);
        panelContenedor.add(tarjetaTabla, BorderLayout.CENTER);

        add(panelContenedor, BorderLayout.CENTER);
    }

    private void generarReporte() {
        Date fechaIniD = (Date) spFechaInicio.getValue();
        Date fechaFinD = (Date) spFechaFin.getValue();
        if (fechaIniD == null || fechaFinD == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "Seleccione ambas fechas.");
            return;
        }
        if (fechaFinD.before(fechaIniD)) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "La fecha fin no puede ser anterior a la fecha inicio.");
            return;
        }
        LocalDateTime ini = fechaIniD.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = fechaFinD.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withHour(23).withMinute(59).withSecond(59);
        try {
            List<ProductoVendidoDTO> lista = reporteService.listarProductosMasVendidos(ini, fin);
            modeloTabla.setRowCount(0);
            for (ProductoVendidoDTO p : lista) {
                modeloTabla.addRow(new Object[]{
                        p.getIdItem(),
                        p.getTipoItem(),
                        p.getNombreItem(),
                        p.getCantidadVendida() != null ? p.getCantidadVendida() : BigDecimal.ZERO,
                        p.getOrigen()
                });
            }
            FabricaDaisyUI.mostrarToastExito(this, "Reporte generado: " + lista.size() + " items.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al generar reporte: " + ex.getMessage());
        }
    }

    private void exportarCsv() {
        ExportadorCSV.exportarTabla(this, tabla, "Reporte_Productos_Mas_Vendidos");
    }
}