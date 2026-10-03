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
import vista.util.FabricaDaisyUI;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
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
    private Integer idMenuSeleccionado = null;

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("← Volver");

    private final JButton btnAgregarDetalle = new JButton("+ Agregar");
    private final JButton btnQuitarDetalle = new JButton("- Quitar");

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JComboBox<String> cmbActivo = new JComboBox<>(new String[]{"Activo", "Inactivo"});

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
                String mensaje = "Sin menús registrados";
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
                String mensaje = "Seleccione un menú para ver su composición";
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
        super("Gestión de Combos");
        this.parent = parent;
        this.menuService = new MenuService();
        this.detalleMenuService = new DetalleMenuService();
        this.sandwichService = new SandwichService();
        this.productoService = new ProductoService();
        iniciarComponentes();
        cargarItems();
        cargarTabla();
        actualizarEstadoBotones(false);
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedorNorte = new JPanel(new GridLayout(1, 2, 14, 0));
        panelContenedorNorte.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));
        panelContenedorNorte.setOpaque(false);

        JPanel panelCamposFormulario = new JPanel(new GridLayout(2, 2, 12, 8));
        panelCamposFormulario.setOpaque(false);

        txtCodigo.setPreferredSize(new Dimension(140, 36));
        txtNombre.setPreferredSize(new Dimension(200, 36));
        txtPrecio.setText("0.00");
        txtPrecio.setPreferredSize(new Dimension(140, 36));
        cmbActivo.setPreferredSize(new Dimension(140, 36));

        FabricaDaisyUI.estilizarCampo(txtCodigo);
        FabricaDaisyUI.estilizarCampo(txtNombre);
        FabricaDaisyUI.estilizarCampo(txtPrecio);
        FabricaDaisyUI.estilizarCampo(cmbActivo);

        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Código:", txtCodigo));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Estado:", cmbActivo));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Nombre del combo:", txtNombre));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Precio (Q):", txtPrecio));

        JPanel tarjetaFormulario = FabricaDaisyUI.crearTarjetaSeccion(
                "Datos del combo",
                panelCamposFormulario
        );
        panelContenedorNorte.add(tarjetaFormulario);

        JPanel panelDetalleForm = new JPanel(new BorderLayout(8, 8));
        panelDetalleForm.setOpaque(false);

        JPanel panelDetalleCampos = new JPanel(new GridLayout(2, 2, 12, 8));
        panelDetalleCampos.setOpaque(false);

        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(80, 36));
        cmbSandwich.setPreferredSize(new Dimension(220, 36));
        cmbBebida.setPreferredSize(new Dimension(220, 36));
        cmbRicito.setPreferredSize(new Dimension(220, 36));

        FabricaDaisyUI.estilizarCampo(txtCantidad);
        FabricaDaisyUI.estilizarCampo(cmbSandwich);
        FabricaDaisyUI.estilizarCampo(cmbBebida);
        FabricaDaisyUI.estilizarCampo(cmbRicito);

        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Sándwich:", cmbSandwich));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Bebida:", cmbBebida));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Ricito:", cmbRicito));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Cantidad:", txtCantidad));

        JPanel panelDetalleBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelDetalleBotones.setOpaque(false);

        btnAgregarDetalle.setPreferredSize(new Dimension(145, 34));
        btnQuitarDetalle.setPreferredSize(new Dimension(145, 34));

        FabricaDaisyUI.aplicarBotonPrimario(btnAgregarDetalle);
        FabricaDaisyUI.aplicarBotonPeligro(btnQuitarDetalle);

        btnAgregarDetalle.addActionListener(e -> agregarDetalle());
        btnQuitarDetalle.addActionListener(e -> quitarDetalle());

        panelDetalleBotones.add(btnAgregarDetalle);
        panelDetalleBotones.add(btnQuitarDetalle);

        panelDetalleForm.add(panelDetalleCampos, BorderLayout.CENTER);
        panelDetalleForm.add(panelDetalleBotones, BorderLayout.SOUTH);

        JPanel tarjetaDetalle = FabricaDaisyUI.crearTarjetaSeccion(
                "Composición del Combo",
                panelDetalleForm
        );
        panelContenedorNorte.add(tarjetaDetalle);

        add(panelContenedorNorte, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 14, 0));
        panelTablas.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        panelTablas.setOpaque(false);

        modeloTabla.setColumnIdentifiers(new String[]{"ID", "Código", "Nombre", "Precio", "Activo"});
        tabla.setModel(modeloTabla);
        FabricaDaisyUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());

        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(45);
        colModel.getColumn(0).setMaxWidth(60);
        colModel.getColumn(1).setPreferredWidth(95);
        colModel.getColumn(2).setPreferredWidth(210);
        colModel.getColumn(3).setPreferredWidth(85);
        colModel.getColumn(4).setPreferredWidth(95);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setPreferredSize(new Dimension(520, 320));
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> {
            cargarItems();
            cargarTabla();
        });

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Lista de Combos",
                btnRefrescar,
                scrollTabla
        );

        modeloDetalle.setColumnIdentifiers(new String[]{"ID", "Tipo", "Item", "Cantidad"});
        tablaDetalle.setModel(modeloDetalle);
        FabricaDaisyUI.estilizarTabla(tablaDetalle);

        TableColumnModel colDetModel = tablaDetalle.getColumnModel();
        colDetModel.getColumn(0).setPreferredWidth(45);
        colDetModel.getColumn(0).setMaxWidth(60);
        colDetModel.getColumn(1).setPreferredWidth(90);
        colDetModel.getColumn(2).setPreferredWidth(250);
        colDetModel.getColumn(3).setPreferredWidth(80);

        tablaDetalle.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnQuitarDetalle.setEnabled(tablaDetalle.getSelectedRow() != -1);
            }
        });

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setPreferredSize(new Dimension(520, 320));
        scrollDetalle.setBorder(BorderFactory.createEmptyBorder());

        JPanel tarjetaDetalleTabla = FabricaDaisyUI.crearTarjetaSeccion(
                "Detalle",
                scrollDetalle
        );

        panelTablas.add(tarjetaTabla);
        panelTablas.add(tarjetaDetalleTabla);

        add(panelTablas, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));
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

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void actualizarEstadoBotones(boolean seleccionActiva) {
        btnAgregar.setEnabled(!seleccionActiva);
        btnModificar.setEnabled(seleccionActiva);
        btnEliminar.setEnabled(seleccionActiva);
        btnAgregarDetalle.setEnabled(seleccionActiva);
        btnQuitarDetalle.setEnabled(false);
    }

    private void cargarItems() {
        try {
            cmbSandwich.removeAllItems();
            idsSandwich.clear();
            for (Sandwich s : sandwichService.listarActivos()) {
                cmbSandwich.addItem(s.getNombreSan());
                idsSandwich.add(s.getIdSan());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los sándwich: " + ex.getMessage(),
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
                combo.addItem(p.getNombrePro());
                ids.add(p.getIdPro());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los productos de categoría " + nombre + ": " + ex.getMessage(),
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
                        "A".equalsIgnoreCase(m.getActivoMen()) ? "ACTIVO" : "INACTIVO"
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de combos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDetalle(int idMen) {
        modeloDetalle.setRowCount(0);
        try {
            for (DetalleMenu d : detalleMenuService.listarPorMenu(idMen)) {
                modeloDetalle.addRow(new Object[]{
                        d.getIdDetMen(),
                        tipoDescripcion(d.getTipoItemDet()),
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
        return TIPO_SANDWICH.equals(tipo) ? "Sándwich" : "Producto";
    }

    private String nombreItem(String tipo, int idItem) {
        if (TIPO_SANDWICH.equals(tipo)) {
            for (Sandwich s : sandwichService.listar()) {
                if (s.getIdSan() == idItem) {
                    return s.getNombreSan();
                }
            }
            return "Sándwich #" + idItem;
        }
        for (Productos p : productoService.listar()) {
            if (p.getIdPro() == idItem) {
                return p.getNombrePro();
            }
        }
        return "Producto #" + idItem;
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            actualizarEstadoBotones(false);
            return;
        }
        idMenuSeleccionado = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        txtCodigo.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        txtPrecio.setText(String.valueOf(modeloTabla.getValueAt(fila, 3)));
        String activoFila = String.valueOf(modeloTabla.getValueAt(fila, 4));
        cmbActivo.setSelectedItem("ACTIVO".equalsIgnoreCase(activoFila) ? "Activo" : "Inactivo");
        cargarDetalle(idMenuSeleccionado);
        actualizarEstadoBotones(true);
    }

    private void guardarMenu() {
        if (!validarFormulario()) {
            return;
        }
        if (!validarComposicionInputs()) {
            return;
        }
        String codigo = txtCodigo.getText().trim();
        if (menuService.buscarPorCodigo(codigo) != null) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un menú con el código '" + codigo + "'. Use el botón 'Modificar' para actualizar el combo existente.",
                    "Código Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Menus menu = new Menus();
            menu.setCodigoMen(codigo);
            menu.setNombreMen(txtNombre.getText().trim());
            menu.setPrecioMen(leerDecimal(txtPrecio.getText()));
            menu.setActivoMen("Activo".equals(cmbActivo.getSelectedItem()) ? "A" : "I");

            menuService.insertar(menu);
            Menus guardado = menuService.buscarPorCodigo(codigo);

            if (guardado != null) {
                guardarComposicion(guardado.getIdMen());
            }

            JOptionPane.showMessageDialog(this, "Combo guardado correctamente.");
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

    private boolean validarComposicionInputs() {
        if (cmbSandwich.getSelectedIndex() == -1 || cmbBebida.getSelectedIndex() == -1 || cmbRicito.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this,
                    "El combo requiere seleccionar un sándwich, una bebida y un ricito.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            BigDecimal cantidad = leerDecimal(txtCantidad.getText());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad de porciones debe ser mayor que cero.", "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean guardarComposicion(int idMen) {
        int[] indexes = new int[]{
                cmbSandwich.getSelectedIndex(),
                cmbBebida.getSelectedIndex(),
                cmbRicito.getSelectedIndex()
        };
        for (int index : indexes) {
            if (index == -1) {
                return false;
            }
        }

        try {
            BigDecimal cantidad = leerDecimal(txtCantidad.getText());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            insertarDetalle(idMen, TIPO_SANDWICH, idsSandwich.get(indexes[0]), cantidad);
            insertarDetalle(idMen, TIPO_PRODUCTO, idsBebida.get(indexes[1]), cantidad);
            insertarDetalle(idMen, TIPO_PRODUCTO, idsRicito.get(indexes[2]), cantidad);
            return true;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar la composición del menú: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        if (idMenuSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un menú de la tabla para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        String codigo = txtCodigo.getText().trim();
        Menus existente = menuService.buscarPorCodigo(codigo);
        if (existente != null && existente.getIdMen() != idMenuSeleccionado) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe otro menú con el código '" + codigo + "'. Ingrese un código diferente.",
                    "Código Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Menus menu = new Menus();
            menu.setIdMen(idMenuSeleccionado);
            menu.setCodigoMen(codigo);
            menu.setNombreMen(txtNombre.getText().trim());
            menu.setPrecioMen(leerDecimal(txtPrecio.getText()));
            menu.setActivoMen("Activo".equals(cmbActivo.getSelectedItem()) ? "A" : "I");

            menuService.actualizar(menu);
            JOptionPane.showMessageDialog(this, "Combo actualizado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarMenu() {
        if (idMenuSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un combo de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el combo seleccionado? Se eliminará su composición.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                menuService.eliminar(idMenuSeleccionado);
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarDetalle() {
        if (idMenuSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero debe seleccionar o guardar un combo para poder agregarle detalle.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarComposicionInputs()) {
            return;
        }
        if (!guardarComposicion(idMenuSeleccionado)) {
            return;
        }
        cargarDetalle(idMenuSeleccionado);
    }

    private void quitarDetalle() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila del detalle para quitar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int idDet = (int) modeloDetalle.getValueAt(fila, 0);
            detalleMenuService.eliminar(idDet);
            if (idMenuSeleccionado != null) {
                cargarDetalle(idMenuSeleccionado);
            }
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
            JOptionPane.showMessageDialog(this, "El código del combo es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del combo es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
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
        idMenuSeleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("0.00");
        cmbActivo.setSelectedItem("Activo");
        txtCantidad.setText("1");
        tabla.clearSelection();
        modeloDetalle.setRowCount(0);
        actualizarEstadoBotones(false);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
