package vista;

import model.reportes.ProductoInventarioDTO;
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
import java.util.List;

public class ProductosBajoInventarioDialog extends JDialog {

    private final Window parent;
    private final ReporteService reporteService;

    private final JButton btnGenerar = new JButton("Generar Reporte");
    private final JButton btnExportar = FabricaDaisyUI.crearBotonExportarCsv(e -> exportarCsv());
    private final JButton btnCerrar = new JButton("Cerrar");

    private final JTable tabla = new JTable() {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(TemaGestor.esModoOscuro() ? new Color(98, 114, 164) : Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Sin productos con bajo inventario o agotados";
                int x = (getWidth() - fm.stringWidth(mensaje)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(mensaje, x, y);
            }
        }
    };
    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ProductosBajoInventarioDialog(Window parent, ReporteService reporteService) {
        super(parent, "Productos agotados o con bajo inventario", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.reporteService = reporteService;
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 520);
        setMinimumSize(new Dimension(740, 460));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedor = new JPanel(new BorderLayout(12, 12));
        panelContenedor.setBorder(new EmptyBorder(14, 18, 14, 18));
        panelContenedor.setOpaque(false);

        btnGenerar.setPreferredSize(new Dimension(160, 38));
        btnExportar.setPreferredSize(new Dimension(150, 38));
        btnCerrar.setPreferredSize(new Dimension(120, 38));
        FabricaDaisyUI.aplicarBotonPrimario(btnGenerar);
        FabricaDaisyUI.aplicarBotonNeutral(btnCerrar);
        btnGenerar.addActionListener(e -> generarReporte());
        btnCerrar.addActionListener(e -> dispose());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panelBotones.setOpaque(false);
        panelBotones.add(btnGenerar);
        panelBotones.add(btnExportar);
        panelBotones.add(btnCerrar);

        JPanel tarjetaFiltros = FabricaDaisyUI.crearTarjetaSeccion("Acciones", panelBotones);
        panelContenedor.add(tarjetaFiltros, BorderLayout.NORTH);

        String[] columnas = {"ID", "Código", "Nombre", "Existencia", "Estado"};
        modeloTabla.setColumnIdentifiers(columnas);
        tabla.setModel(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());
        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(60);
        colModel.getColumn(1).setPreferredWidth(110);
        colModel.getColumn(2).setPreferredWidth(340);
        colModel.getColumn(3).setPreferredWidth(110);
        colModel.getColumn(4).setPreferredWidth(150);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccion("Resultado", scrollTabla);
        panelContenedor.add(tarjetaTabla, BorderLayout.CENTER);

        add(panelContenedor, BorderLayout.CENTER);
    }

    private void generarReporte() {
        try {
            List<ProductoInventarioDTO> lista = reporteService.listarProductosBajoInventario();
            modeloTabla.setRowCount(0);
            for (ProductoInventarioDTO p : lista) {
                modeloTabla.addRow(new Object[]{
                        p.getIdPro(),
                        p.getCodigoPro(),
                        p.getNombrePro(),
                        p.getExistenciaPro() != null ? p.getExistenciaPro() : BigDecimal.ZERO,
                        p.getEstadoInventario() != null ? p.getEstadoInventario().toUpperCase() : ""
                });
            }
            FabricaDaisyUI.mostrarToastExito(this, "Reporte generado: " + lista.size() + " productos críticos.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al generar reporte: " + ex.getMessage());
        }
    }

    private void exportarCsv() {
        ExportadorCSV.exportarTabla(this, tabla, "Reporte_Productos_Bajo_Inventario");
    }
}