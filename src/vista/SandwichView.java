package vista;

import model.DetalleSandwich;
import model.Productos;
import model.Sandwich;
import service.DetalleSandwichService;
import service.ProductoService;
import service.SandwichService;
import util.CategoriasItem;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SandwichView extends JFrame {

    private final SandwichService sandwichService;
    private final DetalleSandwichService detalleSandwichService;
    private final ProductoService productoService;

    private final Window parent;
    private Integer idSandwichSeleccionado = null;

    private final JButton btnAgregar = FabricaDaisyUI.crearBotonPrimario("Agregar", Icons.plus(16), null);
    private final JButton btnModificar = FabricaDaisyUI.crearBotonSecundario("Modificar", Icons.edit(16), null);
    private final JButton btnEliminar = FabricaDaisyUI.crearBotonPeligro("Eliminar", Icons.trash(16), null);
    private final JButton btnLimpiar = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), null);
    private final JButton btnRegresar = FabricaDaisyUI.crearBotonNeutral("Volver", Icons.arrowLeft(16), null);

    private final JButton btnAgregarDetalle = FabricaDaisyUI.crearBotonPrimario("Agregar", Icons.plus(14), null);
    private final JButton btnQuitarDetalle = FabricaDaisyUI.crearBotonPeligro("Quitar", Icons.trash(14), null);

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JComboBox<String> cmbActivo = new JComboBox<>(new String[]{"Activo", "Inactivo"});

    private final JComboBox<String> cmbProducto = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField();
    private final JComboBox<String> cmbObligatorio = new JComboBox<>(new String[]{"Incluido", "Opcional"});

    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscarSandwich = FabricaDaisyUI.crearCampoTexto("Buscar por código o nombre...", 16);
    private TableRowSorter<DefaultTableModel> clasificadorSandwich;

    private final DefaultTableModel modeloDetalle = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaDetalle = new JTable(modeloDetalle);

    private List<Integer> idsProductoPan = new ArrayList<>();

    public SandwichView(Window parent) {
        super("Gestión de Sándwiches");
        this.parent = parent;
        this.sandwichService = new SandwichService();
        this.detalleSandwichService = new DetalleSandwichService();
        this.productoService = new ProductoService();
        iniciarComponentes();
        cargarProductos();
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
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Nombre:", txtNombre));
        panelCamposFormulario.add(FabricaDaisyUI.crearCampoConEtiqueta("Precio Unitario (Q):", txtPrecio));

        JPanel tarjetaFormulario = FabricaDaisyUI.crearTarjetaSeccion(
                "Datos del Sándwich",
                panelCamposFormulario
        );
        panelContenedorNorte.add(tarjetaFormulario);

        JPanel panelDetalleContenedor = new JPanel(new BorderLayout(8, 8));
        panelDetalleContenedor.setOpaque(false);

        JPanel panelDetalleCampos = new JPanel(new GridLayout(2, 2, 12, 8));
        panelDetalleCampos.setOpaque(false);

        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(80, 36));
        cmbProducto.setPreferredSize(new Dimension(220, 36));
        cmbObligatorio.setPreferredSize(new Dimension(140, 36));

        FabricaDaisyUI.estilizarCampo(txtCantidad);
        FabricaDaisyUI.estilizarCampo(cmbProducto);
        FabricaDaisyUI.estilizarCampo(cmbObligatorio);

        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Producto:", cmbProducto));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Tipo:", cmbObligatorio));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Cantidad:", txtCantidad));

        JPanel panelDetalleAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelDetalleAcciones.setOpaque(false);

        btnAgregarDetalle.setIconTextGap(6);
        btnQuitarDetalle.setIconTextGap(6);
        btnAgregarDetalle.setPreferredSize(new Dimension(140, 34));
        btnQuitarDetalle.setPreferredSize(new Dimension(140, 34));

        FabricaDaisyUI.aplicarBotonPrimario(btnAgregarDetalle);
        FabricaDaisyUI.aplicarBotonPeligro(btnQuitarDetalle);

        btnAgregarDetalle.addActionListener(e -> agregarDetalle());
        btnQuitarDetalle.addActionListener(e -> quitarDetalle());

        panelDetalleAcciones.add(btnAgregarDetalle);
        panelDetalleAcciones.add(btnQuitarDetalle);

        panelDetalleContenedor.add(panelDetalleCampos, BorderLayout.CENTER);
        panelDetalleContenedor.add(panelDetalleAcciones, BorderLayout.SOUTH);

        JPanel tarjetaDetalle = FabricaDaisyUI.crearTarjetaSeccion(
                "Elementos Requeridos",
                panelDetalleContenedor
        );
        panelContenedorNorte.add(tarjetaDetalle);

        add(panelContenedorNorte, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 14, 0));
        panelTablas.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        panelTablas.setOpaque(false);

        modeloTabla.setColumnIdentifiers(new String[]{"ID", "Código", "Nombre", "Precio", "Activo"});
        clasificadorSandwich = new TableRowSorter<>(modeloTabla);
        tabla.setModel(modeloTabla);
        tabla.setRowSorter(clasificadorSandwich);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JPanel panelBarraBusquedaSandwich = new JPanel(new BorderLayout(8, 0));
        panelBarraBusquedaSandwich.setOpaque(false);
        panelBarraBusquedaSandwich.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel lblBuscarSan = new JLabel("Buscar:");
        lblBuscarSan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton btnLimpiarBusquedaSan = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(14), e -> {
            txtBuscarSandwich.setText("");
            filtrarTablaSandwich();
        });
        btnLimpiarBusquedaSan.setPreferredSize(new Dimension(95, 34));

        panelBarraBusquedaSandwich.add(lblBuscarSan, BorderLayout.WEST);
        panelBarraBusquedaSandwich.add(txtBuscarSandwich, BorderLayout.CENTER);
        panelBarraBusquedaSandwich.add(btnLimpiarBusquedaSan, BorderLayout.EAST);

        txtBuscarSandwich.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarTablaSandwich();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTablaSandwich();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTablaSandwich();
            }
        });

        JPanel panelContenidoSandwich = new JPanel(new BorderLayout(6, 6));
        panelContenidoSandwich.setOpaque(false);
        panelContenidoSandwich.add(panelBarraBusquedaSandwich, BorderLayout.NORTH);
        panelContenidoSandwich.add(scrollTabla, BorderLayout.CENTER);

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> {
            cargarProductos();
            cargarTabla();
        });

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Lista de Sándwiches",
                btnRefrescar,
                panelContenidoSandwich
        );

        modeloDetalle.setColumnIdentifiers(new String[]{"ID", "Ingrediente", "Cantidad", "Tipo"});
        tablaDetalle.setModel(modeloDetalle);
        tablaDetalle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tablaDetalle);

        TableColumnModel colDetModel = tablaDetalle.getColumnModel();
        colDetModel.getColumn(0).setPreferredWidth(45);
        colDetModel.getColumn(0).setMaxWidth(60);
        colDetModel.getColumn(1).setPreferredWidth(240);
        colDetModel.getColumn(2).setPreferredWidth(85);
        colDetModel.getColumn(3).setPreferredWidth(110);

        tablaDetalle.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnQuitarDetalle.setEnabled(tablaDetalle.getSelectedRow() != -1);
            }
        });

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(BorderFactory.createEmptyBorder());

        JPanel tarjetaDetalleTabla = FabricaDaisyUI.crearTarjetaSeccion(
                "Receta de Ingredientes",
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

        btnAgregar.setIconTextGap(8);
        btnModificar.setIconTextGap(8);
        btnEliminar.setIconTextGap(8);
        btnLimpiar.setIconTextGap(8);
        btnRegresar.setIconTextGap(8);

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

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void actualizarEstadoBotones(boolean seleccionActiva) {
        btnAgregar.setEnabled(!seleccionActiva);
        btnModificar.setEnabled(seleccionActiva);
        btnEliminar.setEnabled(seleccionActiva);
        btnAgregarDetalle.setEnabled(seleccionActiva);
        btnQuitarDetalle.setEnabled(false);
    }

    private void cargarProductos() {
        cmbProducto.removeAllItems();
        idsProductoPan = new ArrayList<>();
        try {
            int idPan = CategoriasItem.obtenerIdPan();
            List<Productos> productos = productoService.listarActivosPorCategoria(idPan);
            if (productos.isEmpty()) {
                productos = productoService.listar();
            }
            for (Productos p : productos) {
                cmbProducto.addItem(p.getNombrePro());
                idsProductoPan.add(p.getIdPro());
            }
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "No se pudieron cargar los productos: " + ex.getMessage());
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
                        "A".equalsIgnoreCase(s.getActivoSan()) ? "ACTIVO" : "INACTIVO"
                });
            }
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "No se pudo cargar la lista de sándwiches: " + ex.getMessage());
        }
    }

    private void cargarDetalle(int idSan) {
        modeloDetalle.setRowCount(0);
        try {
            for (DetalleSandwich d : detalleSandwichService.listarPorSandwich(idSan)) {
                modeloDetalle.addRow(new Object[]{
                        d.getIdDetSan(),
                        nombreProducto(d.getIdProDet()),
                        d.getCantidadDet(),
                        "S".equalsIgnoreCase(d.getObligatorioDet()) ? "INCLUIDO" : "OPCIONAL"
                });
            }
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "No se pudo cargar el detalle: " + ex.getMessage());
        }
    }

    private String nombreProducto(int idPro) {
        for (Productos p : productoService.listar()) {
            if (p.getIdPro() == idPro) {
                return p.getNombrePro();
            }
        }
        return "Producto #" + idPro;
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            idSandwichSeleccionado = null;
            limpiarCamposSinDeseleccionar();
            actualizarEstadoBotones(false);
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        idSandwichSeleccionado = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(filaModelo, 0)));
        txtCodigo.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 1)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 2)));
        txtPrecio.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 3)));
        String activoFila = String.valueOf(modeloTabla.getValueAt(filaModelo, 4));
        cmbActivo.setSelectedItem("ACTIVO".equalsIgnoreCase(activoFila) ? "Activo" : "Inactivo");
        cargarDetalle(idSandwichSeleccionado);
        actualizarEstadoBotones(true);
    }

    private void limpiarCamposSinDeseleccionar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("0.00");
        cmbActivo.setSelectedItem("Activo");
        txtCantidad.setText("1");
        cmbObligatorio.setSelectedItem("Incluido");
        modeloDetalle.setRowCount(0);
    }

    private void filtrarTablaSandwich() {
        if (clasificadorSandwich == null) {
            return;
        }
        String texto = txtBuscarSandwich.getText().trim();
        if (texto.isEmpty()) {
            clasificadorSandwich.setRowFilter(null);
            return;
        }
        String textoMin = texto.toLowerCase();
        clasificadorSandwich.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    String val = entry.getStringValue(i);
                    if (val != null && val.toLowerCase().contains(textoMin)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void guardarSandwich() {
        if (!validarFormulario()) {
            return;
        }
        String codigo = txtCodigo.getText().trim();
        if (sandwichService.buscarPorCodigo(codigo) != null) {
            FabricaDaisyUI.mostrarAdvertencia(this,
                    "Código Duplicado",
                    "Ya existe un sándwich con el código '" + codigo + "'. Use el botón 'Modificar' para actualizar el registro existente.");
            return;
        }
        try {
            Sandwich sandwich = new Sandwich();
            sandwich.setCodigoSan(codigo);
            sandwich.setNombreSan(txtNombre.getText().trim());
            sandwich.setPrecioSan(leerDecimal(txtPrecio.getText()));
            sandwich.setActivoSan("Activo".equals(cmbActivo.getSelectedItem()) ? "A" : "I");

            sandwichService.insertar(sandwich);
            Sandwich guardado = sandwichService.buscarPorCodigo(codigo);

            FabricaDaisyUI.mostrarToastExito(this, "Sándwich guardado correctamente.");
            cargarTabla();
            limpiarFormulario();

            if (guardado != null) {
                seleccionarEnTabla(guardado.getIdSan());
            }
        } catch (NumberFormatException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "El precio debe ser un número válido.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al guardar: " + ex.getMessage());
        }
    }

    private void actualizarSandwich() {
        if (idSandwichSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un sándwich de la tabla para modificar.");
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        String codigo = txtCodigo.getText().trim();
        Sandwich existente = sandwichService.buscarPorCodigo(codigo);
        if (existente != null && existente.getIdSan() != idSandwichSeleccionado) {
            FabricaDaisyUI.mostrarAdvertencia(this,
                    "Código Duplicado",
                    "Ya existe otro sándwich con el código '" + codigo + "'. Ingrese un código diferente.");
            return;
        }
        try {
            Sandwich sandwich = new Sandwich();
            sandwich.setIdSan(idSandwichSeleccionado);
            sandwich.setCodigoSan(codigo);
            sandwich.setNombreSan(txtNombre.getText().trim());
            sandwich.setPrecioSan(leerDecimal(txtPrecio.getText()));
            sandwich.setActivoSan("Activo".equals(cmbActivo.getSelectedItem()) ? "A" : "I");

            sandwichService.actualizar(sandwich);
            FabricaDaisyUI.mostrarToastExito(this, "Sándwich actualizado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "El precio debe ser un número válido.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al actualizar: " + ex.getMessage());
        }
    }

    private void eliminarSandwich() {
        if (idSandwichSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un sándwich de la tabla para eliminar.");
            return;
        }
        boolean confirmar = FabricaDaisyUI.mostrarConfirmacion(this, "Confirmar Eliminación", "¿Desea eliminar el sándwich seleccionado? Se eliminarán sus ingredientes asociados.");
        if (confirmar) {
            try {
                for (DetalleSandwich d : detalleSandwichService.listarPorSandwich(idSandwichSeleccionado)) {
                    detalleSandwichService.eliminar(d.getIdDetSan());
                }
                sandwichService.eliminar(idSandwichSeleccionado);
                FabricaDaisyUI.mostrarToastExito(this, "Sándwich eliminado correctamente.");
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al eliminar: " + ex.getMessage());
            }
        }
    }

    private void agregarDetalle() {
        if (idSandwichSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Primero debe seleccionar un sándwich para poder agregarle ingredientes.");
            return;
        }
        if (cmbProducto.getSelectedIndex() == -1) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Debe seleccionar un producto.");
            return;
        }
        try {
            BigDecimal cantidad = leerDecimal(txtCantidad.getText());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "La cantidad debe ser mayor que cero.");
                return;
            }

            DetalleSandwich detalle = new DetalleSandwich();
            detalle.setIdSanDet(idSandwichSeleccionado);
            detalle.setIdProDet(idsProductoPan.get(cmbProducto.getSelectedIndex()));
            detalle.setCantidadDet(cantidad);
            detalle.setObligatorioDet("Incluido".equals(cmbObligatorio.getSelectedItem()) ? "S" : "N");

            detalleSandwichService.insertar(detalle);
            cargarDetalle(idSandwichSeleccionado);
            FabricaDaisyUI.mostrarToastExito(this, "Ingrediente agregado.");
        } catch (NumberFormatException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "La cantidad debe ser un número válido.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al agregar el detalle: " + ex.getMessage());
        }
    }

    private void quitarDetalle() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un ingrediente para quitar.");
            return;
        }
        try {
            int idDet = (int) modeloDetalle.getValueAt(fila, 0);
            detalleSandwichService.eliminar(idDet);
            if (idSandwichSeleccionado != null) {
                cargarDetalle(idSandwichSeleccionado);
            }
            FabricaDaisyUI.mostrarToastExito(this, "Ingrediente quitado.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al quitar el ingrediente: " + ex.getMessage());
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
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "El código del sándwich es obligatorio.");
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "El nombre del sándwich es obligatorio.");
            return false;
        }
        try {
            if (leerDecimal(txtPrecio.getText()).compareTo(BigDecimal.ZERO) < 0) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "El precio no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException ex) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "El precio debe ser un número válido.");
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
        idSandwichSeleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("0.00");
        cmbActivo.setSelectedItem("Activo");
        txtCantidad.setText("1");
        cmbObligatorio.setSelectedItem("Incluido");
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
