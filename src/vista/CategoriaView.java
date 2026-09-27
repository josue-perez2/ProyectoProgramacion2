package vista;

import model.Categorias;
import service.CategoriaService;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class CategoriaView extends JFrame {
    private final CategoriaService categoriaService;

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("Regresar");

    private final Window parent;

    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();


    private final JTable tabla = new JTable() {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Sin datos registrados";
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

    public CategoriaView() {
        this(null);
    }

    public CategoriaView(Window parent) {
        super("Gestión de Categorias");
        this.parent = parent;
        this.categoriaService = new CategoriaService();
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 3, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos de la categoria"));

        txtId.setEditable(false);
        txtId.setPreferredSize(new Dimension(200, 25));

        txtNombre.setPreferredSize(new Dimension(200, 25));


        panelFormulario.add(crearCampo("ID:", txtId));
        panelFormulario.add(crearCampo("Nombre:", txtNombre));


        add(panelFormulario, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAgregar.setPreferredSize(new Dimension(110, 30));
        btnModificar.setPreferredSize(new Dimension(110, 30));
        btnEliminar.setPreferredSize(new Dimension(110, 30));
        btnLimpiar.setPreferredSize(new Dimension(110, 30));
        btnRegresar.setPreferredSize(new Dimension(110, 30));

        btnAgregar.addActionListener(e -> guardarCategoria());
        btnModificar.addActionListener(e -> actualizarCategoria());
        btnEliminar.addActionListener(e -> eliminarCategoria());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRegresar);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Listado de categorias"));

        String[] columnas = {"ID", "Nombre"};
        modeloTabla.setColumnIdentifiers(columnas);
        tabla.setModel(modeloTabla);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panelCentral.add(panelBotones, BorderLayout.NORTH);
        panelCentral.add(panelTabla, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);
    }

    private JPanel crearCampo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Categorias> categorias = categoriaService.listar();
        for (Categorias c : categorias) {
            modeloTabla.addRow(new Object[]{
                    c.getIdCat(),
                    c.getNombreCat()
            });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        txtId.setText(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));

    }

    private void guardarCategoria() {

        try {
            Categorias categoria = new Categorias();
            categoria.setNombreCat(txtNombre.getText().trim());

            categoriaService.insertar(categoria);
            JOptionPane.showMessageDialog(this, "Categoria guardado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCategoria() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoria de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Categorias categoria = new Categorias();
            categoria.setIdCat(Integer.parseInt(txtId.getText()));
            categoria.setNombreCat(txtNombre.getText().trim());

            categoriaService.actualizar(categoria);
            JOptionPane.showMessageDialog(this, "Categoria actualizado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCategoria() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoria de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar la categoria seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                categoriaService.eliminar(Integer.parseInt(txtId.getText()));
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }





    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        tabla.clearSelection();
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }

}
