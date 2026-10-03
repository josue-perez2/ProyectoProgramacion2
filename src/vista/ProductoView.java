package vista;

import model.Categorias;
import model.Productos;
import service.CategoriaService;
import service.ProductoService;
import vista.util.FabricaDaisyUI;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
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
    private Integer idProductoSeleccionado = null;

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("← Volver");

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JTextField txtExistencia = new JTextField();
    private final JComboBox<String> cmbCategoria = new JComboBox<>();
    private final JComboBox<String> cmbActivo = new JComboBox<>(new String[]{"Activo", "Inactivo"});

    private final List<Integer> idsCategoria = new ArrayList<>();
    private final Map<Integer, String> nombresCategoria = new HashMap<>();

    private final JTable tabla = new JTable() {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(TemaGestor.esModoOscuro() ? new Color(98, 114, 164) : Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Sin productos registrados";
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
        actualizarEstadoBotones(false);
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1140, 740);
        setMinimumSize(new Dimension(1060, 660));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedorPrincipal = new JPanel(new BorderLayout(12, 12));
        panelContenedorPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        panelContenedorPrincipal.setOpaque(false);

        JPanel panelCamposFormulario = new JPanel(new GridLayout(2, 3, 14, 10));
        panelCamposFormulario.setOpaque(false);

        txtCodigo.setPreferredSize(new Dimension(140, 36));
        txtNombre.setPreferredSize(new Dimension(220, 36));
        txtPrecio.setText("0.00");
        txtPrecio.setPreferredSize(new Dimension(140, 36));
        txtExistencia.setText("0.00");
        txtExistencia.setPreferredSize(new Dimension(140, 36));
        cmbCategoria.setPreferredSize(new Dimension(200, 36));
        cmbActivo.setPreferredSize(new Dimension(140, 36));

        FabricaDaisyUI.estilizarCampo(txtCodigo);
        FabricaDaisyUI.estilizarCampo(txtNombre);
        FabricaDaisyUI.estilizarCampo(txtPrecio);
        FabricaDaisyUI.estilizarCampo(txtExistencia);
        FabricaDaisyUI.estilizarCampo(cmbCategoria);
        FabricaDaisyUI.estilizarCampo(cmbActivo);

        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Código:", txtCodigo));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Categoría:", cmbCategoria));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Estado:", cmbActivo));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Nombre:", txtNombre));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Precio Unitario (Q):", txtPrecio));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Existencia:", txtExistencia));

        JPanel tarjetaFormulario = FabricaDaisyUI.crearTarjetaSeccion(
                "Datos del Producto",
                panelCamposFormulario
        );

        panelContenedorPrincipal.add(tarjetaFormulario, BorderLayout.NORTH);

        String[] columnas = {"ID", "Código", "Categoría", "Nombre", "Precio", "Existencia", "Estado"};
        modeloTabla.setColumnIdentifiers(columnas);
        tabla.setModel(modeloTabla);
        FabricaDaisyUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(6).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());

        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(45);
        colModel.getColumn(0).setMaxWidth(60);
        colModel.getColumn(1).setPreferredWidth(90);
        colModel.getColumn(2).setPreferredWidth(140);
        colModel.getColumn(3).setPreferredWidth(210);
        colModel.getColumn(4).setPreferredWidth(90);
        colModel.getColumn(5).setPreferredWidth(90);
        colModel.getColumn(6).setPreferredWidth(95);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> {
            cargarCategorias();
            cargarTabla();
        });

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Productos Registrados",
                btnRefrescar,
                scrollTabla
        );
        panelContenedorPrincipal.add(tarjetaTabla, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(4, 16, 14, 16));
        panelBotones.setOpaque(false);

        btnAgregar.setPreferredSize(new Dimension(135, 38));
        btnModificar.setPreferredSize(new Dimension(135, 38));
        btnEliminar.setPreferredSize(new Dimension(135, 38));
        btnLimpiar.setPreferredSize(new Dimension(130, 38));
        btnRegresar.setPreferredSize(new Dimension(130, 38));

        FabricaDaisyUI.aplicarBotonPrimario(btnAgregar);
        FabricaDaisyUI.aplicarBotonSecundario(btnModificar);
        FabricaDaisyUI.aplicarBotonPeligro(btnEliminar);
        FabricaDaisyUI.aplicarBotonNeutral(btnLimpiar);
        FabricaDaisyUI.aplicarBotonNeutral(btnRegresar);

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

        add(panelContenedorPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void actualizarEstadoBotones(boolean seleccionActiva) {
        btnAgregar.setEnabled(!seleccionActiva);
        btnModificar.setEnabled(seleccionActiva);
        btnEliminar.setEnabled(seleccionActiva);
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
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las categorías: " + ex.getMessage(),
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
                        nombresCategoria.getOrDefault(p.getIdCatPro(), "Sin categoría"),
                        p.getNombrePro(),
                        p.getPrecioPro(),
                        p.getExistenciaPro(),
                        "A".equalsIgnoreCase(p.getActivoPro()) ? "ACTIVO" : "INACTIVO"
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
            actualizarEstadoBotones(false);
            return;
        }
        idProductoSeleccionado = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        txtCodigo.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        seleccionarCategoria(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 3)));
        txtPrecio.setText(String.valueOf(modeloTabla.getValueAt(fila, 4)));
        txtExistencia.setText(String.valueOf(modeloTabla.getValueAt(fila, 5)));
        String activoFila = String.valueOf(modeloTabla.getValueAt(fila, 6));
        cmbActivo.setSelectedItem("ACTIVO".equalsIgnoreCase(activoFila) ? "Activo" : "Inactivo");
        actualizarEstadoBotones(true);
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
        String codigo = txtCodigo.getText().trim();
        for (Productos p : productoService.listar()) {
            if (p.getCodigoPro().equalsIgnoreCase(codigo)) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe un producto con el código '" + codigo + "'. Use el botón 'Modificar' para actualizar el registro existente.",
                        "Código Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        try {
            Productos producto = new Productos();
            producto.setCodigoPro(codigo);
            producto.setIdCatPro(categoriaSeleccionadaId());
            producto.setNombrePro(txtNombre.getText().trim());
            producto.setPrecioPro(leerDecimal(txtPrecio.getText()));
            producto.setExistenciaPro(leerDecimal(txtExistencia.getText()));
            producto.setActivoPro("Activo".equals(cmbActivo.getSelectedItem()) ? "A" : "I");

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
        if (idProductoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        String codigo = txtCodigo.getText().trim();
        for (Productos p : productoService.listar()) {
            if (p.getCodigoPro().equalsIgnoreCase(codigo) && p.getIdPro() != idProductoSeleccionado) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe otro producto con el código '" + codigo + "'. Ingrese un código único.",
                        "Código Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        try {
            Productos producto = new Productos();
            producto.setIdPro(idProductoSeleccionado);
            producto.setCodigoPro(codigo);
            producto.setIdCatPro(categoriaSeleccionadaId());
            producto.setNombrePro(txtNombre.getText().trim());
            producto.setPrecioPro(leerDecimal(txtPrecio.getText()));
            producto.setExistenciaPro(leerDecimal(txtExistencia.getText()));
            producto.setActivoPro("Activo".equals(cmbActivo.getSelectedItem()) ? "A" : "I");

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
        if (idProductoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el producto seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                productoService.eliminar(idProductoSeleccionado);
                JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.");
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El código es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cmbCategoria.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            BigDecimal existencia = new BigDecimal(txtExistencia.getText().trim());
            if (existencia.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "La existencia no puede ser negativa.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La existencia debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private int categoriaSeleccionadaId() {
        int index = cmbCategoria.getSelectedIndex();
        if (index >= 0 && index < idsCategoria.size()) {
            return idsCategoria.get(index);
        }
        return 1;
    }

    private BigDecimal leerDecimal(String valor) {
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(limpio);
    }

    private void limpiarFormulario() {
        idProductoSeleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("0.00");
        txtExistencia.setText("0.00");
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        cmbActivo.setSelectedItem("Activo");
        tabla.clearSelection();
        actualizarEstadoBotones(false);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
