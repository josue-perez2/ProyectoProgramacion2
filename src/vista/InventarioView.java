package vista;

import model.Categorias;
import model.InventarioMovimientos;
import model.Productos;
import service.CategoriaService;
import service.InventarioMovimientosService;
import service.ProductoService;
import util.ExportadorCSV;
import util.FormatoTexto;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventarioView extends JFrame {

    private final Window parent;
    private final ProductoService productoService;
    private final InventarioMovimientosService inventarioService;
    private final CategoriaService categoriaService;

    private final JComboBox<String> cmbProductos = new JComboBox<>();
    private final List<Integer> idsProductos = new ArrayList<>();
    private final List<Productos> listaProductosMemoria = new ArrayList<>();
    private final Map<Integer, String> mapaCategorias = new HashMap<>();
    private boolean cargandoFiltros = false;

    private final JLabel lblDetalleCategoria = new JLabel("Categoría: -");
    private final JLabel lblDetallePrecio = new JLabel("Precio Unitario: -");
    private final JLabel lblDetalleExistencia = new JLabel("Existencia Actual: -");

    private final JTextField txtCantidadIngreso = FabricaDaisyUI.crearCampoTexto("Cantidad a ingresar", 12);

    private final JButton btnConfirmar = FabricaDaisyUI.crearBotonPrimario("Confirmar Abastecimiento", Icons.plus(16), e -> abastecerStock());
    private final JButton btnLimpiar = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), e -> limpiarFormulario());
    private final JButton btnVolver = FabricaDaisyUI.crearBotonNeutral("Volver al Inicio", Icons.arrowLeft(16), e -> regresar());

    private final JTextField txtBuscar = FabricaDaisyUI.crearCampoTexto("Buscar por nombre o código...", 18);
    private final JComboBox<String> cmbFiltroCategoria = new JComboBox<>();
    private final JComboBox<String> cmbFiltroEstado = new JComboBox<>(new String[]{
            "Todos los estados",
            "Óptimo (> 15)",
            "Bajo Stock (1 - 15)",
            "Agotados (0)"
    });
    private final JButton btnBuscar = FabricaDaisyUI.crearBotonPrimario("Buscar", Icons.search(16), e -> filtrarTablaProductos());

    private final DefaultTableModel modeloProductos = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaProductos = new JTable(modeloProductos);

    private final JComboBox<String> cmbFiltroTipoMov = new JComboBox<>(new String[]{
            "TODOS LOS MOVIMIENTOS",
            "ABASTECIMIENTO",
            "VENTA",
            "CANJE",
            "REVERSION"
    });

    private final DefaultTableModel modeloMovimientos = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaMovimientos = new JTable(modeloMovimientos);

    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public InventarioView() {
        this(null);
    }

    public InventarioView(Window parent) {
        super("Inventario");
        this.parent = parent;
        this.productoService = new ProductoService();
        this.inventarioService = new InventarioMovimientosService();
        this.categoriaService = new CategoriaService();

        iniciarComponentes();
        cargandoFiltros = true;
        cargarCategorias();
        cargarProductosEnCombo();
        cargandoFiltros = false;
        cargarTablaProductos();
        cargarTablaMovimientos();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1140, 720);
        setMinimumSize(new Dimension(980, 580));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedor = new JPanel(new BorderLayout(14, 14));
        panelContenedor.setBorder(new EmptyBorder(14, 20, 14, 20));
        panelContenedor.setOpaque(false);

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 0));
        panelSuperior.setOpaque(false);

        JPanel panelTextoTitulo = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTextoTitulo.setOpaque(false);

        JLabel lblTitulo = new JLabel("Control y Abastecimiento de Inventario");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");

        panelTextoTitulo.add(lblTitulo);
        panelSuperior.add(panelTextoTitulo, BorderLayout.WEST);

        JPanel panelAccionesSup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelAccionesSup.setOpaque(false);

        JButton btnRefrescarGlobal = FabricaDaisyUI.crearBotonRefrescarIcono(e -> refrescarTodo());
        btnVolver.setPreferredSize(new Dimension(145, 38));

        panelAccionesSup.add(btnRefrescarGlobal);
        panelAccionesSup.add(btnVolver);
        panelSuperior.add(panelAccionesSup, BorderLayout.EAST);

        panelContenedor.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(14, 14));
        panelCentro.setOpaque(false);

        panelCentro.add(crearPanelFormulario(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Existencias de Productos", Icons.boxes(16), crearPanelTabProductos());
        tabbedPane.addTab("Historial de Movimientos", Icons.refreshCw(16), crearPanelTabMovimientos());

        panelCentro.add(tabbedPane, BorderLayout.CENTER);
        panelContenedor.add(panelCentro, BorderLayout.CENTER);

        add(panelContenedor, BorderLayout.CENTER);
    }

    private JPanel crearPanelFormulario() {
        JPanel contenedor = new JPanel(new GridLayout(1, 2, 24, 0));
        contenedor.setOpaque(false);

        JPanel colIzquierda = new JPanel(new BorderLayout(0, 8));
        colIzquierda.setOpaque(false);

        JLabel lblSelProd = new JLabel("Producto a abastecer:");
        lblSelProd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSelProd.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #334155; [dark]foreground: #e2e8f0");

        FabricaDaisyUI.estilizarCampo(cmbProductos);
        cmbProductos.setPreferredSize(new Dimension(380, 38));
        cmbProductos.addActionListener(e -> seleccionarProductoEnCombo());

        JPanel panelFichaProd = new JPanel(new GridLayout(3, 1, 0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean oscuro = TemaGestor.esModoOscuro();
                g2.setColor(oscuro ? new Color(44, 47, 60) : new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(oscuro ? new Color(68, 71, 90) : new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        panelFichaProd.setOpaque(false);
        panelFichaProd.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblDetalleCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetalleCategoria.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #475569; [dark]foreground: #cbd5e1");

        lblDetallePrecio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetallePrecio.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #475569; [dark]foreground: #cbd5e1");

        lblDetalleExistencia.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetalleExistencia.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");

        panelFichaProd.add(lblDetalleCategoria);
        panelFichaProd.add(lblDetallePrecio);
        panelFichaProd.add(lblDetalleExistencia);

        JPanel panelCampoProd = new JPanel(new BorderLayout(0, 6));
        panelCampoProd.setOpaque(false);
        panelCampoProd.add(lblSelProd, BorderLayout.NORTH);
        panelCampoProd.add(cmbProductos, BorderLayout.CENTER);

        colIzquierda.add(panelCampoProd, BorderLayout.NORTH);
        colIzquierda.add(panelFichaProd, BorderLayout.SOUTH);

        JPanel colDerecha = new JPanel(new BorderLayout(0, 10));
        colDerecha.setOpaque(false);

        JLabel lblCant = new JLabel("Cantidad a ingresar:");
        lblCant.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCant.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #334155; [dark]foreground: #f8f8f2");

        ((AbstractDocument) txtCantidadIngreso.getDocument()).setDocumentFilter(FormatoTexto.filtroEnteros());
        txtCantidadIngreso.setPreferredSize(new Dimension(220, 38));
        txtCantidadIngreso.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validarEstadoBotonConfirmar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validarEstadoBotonConfirmar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validarEstadoBotonConfirmar();
            }
        });

        JPanel panelCampoCant = new JPanel(new BorderLayout(0, 6));
        panelCampoCant.setOpaque(false);
        panelCampoCant.add(lblCant, BorderLayout.NORTH);
        panelCampoCant.add(txtCantidadIngreso, BorderLayout.CENTER);

        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotonesAccion.setOpaque(false);
        panelBotonesAccion.setBorder(new EmptyBorder(16, 0, 0, 0));
        btnConfirmar.setPreferredSize(new Dimension(220, 38));
        btnConfirmar.setEnabled(false);
        btnLimpiar.setPreferredSize(new Dimension(110, 38));
        panelBotonesAccion.add(btnLimpiar);
        panelBotonesAccion.add(btnConfirmar);

        colDerecha.add(panelCampoCant, BorderLayout.NORTH);
        colDerecha.add(panelBotonesAccion, BorderLayout.SOUTH);

        contenedor.add(colIzquierda);
        contenedor.add(colDerecha);

        return FabricaDaisyUI.crearTarjetaSeccion("Entrada de productos", contenedor);
    }

    private JPanel crearPanelTabProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setOpaque(false);

        JPanel panelBarra = new JPanel(new BorderLayout(10, 0));
        panelBarra.setOpaque(false);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFiltros.setOpaque(false);

        txtBuscar.setPreferredSize(new Dimension(240, 38));
        txtBuscar.addActionListener(e -> filtrarTablaProductos());
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarTablaProductos();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTablaProductos();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTablaProductos();
            }
        });

        cmbFiltroCategoria.setPreferredSize(new Dimension(170, 38));
        FabricaDaisyUI.estilizarCampo(cmbFiltroCategoria);
        cmbFiltroCategoria.addActionListener(e -> filtrarTablaProductos());

        cmbFiltroEstado.setPreferredSize(new Dimension(160, 38));
        FabricaDaisyUI.estilizarCampo(cmbFiltroEstado);
        cmbFiltroEstado.addActionListener(e -> filtrarTablaProductos());

        btnBuscar.setPreferredSize(new Dimension(100, 38));
        FabricaDaisyUI.aplicarBotonPrimario(btnBuscar);

        JLabel lblFiltrar = new JLabel("Filtrar:");
        lblFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFiltrar.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #334155; [dark]foreground: #f8f8f2");

        panelFiltros.add(lblFiltrar);
        panelFiltros.add(txtBuscar);
        panelFiltros.add(cmbFiltroCategoria);
        panelFiltros.add(cmbFiltroEstado);
        panelFiltros.add(btnBuscar);

        JPanel panelAccionesDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelAccionesDer.setOpaque(false);

        JButton btnExportarProd = FabricaDaisyUI.crearBotonExportarCsvIcono(e ->
                ExportadorCSV.exportarTabla(this, tablaProductos, "Inventario_Existencias_Productos"));
        JButton btnLimpiarFiltros = FabricaDaisyUI.crearBotonLimpiarFiltros(e -> resetearFiltrosProductos());
        JButton btnRefrescarProd = FabricaDaisyUI.crearBotonRefrescarIcono(e -> {
            cargarProductosEnCombo();
            cargarTablaProductos();
        });

        panelAccionesDer.add(btnExportarProd);
        panelAccionesDer.add(btnLimpiarFiltros);
        panelAccionesDer.add(btnRefrescarProd);

        panelBarra.add(panelFiltros, BorderLayout.WEST);
        panelBarra.add(panelAccionesDer, BorderLayout.EAST);

        panel.add(panelBarra, BorderLayout.NORTH);

        String[] cols = {"ID", "Código", "Producto", "Categoría", "Precio (Q)", "Existencia", "Estado de Stock"};
        modeloProductos.setColumnIdentifiers(cols);
        tablaProductos.setModel(modeloProductos);
        tablaProductos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tablaProductos);

        TableColumnModel cm = tablaProductos.getColumnModel();
        cm.getColumn(0).setPreferredWidth(55);
        cm.getColumn(0).setMaxWidth(70);
        cm.getColumn(1).setPreferredWidth(100);
        cm.getColumn(2).setPreferredWidth(230);
        cm.getColumn(3).setPreferredWidth(140);
        cm.getColumn(4).setPreferredWidth(90);
        cm.getColumn(5).setPreferredWidth(90);
        cm.getColumn(6).setPreferredWidth(140);

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        cm.getColumn(0).setCellRenderer(renderCentro);
        cm.getColumn(1).setCellRenderer(renderCentro);
        cm.getColumn(4).setCellRenderer(renderCentro);
        cm.getColumn(5).setCellRenderer(renderCentro);

        cm.getColumn(6).setCellRenderer(new RenderizadorEstadoStock());

        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaProductos.getSelectedRow();
                if (fila != -1) {
                    int filaMod = tablaProductos.convertRowIndexToModel(fila);
                    int idPro = (int) modeloProductos.getValueAt(filaMod, 0);
                    seleccionarProductoPorId(idPro);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablaProductos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void resetearFiltrosProductos() {
        txtBuscar.setText("");
        if (cmbFiltroCategoria.getItemCount() > 0) {
            cmbFiltroCategoria.setSelectedIndex(0);
        }
        if (cmbFiltroEstado.getItemCount() > 0) {
            cmbFiltroEstado.setSelectedIndex(0);
        }
        filtrarTablaProductos();
    }

    private JPanel crearPanelTabMovimientos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setOpaque(false);

        JPanel panelBarra = new JPanel(new BorderLayout(10, 0));
        panelBarra.setOpaque(false);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelFiltros.setOpaque(false);

        cmbFiltroTipoMov.setPreferredSize(new Dimension(220, 38));
        FabricaDaisyUI.estilizarCampo(cmbFiltroTipoMov);
        cmbFiltroTipoMov.addActionListener(e -> filtrarTablaMovimientos());

        panelFiltros.add(new JLabel("Tipo de Movimiento:"));
        panelFiltros.add(cmbFiltroTipoMov);

        JPanel panelAccionesDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelAccionesDer.setOpaque(false);

        JButton btnExportarMov = FabricaDaisyUI.crearBotonExportarCsvIcono(e ->
                ExportadorCSV.exportarTabla(this, tablaMovimientos, "Historial_Movimientos_Inventario"));
        JButton btnLimpiarFiltroMov = FabricaDaisyUI.crearBotonLimpiarFiltros(e -> {
            cmbFiltroTipoMov.setSelectedIndex(0);
            filtrarTablaMovimientos();
        });

        JButton btnRefrescarMov = FabricaDaisyUI.crearBotonRefrescarIcono(e -> cargarTablaMovimientos());

        panelAccionesDer.add(btnExportarMov);
        panelAccionesDer.add(btnLimpiarFiltroMov);
        panelAccionesDer.add(btnRefrescarMov);

        panelBarra.add(panelFiltros, BorderLayout.WEST);
        panelBarra.add(panelAccionesDer, BorderLayout.EAST);

        panel.add(panelBarra, BorderLayout.NORTH);

        String[] cols = {"ID Mov.", "Fecha y Hora", "ID Prod.", "Producto", "Cantidad", "Tipo de Movimiento"};
        modeloMovimientos.setColumnIdentifiers(cols);
        tablaMovimientos.setModel(modeloMovimientos);
        tablaMovimientos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tablaMovimientos);

        TableColumnModel cm = tablaMovimientos.getColumnModel();
        cm.getColumn(0).setPreferredWidth(65);
        cm.getColumn(0).setMaxWidth(80);
        cm.getColumn(1).setPreferredWidth(160);
        cm.getColumn(2).setPreferredWidth(70);
        cm.getColumn(3).setPreferredWidth(230);
        cm.getColumn(4).setPreferredWidth(90);
        cm.getColumn(5).setPreferredWidth(170);

        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        cm.getColumn(0).setCellRenderer(renderCentro);
        cm.getColumn(1).setCellRenderer(renderCentro);
        cm.getColumn(2).setCellRenderer(renderCentro);
        cm.getColumn(4).setCellRenderer(renderCentro);

        cm.getColumn(5).setCellRenderer(new RenderizadorTipoMovimiento());

        JScrollPane scroll = new JScrollPane(tablaMovimientos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void cargarCategorias() {
        boolean prev = cargandoFiltros;
        cargandoFiltros = true;
        mapaCategorias.clear();
        cmbFiltroCategoria.removeAllItems();
        cmbFiltroCategoria.addItem("Todas las categorías");
        try {
            for (Categorias c : categoriaService.listar()) {
                mapaCategorias.put(c.getIdCat(), c.getNombreCat());
                cmbFiltroCategoria.addItem(c.getNombreCat());
            }
        } catch (Exception ignored) {
        }
        cargandoFiltros = prev;
    }

    private void cargarProductosEnCombo() {
        cmbProductos.removeAllItems();
        idsProductos.clear();
        listaProductosMemoria.clear();

        try {
            List<Productos> lista = productoService.listar();
            listaProductosMemoria.addAll(lista);
            for (Productos p : lista) {
                idsProductos.add(p.getIdPro());
                cmbProductos.addItem("[" + p.getCodigoPro() + "] " + p.getNombrePro());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 300px; font-family: Segoe UI, sans-serif;'>Error al cargar catálogo de productos: " + ex.getMessage() + "</body></html>",
                    "Error de Carga",
                    JOptionPane.ERROR_MESSAGE,
                    Icons.x(32));
        }
        seleccionarProductoEnCombo();
    }

    private void seleccionarProductoEnCombo() {
        int idx = cmbProductos.getSelectedIndex();
        if (idx == -1 || idx >= listaProductosMemoria.size()) {
            lblDetalleCategoria.setText("Categoría: -");
            lblDetallePrecio.setText("Precio Unitario: -");
            lblDetalleExistencia.setText("Existencia Actual: -");
            return;
        }
        Productos p = listaProductosMemoria.get(idx);
        String nomCat = mapaCategorias.getOrDefault(p.getIdCatPro(), "General");
        BigDecimal exist = p.getExistenciaPro() != null ? p.getExistenciaPro() : BigDecimal.ZERO;
        BigDecimal precio = p.getPrecioPro() != null ? p.getPrecioPro() : BigDecimal.ZERO;

        lblDetalleCategoria.setText("Categoría: " + nomCat);
        lblDetalleCategoria.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #475569; [dark]foreground: #bd93f9");

        lblDetallePrecio.setText("Precio Unitario: Q" + precio.setScale(2).toPlainString());
        lblDetallePrecio.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");

        lblDetalleExistencia.setText("Existencia Actual: " + exist.stripTrailingZeros().toPlainString() + " unid.");
        lblDetalleExistencia.putClientProperty(FabricaDaisyUI.PROPIEDAD_ESTILO, "[light]foreground: #047857; [dark]foreground: #50fa7b; font: bold 13");
        validarEstadoBotonConfirmar();
    }

    private void seleccionarProductoPorId(int idPro) {
        for (int i = 0; i < idsProductos.size(); i++) {
            if (idsProductos.get(i) == idPro) {
                if (cmbProductos.getSelectedIndex() != i) {
                    cmbProductos.setSelectedIndex(i);
                }
                break;
            }
        }
    }

    private void abastecerStock() {
        int idx = cmbProductos.getSelectedIndex();
        if (idx == -1 || idx >= idsProductos.size()) {
            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>Seleccione un producto para realizar el abastecimiento.</body></html>",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE,
                    Icons.triangleAlert(32));
            return;
        }
        String texto = txtCantidadIngreso.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>Ingrese la cantidad en unidades a abastecer.</body></html>",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE,
                    Icons.triangleAlert(32));
            txtCantidadIngreso.requestFocus();
            return;
        }
        BigDecimal cantidad;
        try {
            cantidad = new BigDecimal(texto);
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this,
                        "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>La cantidad a abastecer debe ser estrictamente mayor a cero.</body></html>",
                        "Validación",
                        JOptionPane.WARNING_MESSAGE,
                        Icons.triangleAlert(32));
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 280px; font-family: Segoe UI, sans-serif;'>Ingrese un número entero válido para la cantidad.</body></html>",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE,
                    Icons.triangleAlert(32));
            return;
        }

        int idPro = idsProductos.get(idx);
        Productos prod = listaProductosMemoria.get(idx);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html><body style='width: 320px; font-family: Segoe UI, sans-serif;'>"
                        + "¿Desea registrar el abastecimiento de <b>" + cantidad.stripTrailingZeros().toPlainString() + " unidades</b> para:<br/>"
                        + "<b>" + prod.getNombrePro() + "</b> [" + prod.getCodigoPro() + "]?</body></html>",
                "Confirmar Abastecimiento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                Icons.helpCircle(32)
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            inventarioService.abastecer(idPro, cantidad);
            FabricaDaisyUI.mostrarToastExito(this, "Stock abastecido con éxito (+ " + cantidad.stripTrailingZeros().toPlainString() + " unid.)");
            txtCantidadIngreso.setText("");
            cargarProductosEnCombo();
            seleccionarProductoPorId(idPro);
            cargarTablaProductos();
            cargarTablaMovimientos();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 320px; font-family: Segoe UI, sans-serif;'>Error al registrar abastecimiento: " + ex.getMessage() + "</body></html>",
                    "Error de Abastecimiento",
                    JOptionPane.ERROR_MESSAGE,
                    Icons.x(32));
        }
    }

    private void cargarTablaProductos() {
        try {
            List<Productos> fresca = productoService.listar();
            listaProductosMemoria.clear();
            listaProductosMemoria.addAll(fresca);
        } catch (Exception ignored) {
        }
        filtrarTablaProductos();
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String descompuesto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return descompuesto.replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    private void filtrarTablaProductos() {
        if (cargandoFiltros) {
            return;
        }
        String criterio = normalizarTexto(txtBuscar.getText());
        String catFiltro = cmbFiltroCategoria.getSelectedItem() != null ? cmbFiltroCategoria.getSelectedItem().toString() : "Todas las categorías";
        int estadoIdx = cmbFiltroEstado.getSelectedIndex();

        modeloProductos.setRowCount(0);
        for (Productos p : listaProductosMemoria) {
            String categoria = mapaCategorias.getOrDefault(p.getIdCatPro(), "General");
            BigDecimal exist = p.getExistenciaPro() != null ? p.getExistenciaPro() : BigDecimal.ZERO;

            String estadoStock;
            if (exist.compareTo(BigDecimal.ZERO) <= 0) {
                estadoStock = "Agotado";
            } else if (exist.compareTo(new BigDecimal(15)) <= 0) {
                estadoStock = "Bajo Stock";
            } else {
                estadoStock = "Óptimo";
            }

            boolean matchTexto = criterio.isEmpty()
                    || normalizarTexto(p.getNombrePro()).contains(criterio)
                    || normalizarTexto(p.getCodigoPro()).contains(criterio)
                    || String.valueOf(p.getIdPro()).contains(criterio)
                    || normalizarTexto(categoria).contains(criterio)
                    || normalizarTexto(estadoStock).contains(criterio);

            boolean matchCat = catFiltro.startsWith("Todas") || categoria.equalsIgnoreCase(catFiltro);

            boolean matchEstado = true;
            if (estadoIdx == 1) {
                matchEstado = "Óptimo".equals(estadoStock);
            } else if (estadoIdx == 2) {
                matchEstado = "Bajo Stock".equals(estadoStock);
            } else if (estadoIdx == 3) {
                matchEstado = "Agotado".equals(estadoStock);
            }

            if (matchTexto && matchCat && matchEstado) {
                modeloProductos.addRow(new Object[]{
                        p.getIdPro(),
                        p.getCodigoPro(),
                        p.getNombrePro(),
                        categoria,
                        p.getPrecioPro() != null ? "Q" + p.getPrecioPro().setScale(2).toPlainString() : "Q0.00",
                        exist.stripTrailingZeros().toPlainString(),
                        estadoStock
                });
            }
        }
    }

    private void cargarTablaMovimientos() {
        filtrarTablaMovimientos();
    }

    private void filtrarTablaMovimientos() {
        modeloMovimientos.setRowCount(0);
        Map<Integer, String> nombreProductos = new HashMap<>();
        for (Productos p : listaProductosMemoria) {
            nombreProductos.put(p.getIdPro(), p.getNombrePro() + " [" + p.getCodigoPro() + "]");
        }

        String tipoFiltro = cmbFiltroTipoMov.getSelectedItem() != null ? cmbFiltroTipoMov.getSelectedItem().toString() : "TODOS LOS MOVIMIENTOS";
        String normFiltro = tipoFiltro.toUpperCase().trim();

        try {
            List<InventarioMovimientos> lista = inventarioService.listar();
            for (InventarioMovimientos m : lista) {
                String tipo = m.getTipoMovimientoImo() != null ? m.getTipoMovimientoImo().toUpperCase().trim() : "";
                if (!normFiltro.startsWith("TODOS")) {
                    if ("VENTA".equals(normFiltro)) {
                        if (!"VENTA".equals(tipo)) {
                            continue;
                        }
                    } else if (!tipo.contains(normFiltro)) {
                        continue;
                    }
                }
                String nomProd = nombreProductos.getOrDefault(m.getIdProImo(), "Producto #" + m.getIdProImo());
                String fecha = m.getFechaImo() != null ? m.getFechaImo().format(formateadorFecha) : "";
                modeloMovimientos.addRow(new Object[]{
                        m.getIdImo(),
                        fecha,
                        m.getIdProImo(),
                        nomProd,
                        m.getCantidadImo() != null ? m.getCantidadImo().stripTrailingZeros().toPlainString() : "0",
                        m.getTipoMovimientoImo() != null ? m.getTipoMovimientoImo() : ""
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "<html><body style='width: 300px; font-family: Segoe UI, sans-serif;'>Error al cargar movimientos: " + ex.getMessage() + "</body></html>",
                    "Error de Carga",
                    JOptionPane.ERROR_MESSAGE,
                    Icons.x(32));
        }
    }

    private void refrescarTodo() {
        cargandoFiltros = true;
        cargarCategorias();
        cargarProductosEnCombo();
        cargandoFiltros = false;
        cargarTablaProductos();
        cargarTablaMovimientos();
        FabricaDaisyUI.mostrarToastInfo(this, "Datos de inventario actualizados");
    }

    private void validarEstadoBotonConfirmar() {
        int idx = cmbProductos.getSelectedIndex();
        String cantStr = txtCantidadIngreso.getText().trim();
        boolean cantidadValida = false;
        if (!cantStr.isEmpty()) {
            try {
                int c = Integer.parseInt(cantStr);
                cantidadValida = c > 0;
            } catch (NumberFormatException ignored) {
            }
        }
        btnConfirmar.setEnabled(idx != -1 && cantidadValida);
    }

    private void limpiarFormulario() {
        txtCantidadIngreso.setText("");
        if (cmbProductos.getItemCount() > 0) {
            cmbProductos.setSelectedIndex(0);
        }
        validarEstadoBotonConfirmar();
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }

    private static class RenderizadorEstadoStock extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String val = value != null ? value.toString() : "";
            Color colorTexto;
            Color colorFondo;

            if ("Agotado".equalsIgnoreCase(val)) {
                colorTexto = new Color(239, 68, 68);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(60, 25, 30) : new Color(254, 226, 226);
            } else if ("Bajo Stock".equalsIgnoreCase(val)) {
                colorTexto = new Color(217, 119, 6);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(60, 42, 20) : new Color(254, 243, 199);
            } else {
                colorTexto = new Color(16, 185, 129);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(20, 55, 40) : new Color(220, 252, 231);
            }

            JLabel badge = new JLabel(val) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(colorFondo);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(colorTexto);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setForeground(colorTexto);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));

            panel.add(badge);
            return panel;
        }
    }

    private static class RenderizadorTipoMovimiento extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String val = value != null ? value.toString() : "";
            Color colorTexto;
            Color colorFondo;

            if ("ABASTECIMIENTO".equalsIgnoreCase(val)) {
                colorTexto = new Color(16, 185, 129);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(20, 55, 40) : new Color(220, 252, 231);
            } else if ("VENTA".equalsIgnoreCase(val)) {
                colorTexto = new Color(59, 130, 246);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(30, 41, 59) : new Color(239, 246, 255);
            } else if (val.contains("CANJE")) {
                colorTexto = new Color(168, 85, 247);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(50, 30, 65) : new Color(243, 232, 255);
            } else if (val.contains("REVERSION")) {
                colorTexto = new Color(217, 119, 6);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(60, 42, 20) : new Color(254, 243, 199);
            } else {
                colorTexto = new Color(100, 116, 139);
                colorFondo = TemaGestor.esModoOscuro() ? new Color(40, 45, 55) : new Color(241, 245, 249);
            }

            JLabel badge = new JLabel(val) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(colorFondo);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(colorTexto);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setForeground(colorTexto);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));

            panel.add(badge);
            return panel;
        }
    }
}
