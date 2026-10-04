package vista;

import vista.util.FabricaDaisyUI;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import service.ReporteService;
import model.reportes.*;

public class ReportesView extends JFrame {

    private final Window parent;
    private final ReporteService reporteService;

    private final JButton btnVentasPeriodo = new JButton("01. Ventas por período");
    private final JButton btnProductosMasVendidos = new JButton("02. Productos más vendidos");
    private final JButton btnProductosBajoInventario = new JButton("03. Productos agotados o con bajo inventario");
    private final JButton btnPuntosCliente = new JButton("04. Puntos acumulados por cliente");
    private final JButton btnHistorialCanjes = new JButton("05. Historial de canjes");
    private final JButton btnVentasMenus = new JButton("06. Ventas de menús completos");
    private final JButton btnRegresar = new JButton("← Volver");

    public ReportesView(Window parent) {
        super("Reportes");
        this.parent = parent;
        this.reporteService = new ReporteService();
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 560);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(16, 16));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelPrincipal = new JPanel(new BorderLayout(16, 16));
        panelPrincipal.setBorder(new EmptyBorder(22, 28, 22, 28));
        panelPrincipal.setOpaque(false);

        JLabel lblTitulo = new JLabel("Reportes del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelTarjetas = new JPanel(new GridLayout(3, 2, 16, 16));
        panelTarjetas.setOpaque(false);

        FabricaDaisyUI.aplicarBotonPrimario(btnVentasPeriodo);
        FabricaDaisyUI.aplicarBotonSecundario(btnProductosMasVendidos);
        FabricaDaisyUI.aplicarBotonAcento(btnProductosBajoInventario);
        FabricaDaisyUI.aplicarBotonPrimario(btnPuntosCliente);
        FabricaDaisyUI.aplicarBotonSecundario(btnHistorialCanjes);
        FabricaDaisyUI.aplicarBotonAcento(btnVentasMenus);
        FabricaDaisyUI.aplicarBotonNeutral(btnRegresar);

        btnVentasPeriodo.setPreferredSize(new Dimension(320, 42));
        btnProductosMasVendidos.setPreferredSize(new Dimension(320, 42));
        btnProductosBajoInventario.setPreferredSize(new Dimension(320, 42));
        btnPuntosCliente.setPreferredSize(new Dimension(320, 42));
        btnHistorialCanjes.setPreferredSize(new Dimension(320, 42));
        btnVentasMenus.setPreferredSize(new Dimension(320, 42));
        btnRegresar.setPreferredSize(new Dimension(150, 38));

        btnVentasPeriodo.addActionListener(e -> abrirVentasPorPeriodo());
        btnProductosMasVendidos.addActionListener(e -> abrirProductosMasVendidos());
        btnProductosBajoInventario.addActionListener(e -> abrirProductosBajoInventario());
        btnPuntosCliente.addActionListener(e -> abrirPuntosPorCliente());
        btnHistorialCanjes.addActionListener(e -> abrirHistorialCanjes());
        btnVentasMenus.addActionListener(e -> abrirVentasMenusCompletos());
        btnRegresar.addActionListener(e -> regresar());

        panelTarjetas.add(crearTarjetaModulo("01. Ventas por período", btnVentasPeriodo));
        panelTarjetas.add(crearTarjetaModulo("02. Productos más vendidos", btnProductosMasVendidos));
        panelTarjetas.add(crearTarjetaModulo("03. Productos agotados o con bajo inventario", btnProductosBajoInventario));
        panelTarjetas.add(crearTarjetaModulo("04. Puntos acumulados por cliente", btnPuntosCliente));
        panelTarjetas.add(crearTarjetaModulo("05. Historial de canjes", btnHistorialCanjes));
        panelTarjetas.add(crearTarjetaModulo("06. Ventas de menús completos", btnVentasMenus));

        panelPrincipal.add(panelTarjetas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelInferior.setOpaque(false);
        panelInferior.add(btnRegresar);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaModulo(String titulo, JButton botonAccion) {
        FabricaDaisyUI.PanelTarjeta tarjeta = new FabricaDaisyUI.PanelTarjeta(new BorderLayout(8, 14));
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTit.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");
        tarjeta.add(lblTit, BorderLayout.NORTH);
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelBoton.setOpaque(false);
        panelBoton.add(botonAccion);
        tarjeta.add(panelBoton, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void abrirVentasPorPeriodo() {
        VentasPorPeriodoDialog dialog = new VentasPorPeriodoDialog(this, reporteService);
        dialog.setVisible(true);
    }

    private void abrirProductosMasVendidos() {
        ProductosMasVendidosDialog dialog = new ProductosMasVendidosDialog(this, reporteService);
        dialog.setVisible(true);
    }

    private void abrirProductosBajoInventario() {
        ProductosBajoInventarioDialog dialog = new ProductosBajoInventarioDialog(this, reporteService);
        dialog.setVisible(true);
    }

    private void abrirPuntosPorCliente() {
        PuntosPorClienteDialog dialog = new PuntosPorClienteDialog(this, reporteService);
        dialog.setVisible(true);
    }

    private void abrirHistorialCanjes() {
        HistorialCanjesDialog dialog = new HistorialCanjesDialog(this, reporteService);
        dialog.setVisible(true);
    }

    private void abrirVentasMenusCompletos() {
        VentasMenusCompletosDialog dialog = new VentasMenusCompletosDialog(this, reporteService);
        dialog.setVisible(true);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}