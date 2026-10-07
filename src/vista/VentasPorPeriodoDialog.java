package vista;

import model.reportes.VentaPorPeriodoDTO;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class VentasPorPeriodoDialog extends JDialog {

    private final Window parent;
    private final ReporteService reporteService;

    private final JSpinner spFechaInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner spFechaFin = new JSpinner(new SpinnerDateModel());
    private final JButton btnGenerar = new JButton("Generar Reporte");
    private final JButton btnExportar = FabricaDaisyUI.crearBotonExportarCsv(e -> exportarCsv());
    private final JButton btnCerrar = new JButton("Cerrar");
    private final JLabel lblTotalesAlPie = new JLabel("Total: Q0.00   |   Efectivo: Q0.00   |   Tarjeta: Q0.00");

    private final JTable tabla = new JTable();
    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public VentasPorPeriodoDialog(Window parent, ReporteService reporteService) {
        super(parent, "Ventas por período", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.reporteService = reporteService;
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 600);
        setMinimumSize(new Dimension(860, 520));
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

        String[] columnas = {"Fecha", "No. Pedido", "Cliente", "Método", "Total Pagado (Q)", "Puntos"};
        modeloTabla.setColumnIdentifiers(columnas);
        tabla.setModel(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tabla);
        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(140);
        colModel.getColumn(1).setPreferredWidth(90);
        colModel.getColumn(2).setPreferredWidth(230);
        colModel.getColumn(3).setPreferredWidth(110);
        colModel.getColumn(4).setPreferredWidth(130);
        colModel.getColumn(5).setPreferredWidth(90);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JPanel panelResultado = new JPanel(new BorderLayout(0, 8));
        panelResultado.setOpaque(false);
        panelResultado.add(scrollTabla, BorderLayout.CENTER);

        JPanel panelPieTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        panelPieTotales.setOpaque(false);
        lblTotalesAlPie.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalesAlPie.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");
        panelPieTotales.add(lblTotalesAlPie);
        panelResultado.add(panelPieTotales, BorderLayout.SOUTH);

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccion("Resultado", panelResultado);
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
            List<VentaPorPeriodoDTO> lista = reporteService.listarVentasPorPeriodo(ini, fin);
            modeloTabla.setRowCount(0);
            BigDecimal totalVentas = BigDecimal.ZERO;
            BigDecimal totalEfectivo = BigDecimal.ZERO;
            BigDecimal totalTarjeta = BigDecimal.ZERO;
            BigDecimal totalTransferencia = BigDecimal.ZERO;

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (VentaPorPeriodoDTO v : lista) {
                String fechaTexto = v.getFechaHora() != null ? v.getFechaHora().format(dtf) : (v.getFecha() != null ? v.getFecha().toString() : "");
                String metOriginal = v.getMetodoPago() != null ? v.getMetodoPago().toUpperCase().trim() : "";
                String metodoTexto;
                BigDecimal pag = v.getTotalPagado() != null ? v.getTotalPagado() : BigDecimal.ZERO;
                totalVentas = totalVentas.add(pag);

                if ("E".equals(metOriginal) || "EF".equals(metOriginal) || metOriginal.contains("EFECTIVO")) {
                    metodoTexto = "Efectivo";
                    totalEfectivo = totalEfectivo.add(pag);
                } else if ("TC".equals(metOriginal) || "T".equals(metOriginal) || "TJ".equals(metOriginal) || metOriginal.contains("TARJETA")) {
                    metodoTexto = "Tarjeta";
                    totalTarjeta = totalTarjeta.add(pag);
                } else if ("TF".equals(metOriginal) || "TR".equals(metOriginal) || metOriginal.contains("TRANSFER")) {
                    metodoTexto = "Transferencia";
                    totalTransferencia = totalTransferencia.add(pag);
                } else {
                    metodoTexto = v.getMetodoPago() != null && !v.getMetodoPago().trim().isEmpty() ? v.getMetodoPago() : "Otro";
                    totalEfectivo = totalEfectivo.add(pag);
                }

                modeloTabla.addRow(new Object[]{
                        fechaTexto,
                        "#" + v.getIdPedido(),
                        v.getNombreCliente() != null ? v.getNombreCliente() : "Consumidor Final",
                        metodoTexto,
                        pag.setScale(2, RoundingMode.HALF_UP),
                        v.getPuntos() + " pts"
                });
            }

            StringBuilder sbTotales = new StringBuilder();
            sbTotales.append("Total: Q").append(totalVentas.setScale(2, RoundingMode.HALF_UP));
            sbTotales.append("   |   Efectivo: Q").append(totalEfectivo.setScale(2, RoundingMode.HALF_UP));
            sbTotales.append("   |   Tarjeta: Q").append(totalTarjeta.setScale(2, RoundingMode.HALF_UP));
            if (totalTransferencia.compareTo(BigDecimal.ZERO) > 0) {
                sbTotales.append("   |   Transferencia: Q").append(totalTransferencia.setScale(2, RoundingMode.HALF_UP));
            }
            lblTotalesAlPie.setText(sbTotales.toString());
            FabricaDaisyUI.mostrarToastExito(this, "Reporte generado: " + lista.size() + " registros.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al generar reporte: " + ex.getMessage());
        }
    }

    private void exportarCsv() {
        ExportadorCSV.exportarTabla(this, tabla, "Reporte_Ventas_Por_Periodo");
    }
}