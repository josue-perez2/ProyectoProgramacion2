package vista;

import vista.util.FabricaDaisyUI;
import vista.util.GestorVentanas;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdministracionView extends JFrame {

    private final JButton btnIrCategoria = new JButton("Gestionar Categorías", Icons.menu(16));
    private final JButton btnIrProducto = new JButton("Gestionar Productos", Icons.shoppingBag(16));
    private final JButton btnIrSandwich = new JButton("Gestionar Sándwiches", Icons.utensils(16));
    private final JButton btnIrMenu = new JButton("Gestionar Combos", Icons.menu(16));
    private final JButton btnIrReportes = new JButton("Reportes", Icons.receipt(16));
    private final JButton btnIrInventario = new JButton("Control de Inventario", Icons.refreshCw(16));
    private final JButton btnRegresar = new JButton("Volver al Inicio", Icons.arrowLeft(16));

    private final Window parent;

    public AdministracionView(Window parent) {
        super("Administración");
        this.parent = parent;
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(880, 520);
        setMinimumSize(new Dimension(820, 480));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(16, 16));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelPrincipal = new JPanel(new BorderLayout(16, 16));
        panelPrincipal.setBorder(new EmptyBorder(22, 28, 22, 28));
        panelPrincipal.setOpaque(false);

        JLabel lblTitulo = new JLabel("Administración del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelTarjetas = new JPanel(new GridLayout(3, 2, 16, 16));
        panelTarjetas.setOpaque(false);

        FabricaDaisyUI.aplicarBotonSecundario(btnIrCategoria);
        FabricaDaisyUI.aplicarBotonPrimario(btnIrProducto);
        FabricaDaisyUI.aplicarBotonAcento(btnIrSandwich);
        FabricaDaisyUI.aplicarBotonPrimario(btnIrMenu);
        FabricaDaisyUI.aplicarBotonAcento(btnIrReportes);
        FabricaDaisyUI.aplicarBotonSecundario(btnIrInventario);
        FabricaDaisyUI.aplicarBotonNeutral(btnRegresar);

        btnIrCategoria.setPreferredSize(new Dimension(195, 38));
        btnIrProducto.setPreferredSize(new Dimension(195, 38));
        btnIrSandwich.setPreferredSize(new Dimension(195, 38));
        btnIrMenu.setPreferredSize(new Dimension(195, 38));
        btnIrReportes.setPreferredSize(new Dimension(195, 38));
        btnIrInventario.setPreferredSize(new Dimension(195, 38));
        btnRegresar.setPreferredSize(new Dimension(175, 38));

        btnIrCategoria.setIconTextGap(8);
        btnIrProducto.setIconTextGap(8);
        btnIrSandwich.setIconTextGap(8);
        btnIrMenu.setIconTextGap(8);
        btnIrReportes.setIconTextGap(8);
        btnIrInventario.setIconTextGap(8);
        btnRegresar.setIconTextGap(8);

        btnIrCategoria.addActionListener(e -> abrirCategoria());
        btnIrProducto.addActionListener(e -> abrirProducto());
        btnIrSandwich.addActionListener(e -> abrirSandwich());
        btnIrMenu.addActionListener(e -> abrirMenu());
        btnIrReportes.addActionListener(e -> abrirReportes());
        btnIrInventario.addActionListener(e -> abrirInventario());
        btnRegresar.addActionListener(e -> regresar());

        panelTarjetas.add(crearTarjetaModulo("Categorías", btnIrCategoria));
        panelTarjetas.add(crearTarjetaModulo("Productos", btnIrProducto));
        panelTarjetas.add(crearTarjetaModulo("Sándwiches", btnIrSandwich));
        panelTarjetas.add(crearTarjetaModulo("Combos", btnIrMenu));
        panelTarjetas.add(crearTarjetaModulo("Reportes", btnIrReportes));
        panelTarjetas.add(crearTarjetaModulo("Inventario", btnIrInventario));

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
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTit.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");

        tarjeta.add(lblTit, BorderLayout.NORTH);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelBoton.setOpaque(false);
        panelBoton.add(botonAccion);
        tarjeta.add(panelBoton, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void abrirCategoria() {
        GestorVentanas.abrirOEnfocar(CategoriaView.class, () -> new CategoriaView(this));
    }

    private void abrirProducto() {
        GestorVentanas.abrirOEnfocar(ProductoView.class, () -> new ProductoView(this));
    }

    private void abrirSandwich() {
        GestorVentanas.abrirOEnfocar(SandwichView.class, () -> new SandwichView(this));
    }

    private void abrirMenu() {
        GestorVentanas.abrirOEnfocar(MenuView.class, () -> new MenuView(this));
    }

    private void abrirReportes() {
        GestorVentanas.abrirOEnfocar(ReportesView.class, () -> new ReportesView(this));
    }

    private void abrirInventario() {
        GestorVentanas.abrirOEnfocar(InventarioView.class, () -> new InventarioView(this));
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
