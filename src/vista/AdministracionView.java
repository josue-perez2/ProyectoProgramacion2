package vista;

import javax.swing.*;
import java.awt.*;

public class AdministracionView extends JFrame {

    private final JButton btnIrCategoria = new JButton("Ir");
    private final JButton btnIrProducto = new JButton("Ir");
    private final JButton btnIrSandwich = new JButton("Ir");
    private final JButton btnIrMenu = new JButton("Ir");
    private final JButton btnRegresar = new JButton("Regresar");

    private final Window parent;

    public AdministracionView(Window parent) {
        super("Administracion");
        this.parent = parent;
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelOpciones = new JPanel(new GridLayout(0, 1, 10, 10));
        panelOpciones.setBorder(BorderFactory.createTitledBorder("Opciones de administracion"));

        btnIrCategoria.setPreferredSize(new Dimension(110, 30));
        btnIrProducto.setPreferredSize(new Dimension(110, 30));
        btnIrSandwich.setPreferredSize(new Dimension(110, 30));
        btnIrMenu.setPreferredSize(new Dimension(110, 30));
        btnIrCategoria.addActionListener(e -> abrirCategoria());
        btnIrProducto.addActionListener(e -> abrirProducto());
        btnIrSandwich.addActionListener(e -> abrirSandwich());
        btnIrMenu.addActionListener(e -> abrirMenu());

        panelOpciones.add(crearOpcion("Categoria", btnIrCategoria));
        panelOpciones.add(crearOpcion("Producto", btnIrProducto));
        panelOpciones.add(crearOpcion("Sandwich", btnIrSandwich));
        panelOpciones.add(crearOpcion("Menu", btnIrMenu));

        add(panelOpciones, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnRegresar.setPreferredSize(new Dimension(110, 30));
        btnRegresar.addActionListener(e -> regresar());
        panelBotones.add(btnRegresar);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel crearOpcion(String nombre, JButton boton) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 18));
        etiqueta.setPreferredSize(new Dimension(120, 30));
        panel.add(etiqueta);
        panel.add(boton);
        return panel;
    }

    private void abrirCategoria() {
        CategoriaView ventana = new CategoriaView(this);
        ventana.setVisible(true);
    }

    private void abrirProducto() {
        ProductoView ventana = new ProductoView(this);
        ventana.setVisible(true);
    }

    private void abrirSandwich() {
        SandwichView ventana = new SandwichView(this);
        ventana.setVisible(true);
    }

    private void abrirMenu() {
        MenuView ventana = new MenuView(this);
        ventana.setVisible(true);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
