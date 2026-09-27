package vista;

import model.DetalleSandwich;
import model.Productos;
import model.Sandwich;
import service.DetalleSandwichService;
import service.ProductoService;
import service.SandwichService;
import util.CategoriasItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SandwichView extends JFrame {

    private final SandwichService sandwichService;
    private final DetalleSandwichService detalleSandwichService;
    private final ProductoService productoService;

    private final Window parent;

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("Regresar");

    private final JButton btnAgregarDetalle = new JButton("Agregar Detalle");
    private final JButton btnQuitarDetalle = new JButton("Quitar Detalle");

    private final JTextField txtId = new JTextField();
    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JComboBox<String> cmbActivo = new JComboBox<>(new String[]{"A", "I"});

    private final JComboBox<String> cmbProducto = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField();
    private final JComboBox<String> cmbObligatorio = new JComboBox<>(new String[]{"N", "S"});

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

    private final JTable tablaDetalle = new JTable() {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Seleccione un sandwich para ver su detalle";
                int x = (getWidth() - fm.stringWidth(mensaje)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(mensaje, x, y);
            }
        }
    };
    private final DefaultTableModel modeloDetalle = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private List<Integer> idsProductoPan = new ArrayList<>();

    public SandwichView(Window parent) {
        super("Gestión de Sandwich");
        this.parent = parent;
        this.sandwichService = new SandwichService();
        this.detalleSandwichService = new DetalleSandwichService();
        this.productoService = new ProductoService();
        iniciarComponentes();
        cargarProductos();
        cargarTabla();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 5, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Sandwich"));

        txtId.setEditable(false);
        txtId.setPreferredSize(new Dimension(120, 25));
        txtCodigo.setPreferredSize(new Dimension(120, 25));
        txtNombre.setPreferredSize(new Dimension(160, 25));
        txtPrecio.setText("0.00");
        txtPrecio.setPreferredSize(new Dimension(120, 25));

        panelFormulario.add(crearCampo("ID:", txtId));
        panelFormulario.add(crearCampo("Código:", txtCodigo));
        panelFormulario.add(crearCampo("Nombre:", txtNombre));
        panelFormulario.add(crearCampo("Precio:", txtPrecio));
        panelFormulario.add(crearCampo("Activo:", cmbActivo));

        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.add(panelFormulario, BorderLayout.CENTER);

        JPanel panelDetalleForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelDetalleForm.setBorder(BorderFactory.createTitledBorder("Detalle del Sandwich (productos de categoria Pan)"));
        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(80, 25));
        cmbProducto.setPreferredSize(new Dimension(220, 25));
        btnAgregarDetalle.setPreferredSize(new Dimension(140, 30));
        btnQuitarDetalle.setPreferredSize(new Dimension(140, 30));

        btnAgregarDetalle.addActionListener(e -> agregarDetalle());
        btnQuitarDetalle.addActionListener(e -> quitarDetalle());

        panelDetalleForm.add(new JLabel("Producto:"));
        panelDetalleForm.add(cmbProducto);
        panelDetalleForm.add(new JLabel("Cantidad:"));
        panelDetalleForm.add(txtCantidad);
        panelDetalleForm.add(new JLabel("Obligatorio:"));
        panelDetalleForm.add(cmbObligatorio);
        panelDetalleForm.add(btnAgregarDetalle);
        panelDetalleForm.add(btnQuitarDetalle);

        panelSuperior.add(panelDetalleForm, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 10, 10));
        panelTablas.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Listado de Sandwich"));

        modeloTabla.setColumnIdentifiers(new String[]{"ID", "Código", "Nombre", "Precio", "Activo"});
        tabla.setModel(modeloTabla);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });
        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelTablaDetalle = new JPanel(new BorderLayout());
        panelTablaDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del Sandwich seleccionado"));

        modeloDetalle.setColumnIdentifiers(new String[]{"ID", "ID Sandwich", "ID Producto", "Producto", "Cantidad", "Obligatorio"});
        tablaDetalle.setModel(modeloDetalle);
        tablaDetalle.setFillsViewportHeight(true);
        tablaDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelTablaDetalle.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);

        panelTablas.add(panelTabla);
        panelTablas.add(panelTablaDetalle);

        add(panelTablas, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAgregar.setPreferredSize(new Dimension(110, 30));
        btnModificar.setPreferredSize(new Dimension(110, 30));
        btnEliminar.setPreferredSize(new Dimension(110, 30));
        btnLimpiar.setPreferredSize(new Dimension(110, 30));
        btnRegresar.setPreferredSize(new Dimension(110, 30));

        btnAgregar.addActionListener(e -> guardarSandwich());
        btnModificar.addActionListener(e -> actualizarSandwich());
        btnEliminar.addActionListener(e -> eliminarSandwich());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRegresar);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel crearCampo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    private void cargarProductos() {
        cmbProducto.removeAllItems();
        idsProductoPan = new ArrayList<>();
        try {
            List<Productos> productos = productoService.listarActivosPorCategoria(CategoriasItem.PAN);
            for (Productos p : productos) {
                cmbProducto.addItem(p.getNombrePro() + " (" + p.getCodigoPro() + ")");
                idsProductoPan.add(p.getIdPro());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los productos de categoria Pan: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            for (Sandwich s : sandwichService.listar()) {
                modeloTabla.addRow(new Object[]{
                        s.getIdSan(),
                        s.getCodigoSan(),
                        s.getNombreSan(),
                        s.getPrecioSan(),
                        s.getActivoSan()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de sandwich: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalle(int idSan) {
        modeloDetalle.setRowCount(0);
        try {
            for (DetalleSandwich d : detalleSandwichService.listarPorSandwich(idSan)) {
                modeloDetalle.addRow(new Object[]{
                        d.getIdDetSan(),
                        d.getIdSanDet(),
                        d.getIdProDet(),
                        nombreProducto(d.getIdProDet()),
                        d.getCantidadDet(),
                        d.getObligatorioDet()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el detalle: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String nombreProducto(int idPro) {
        for (int i = 0; i < cmbProducto.getItemCount(); i++) {
            if (idsProductoPan.get(i) == idPro) {
                return cmbProducto.getItemAt(i);
            }
        }
        return "Producto " + idPro;
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        txtId.setText(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        txtCodigo.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        txtPrecio.setText(String.valueOf(modeloTabla.getValueAt(fila, 3)));
        cmbActivo.setSelectedItem(String.valueOf(modeloTabla.getValueAt(fila, 4)));
        cargarDetalle(Integer.parseInt(String.valueOf(modeloTabla.getValueAt(fila, 0))));
    }

    private void guardarSandwich() {
        if (!validarFormulario()) {
            return;
        }
        try {
            Sandwich sandwich = new Sandwich();
            sandwich.setCodigoSan(txtCodigo.getText().trim());
            sandwich.setNombreSan(txtNombre.getText().trim());
            sandwich.setPrecioSan(leerDecimal(txtPrecio.getText()));
            sandwich.setActivoSan((String) cmbActivo.getSelectedItem());

            sandwichService.insertar(sandwich);
            Sandwich guardado = sandwichService.buscarPorCodigo(txtCodigo.getText().trim());

            JOptionPane.showMessageDialog(this, "Sandwich guardado correctamente.");
            cargarTabla();
            limpiarFormulario();

            if (guardado != null) {
                seleccionarEnTabla(guardado.getIdSan());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarSandwich() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un sandwich de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        try {
            Sandwich sandwich = new Sandwich();
            sandwich.setIdSan(Integer.parseInt(txtId.getText()));
            sandwich.setCodigoSan(txtCodigo.getText().trim());
            sandwich.setNombreSan(txtNombre.getText().trim());
            sandwich.setPrecioSan(leerDecimal(txtPrecio.getText()));
            sandwich.setActivoSan((String) cmbActivo.getSelectedItem());

            sandwichService.actualizar(sandwich);
            JOptionPane.showMessageDialog(this, "Sandwich actualizado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSandwich() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un sandwich de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el sandwich seleccionado? Se eliminará su detalle.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                sandwichService.eliminar(Integer.parseInt(txtId.getText()));
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarDetalle() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero debe guardar el sandwich para poder agregarle detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cmbProducto.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe existir al menos un producto activo de categoria Pan.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            DetalleSandwich detalle = new DetalleSandwich();
            detalle.setIdSanDet(Integer.parseInt(txtId.getText()));
            detalle.setIdProDet(idsProductoPan.get(cmbProducto.getSelectedIndex()));
            detalle.setCantidadDet(leerDecimal(txtCantidad.getText()));
            detalle.setObligatorioDet((String) cmbObligatorio.getSelectedItem());

            detalleSandwichService.insertar(detalle);
            cargarDetalle(Integer.parseInt(txtId.getText()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar el detalle: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarDetalle() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila del detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            detalleSandwichService.eliminar((int) modeloDetalle.getValueAt(fila, 0));
            cargarDetalle(Integer.parseInt(txtId.getText()));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al quitar el detalle: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarEnTabla(int idSan) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if ((int) modeloTabla.getValueAt(i, 0) == idSan) {
                tabla.setRowSelectionInterval(i, i);
                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El código del sandwich es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del sandwich es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            if (leerDecimal(txtPrecio.getText()).compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
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
        cmbActivo.setSelectedItem("A");
        txtCantidad.setText("1");
        cmbObligatorio.setSelectedItem("N");
        tabla.clearSelection();
        modeloDetalle.setRowCount(0);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
