package vista;

import model.DetalleMenu;
import model.Menus;
import model.Productos;
import model.Sandwich;
import service.DetalleMenuService;
import service.MenuService;
import service.ProductoService;
import service.SandwichService;
import util.CategoriasItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MenuView extends JFrame {

    private static final String TIPO_SANDWICH = "S";
    private static final String TIPO_PRODUCTO = "P";

    private final MenuService menuService;
    private final DetalleMenuService detalleMenuService;
    private final SandwichService sandwichService;
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

    private final JComboBox<String> cmbSandwich = new JComboBox<>();
    private final JComboBox<String> cmbBebida = new JComboBox<>();
    private final JComboBox<String> cmbRicito = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField();

    private final List<Integer> idsSandwich = new ArrayList<>();
    private final List<Integer> idsBebida = new ArrayList<>();
    private final List<Integer> idsRicito = new ArrayList<>();

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
                String mensaje = "Seleccione un menu para ver su detalle";
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

    public MenuView(Window parent) {
        super("Gestión de Menus");
        this.parent = parent;
        this.menuService = new MenuService();
        this.detalleMenuService = new DetalleMenuService();
        this.sandwichService = new SandwichService();
        this.productoService = new ProductoService();
        iniciarComponentes();
        cargarItems();
        cargarTabla();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1050, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 5, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Menu"));

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
        panelDetalleForm.setBorder(BorderFactory.createTitledBorder("Composicion del Menu (Sandwich + Bebida + Ricito)"));
        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(70, 25));
        cmbSandwich.setPreferredSize(new Dimension(200, 25));
        cmbBebida.setPreferredSize(new Dimension(200, 25));
        cmbRicito.setPreferredSize(new Dimension(200, 25));
        btnAgregarDetalle.setPreferredSize(new Dimension(140, 30));
        btnQuitarDetalle.setPreferredSize(new Dimension(140, 30));

        btnAgregarDetalle.addActionListener(e -> agregarDetalle());
        btnQuitarDetalle.addActionListener(e -> quitarDetalle());

        panelDetalleForm.add(new JLabel("Sandwich:"));
        panelDetalleForm.add(cmbSandwich);
        panelDetalleForm.add(new JLabel("Bebida:"));
        panelDetalleForm.add(cmbBebida);
        panelDetalleForm.add(new JLabel("Ricito:"));
        panelDetalleForm.add(cmbRicito);
        panelDetalleForm.add(new JLabel("Cantidad:"));
        panelDetalleForm.add(txtCantidad);
        panelDetalleForm.add(btnAgregarDetalle);
        panelDetalleForm.add(btnQuitarDetalle);

        panelSuperior.add(panelDetalleForm, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 10, 10));
        panelTablas.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Listado de Menus"));

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
        panelTablaDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del Menu seleccionado"));

        modeloDetalle.setColumnIdentifiers(new String[]{"ID", "ID Menu", "Tipo", "ID Item", "Item", "Cantidad"});
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

        btnAgregar.addActionListener(e -> guardarMenu());
        btnModificar.addActionListener(e -> actualizarMenu());
        btnEliminar.addActionListener(e -> eliminarMenu());
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

    private void cargarItems() {
        try {
            cmbSandwich.removeAllItems();
            idsSandwich.clear();
            for (Sandwich s : sandwichService.listarActivos()) {
                cmbSandwich.addItem(s.getNombreSan() + " (" + s.getCodigoSan() + ")");
                idsSandwich.add(s.getIdSan());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los sandwich: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        cargarComboProducto(cmbBebida, idsBebida, CategoriasItem.BEBIDA, "bebidas");
        cargarComboProducto(cmbRicito, idsRicito, CategoriasItem.RICITO, "ricitos");
    }

    private void cargarComboProducto(JComboBox<String> combo, List<Integer> ids, int idCategoria, String nombre) {
        try {
            combo.removeAllItems();
            ids.clear();
            for (Productos p : productoService.listarActivosPorCategoria(idCategoria)) {
                combo.addItem(p.getNombrePro() + " (" + p.getCodigoPro() + ")");
                ids.add(p.getIdPro());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los productos de categoria " + nombre + ": " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            for (Menus m : menuService.listar()) {
                modeloTabla.addRow(new Object[]{
                        m.getIdMen(),
                        m.getCodigoMen(),
                        m.getNombreMen(),
                        m.getPrecioMen(),
                        m.getActivoMen()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de menus: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalle(int idMen) {
        modeloDetalle.setRowCount(0);
        try {
            for (DetalleMenu d : detalleMenuService.listarPorMenu(idMen)) {
                modeloDetalle.addRow(new Object[]{
                        d.getIdDetMen(),
                        d.getIdMenDet(),
                        tipoDescripcion(d.getTipoItemDet()),
                        d.getIdItemDet(),
                        nombreItem(d.getTipoItemDet(), d.getIdItemDet()),
                        d.getCantidadDet()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el detalle: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String tipoDescripcion(String tipo) {
        return TIPO_SANDWICH.equals(tipo) ? "Sandwich" : "Producto";
    }

    private String nombreItem(String tipo, int idItem) {
        if (TIPO_SANDWICH.equals(tipo)) {
            for (int i = 0; i < cmbSandwich.getItemCount(); i++) {
                if (idsSandwich.get(i) == idItem) {
                    return cmbSandwich.getItemAt(i);
                }
            }
            return "Sandwich " + idItem;
        }
        List<Integer> ids = esBebida(idItem) ? idsBebida : idsRicito;
        JComboBox<String> combo = esBebida(idItem) ? cmbBebida : cmbRicito;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (ids.get(i) == idItem) {
                return combo.getItemAt(i);
            }
        }
        return "Producto " + idItem;
    }

    private boolean esBebida(int idItem) {
        return idsBebida.contains(idItem);
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

    private void guardarMenu() {
        if (!validarFormulario()) {
            return;
        }
        try {
            Menus menu = new Menus();
            menu.setCodigoMen(txtCodigo.getText().trim());
            menu.setNombreMen(txtNombre.getText().trim());
            menu.setPrecioMen(leerDecimal(txtPrecio.getText()));
            menu.setActivoMen((String) cmbActivo.getSelectedItem());

            menuService.insertar(menu);
            Menus guardado = menuService.buscarPorCodigo(txtCodigo.getText().trim());

            if (guardado != null && !guardarComposicion(guardado.getIdMen())) {
                return;
            }

            JOptionPane.showMessageDialog(this, "Menu guardado correctamente.");
            cargarTabla();
            limpiarFormulario();

            if (guardado != null) {
                seleccionarEnTabla(guardado.getIdMen());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean guardarComposicion(int idMen) {
        int[] indexes = new int[]{
                cmbSandwich.getSelectedIndex(),
                cmbBebida.getSelectedIndex(),
                cmbRicito.getSelectedIndex()
        };
        for (int index : indexes) {
            if (index == -1) {
                JOptionPane.showMessageDialog(this,
                        "El menu requiere un sandwich, una bebida y un ricito para poder completarse.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        try {
            BigDecimal cantidad = leerDecimal(txtCantidad.getText());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            insertarDetalle(idMen, TIPO_SANDWICH, idsSandwich.get(indexes[0]), cantidad);
            insertarDetalle(idMen, TIPO_PRODUCTO, idsBebida.get(indexes[1]), cantidad);
            insertarDetalle(idMen, TIPO_PRODUCTO, idsRicito.get(indexes[2]), cantidad);
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar la composición del menu: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void insertarDetalle(int idMen, String tipo, int idItem, BigDecimal cantidad) {
        DetalleMenu detalle = new DetalleMenu();
        detalle.setIdMenDet(idMen);
        detalle.setTipoItemDet(tipo);
        detalle.setIdItemDet(idItem);
        detalle.setCantidadDet(cantidad);
        detalleMenuService.insertar(detalle);
    }

    private void actualizarMenu() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un menu de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        try {
            Menus menu = new Menus();
            menu.setIdMen(Integer.parseInt(txtId.getText()));
            menu.setCodigoMen(txtCodigo.getText().trim());
            menu.setNombreMen(txtNombre.getText().trim());
            menu.setPrecioMen(leerDecimal(txtPrecio.getText()));
            menu.setActivoMen((String) cmbActivo.getSelectedItem());

            menuService.actualizar(menu);
            JOptionPane.showMessageDialog(this, "Menu actualizado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarMenu() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un menu de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el menu seleccionado? Se eliminará su detalle.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                menuService.eliminar(Integer.parseInt(txtId.getText()));
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarDetalle() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero debe guardar el menu para poder agregarle detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!guardarComposicion(Integer.parseInt(txtId.getText()))) {
            return;
        }
        cargarDetalle(Integer.parseInt(txtId.getText()));
    }

    private void quitarDetalle() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila del detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            detalleMenuService.eliminar((int) modeloDetalle.getValueAt(fila, 0));
            cargarDetalle(Integer.parseInt(txtId.getText()));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al quitar el detalle: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarEnTabla(int idMen) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if ((int) modeloTabla.getValueAt(i, 0) == idMen) {
                tabla.setRowSelectionInterval(i, i);
                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El código del menu es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del menu es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
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
