package vista;

import model.Categorias;
import model.Productos;
import service.CategoriaService;
import service.ProductoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductoView extends JFrame {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    private final Window parent;

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("Regresar");

    private final JTextField txtId = new JTextField();
    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JTextField txtExistencia = new JTextField();
    private final JComboBox<String> cmbCategoria = new JComboBox<>();
    private final JComboBox<String> cmbActivo = new JComboBox<>(new String[]{"A", "I"});

    private final List<Integer> idsCategoria = new ArrayList<>();
    private final Map<Integer, String> nombresCategoria = new HashMap<>();

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

    public ProductoView(Window parent) {
        super("Gestión de Productos");
        this.parent = parent;
        this.productoService = new ProductoService();
        this.categoriaService = new CategoriaService();
        iniciarComponentes();
        cargarCategorias();
        cargarTabla();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 3, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));

        txtId.setEditable(false);
        txtId.setPreferredSize(new Dimension(200, 25));
        txtCodigo.setPreferredSize(new Dimension(200, 25));
        txtNombre.setPreferredSize(new Dimension(200, 25));
        txtPrecio.setText("0.00");
        txtPrecio.setPreferredSize(new Dimension(200, 25));
        txtExistencia.setText("0.00");
        txtExistencia.setPreferredSize(new Dimension(200, 25));

        panelFormulario.add(crearCampo("ID:", txtId));
        panelFormulario.add(crearCampo("Código:", txtCodigo));
        panelFormulario.add(crearCampo("Categoría:", cmbCategoria));
        panelFormulario.add(crearCampo("Nombre:", txtNombre));
        panelFormulario.add(crearCampo("Precio:", txtPrecio));
        panelFormulario.add(crearCampo("Existencia:", txtExistencia));
        panelFormulario.add(crearCampo("Activo:", cmbActivo));

        add(panelFormulario, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAgregar.setPreferredSize(new Dimension(110, 30));
        btnModificar.setPreferredSize(new Dimension(110, 30));
        btnEliminar.setPreferredSize(new Dimension(110, 30));
        btnLimpiar.setPreferredSize(new Dimension(110, 30));
        btnRegresar.setPreferredSize(new Dimension(110, 30));

        btnAgregar.addActionListener(e -> guardarProducto());
        btnModificar.addActionListener(e -> actualizarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRegresar);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Listado de Productos"));

        String[] columnas = {"ID", "Código", "Categoría", "Nombre", "Precio", "Existencia", "Activo"};
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

    private void cargarCategorias() {
        cmbCategoria.removeAllItems();
        idsCategoria.clear();
        nombresCategoria.clear();
        try {
            List<Categorias> categorias = categoriaService.listar();
            for (Categorias c : categorias) {
                cmbCategoria.addItem(c.getNombreCat());
                idsCategoria.add(c.getIdCat());
                nombresCategoria.put(c.getIdCat(), c.getNombreCat());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las categorias: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<Productos> productos = productoService.listar();
            for (Productos p : productos) {
                modeloTabla.addRow(new Object[]{
                        p.getIdPro(),
                        p.getCodigoPro(),
                        nombresCategoria.getOrDefault(p.getIdCatPro(), "Sin categoria"),
                        p.getNombrePro(),
                        p.getPrecioPro(),
                        p.getExistenciaPro(),
                        p.getActivoPro()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de productos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        txtId.setText(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        txtCodigo.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        seleccionarCategoria(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 3)));
        txtPrecio.setText(String.valueOf(modeloTabla.getValueAt(fila, 4)));
        txtExistencia.setText(String.valueOf(modeloTabla.getValueAt(fila, 5)));
        cmbActivo.setSelectedItem(String.valueOf(modeloTabla.getValueAt(fila, 6)));
    }

    private void seleccionarCategoria(String nombre) {
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).equals(nombre)) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    private void guardarProducto() {
        if (!validarFormulario()) {
            return;
        }
        try {
            Productos producto = new Productos();
            producto.setCodigoPro(txtCodigo.getText().trim());
            producto.setIdCatPro(categoriaSeleccionadaId());
            producto.setNombrePro(txtNombre.getText().trim());
            producto.setPrecioPro(leerDecimal(txtPrecio.getText()));
            producto.setExistenciaPro(leerDecimal(txtExistencia.getText()));
            producto.setActivoPro((String) cmbActivo.getSelectedItem());

            productoService.insertar(producto);
            JOptionPane.showMessageDialog(this, "Producto guardado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio y la existencia deben ser números válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProducto() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        try {
            Productos producto = new Productos();
            producto.setIdPro(Integer.parseInt(txtId.getText()));
            producto.setCodigoPro(txtCodigo.getText().trim());
            producto.setIdCatPro(categoriaSeleccionadaId());
            producto.setNombrePro(txtNombre.getText().trim());
            producto.setPrecioPro(leerDecimal(txtPrecio.getText()));
            producto.setExistenciaPro(leerDecimal(txtExistencia.getText()));
            producto.setActivoPro((String) cmbActivo.getSelectedItem());

            productoService.actualizar(producto);
            JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio y la existencia deben ser números válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProducto() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el producto seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                productoService.eliminar(Integer.parseInt(txtId.getText()));
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validarFormulario() {
        if (cmbCategoria.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe haber al menos una categoria registrada para el producto.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtCodigo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El código del producto es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            if (leerDecimal(txtPrecio.getText()).compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (leerDecimal(txtExistencia.getText()).compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "La existencia no puede ser negativa.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio y la existencia deben ser números válidos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private int categoriaSeleccionadaId() {
        return idsCategoria.get(cmbCategoria.getSelectedIndex());
    }

    private BigDecimal leerDecimal(String texto) {
        String valor = texto.trim();
        if (valor.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(valor);
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("0.00");
        txtExistencia.setText("0.00");
        cmbActivo.setSelectedItem("A");
        tabla.clearSelection();
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
