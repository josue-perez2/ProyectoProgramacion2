package vista;

import model.Cliente;
import model.DetalleMenu;
import model.DetallesPedido;
import model.DetalleSandwich;
import model.ItemVenta;
import model.MenuCompleto;
import model.Menus;
import model.Pedidos;
import model.ProductoIndividual;
import model.Productos;
import model.Sandwich;
import service.ClienteService;
import service.DetalleMenuService;
import service.DetallePedidoService;
import service.DetalleSandwichService;
import service.MenuService;
import service.PedidoService;
import service.ProductoService;
import service.SandwichService;
import util.ExportadorCSV;
import util.FormatoTexto;
import vista.util.Actualizable;
import vista.util.FabricaDaisyUI;
import vista.util.GestorVentanas;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoView extends JFrame implements Actualizable {

    private static final String TIPO_SANDWICH = "S";
    private static final String TIPO_MENU = "M";
    private static final String TIPO_PRODUCTO = "P";

    private final PedidoService pedidoService;
    private final DetallePedidoService detallePedidoService;
    private final ClienteService clienteService;
    private final SandwichService sandwichService;
    private final MenuService menuService;
    private final ProductoService productoService;
    private final DetalleSandwichService detalleSandwichService;
    private final DetalleMenuService detalleMenuService;

    private final Window parent;
    private final Cliente clienteInicial;
    private Cliente clienteActual = null;
    private Integer idPedidoSeleccionado = null;

    private final Map<Integer, Productos> cacheProductos = new HashMap<>();
    private final Map<Integer, Sandwich> cacheSandwich = new HashMap<>();
    private final Map<Integer, Menus> cacheMenu = new HashMap<>();
    private final Map<Integer, List<DetalleSandwich>> cacheRecetasSandwich = new HashMap<>();
    private final Map<Integer, List<DetalleMenu>> cacheComponentesMenu = new HashMap<>();
    private final Map<Integer, String> cacheNombresClientes = new HashMap<>();

    private static class ItemDetalleBorrador {
        final String tipoCodigo;
        final int idItem;
        final String nombreItem;
        BigDecimal cantidad;
        final BigDecimal precioUnitario;
        BigDecimal subtotal;
        int puntosGenerados;

        ItemDetalleBorrador(String tipoCodigo, int idItem, String nombreItem, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal, int puntosGenerados) {
            this.tipoCodigo = tipoCodigo;
            this.idItem = idItem;
            this.nombreItem = nombreItem;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
            this.puntosGenerados = puntosGenerados;
        }
    }

    private final List<ItemDetalleBorrador> itemsBorrador = new ArrayList<>();
    private boolean ignorarEventosCombo = false;

    private final JLabel lblModoPedido = new JLabel("Nuevo Pedido (Borrador)");
    private final JTextField txtDpiCliente = FabricaDaisyUI.crearCampoTexto("DPI", 13);
    private final JButton btnBuscarCliente = FabricaDaisyUI.crearBotonPrimario("Buscar", Icons.search(16), e -> buscarClientePorDpi());
    private final JButton btnNuevoCliente = FabricaDaisyUI.crearBotonAcento("Registrar", Icons.plus(16), e -> registrarNuevoCliente());
    private final JLabel lblNombreCliente = new JLabel("Ningún cliente seleccionado");
    private final JLabel lblSaldoPuntos = new JLabel("Saldo: 0 pts");
    private final JLabel lblTelefonoCliente = new JLabel("Tel: -");

    private final JTextField txtFecha = new JTextField();
    private final JTextField txtTotal = new JTextField();
    private final JTextField txtPuntosObtenidos = new JTextField();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Pendiente", "Cobrado", "Anulado"});

    private final JComboBox<String> cmbTipoItem = new JComboBox<>(new String[]{"Sándwich", "Menú", "Producto"});
    private final JTextField txtBuscarItem = FabricaDaisyUI.crearCampoTexto("Filtrar item...", 14);
    private final JComboBox<String> cmbItem = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField();
    private final JTextField txtPrecioUnitario = new JTextField();
    private final JLabel lblStockDisponible = new JLabel("Stock: -");

    private final JButton btnGuardar = FabricaDaisyUI.crearBotonPrimario("Guardar Pedido", Icons.save(16), null);
    private final JButton btnCobrar = FabricaDaisyUI.crearBotonSecundario("Cobrar / Pagar", Icons.creditCard(16), null);
    private final JButton btnVerFactura = FabricaDaisyUI.crearBotonAcento("Ver Factura", Icons.receipt(16), null);
    private final JButton btnModificar = FabricaDaisyUI.crearBotonNeutral("Modificar", Icons.edit(16), null);
    private final JButton btnEliminar = FabricaDaisyUI.crearBotonPeligro("Eliminar", Icons.trash(16), null);
    private final JButton btnLimpiar = FabricaDaisyUI.crearBotonNeutral("Nuevo / Limpiar", Icons.broom(16), null);
    private final JButton btnRegresar = FabricaDaisyUI.crearBotonNeutral("Volver", Icons.arrowLeft(16), null);

    private final JButton btnAgregarItem = FabricaDaisyUI.crearBotonPrimario("Agregar", Icons.plus(14), null);
    private final JButton btnQuitarItem = FabricaDaisyUI.crearBotonPeligro("Quitar", Icons.trash(14), null);

    private static class OpcionCatalogo {
        final int id;
        final String nombre;
        final BigDecimal precio;

        OpcionCatalogo(int id, String nombre, BigDecimal precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }
    }

    private final List<OpcionCatalogo> catalogoSandwich = new ArrayList<>();
    private final List<OpcionCatalogo> catalogoMenu = new ArrayList<>();
    private final List<OpcionCatalogo> catalogoProducto = new ArrayList<>();
    private final List<OpcionCatalogo> catalogoVisible = new ArrayList<>();

    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JTextField txtBuscarPedido = FabricaDaisyUI.crearCampoTexto("Buscar pedido...", 18);
    private TableRowSorter<DefaultTableModel> clasificadorPedidos;

    private final DefaultTableModel modeloPedidos = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaPedidos = new JTable(modeloPedidos);

    private final DefaultTableModel modeloDetalle = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaDetalle = new JTable(modeloDetalle);

    public PedidoView() {
        this(null, null);
    }

    public PedidoView(Window parent) {
        this(parent, null);
    }

    public PedidoView(Window parent, Cliente clienteInicial) {
        super("Gestión de Pedidos");
        this.parent = parent;
        this.clienteInicial = clienteInicial;
        this.pedidoService = new PedidoService();
        this.detallePedidoService = new DetallePedidoService();
        this.clienteService = new ClienteService();
        this.sandwichService = new SandwichService();
        this.menuService = new MenuService();
        this.productoService = new ProductoService();
        this.detalleSandwichService = new DetalleSandwichService();
        this.detalleMenuService = new DetalleMenuService();

        GestorVentanas.registrarVentana(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                GestorVentanas.desregistrarVentana(PedidoView.class);
            }
        });

        iniciarComponentes();
        cargarCatalogosItems();
        actualizarComboItems();
        cargarTablaPedidos();
        if (clienteInicial != null) {
            asignarClienteActual(clienteInicial);
        } else {
            asignarClienteActual(null);
        }
        actualizarEstadoBotones(false, false);
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1160, 740));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedorNorte = new JPanel(new GridLayout(1, 2, 14, 0));
        panelContenedorNorte.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));
        panelContenedorNorte.setOpaque(false);

        ((AbstractDocument) txtDpiCliente.getDocument()).setDocumentFilter(FormatoTexto.filtroDpi());
        txtDpiCliente.setPreferredSize(new Dimension(160, 36));
        txtDpiCliente.addActionListener(e -> buscarClientePorDpi());

        btnBuscarCliente.setPreferredSize(new Dimension(110, 36));
        btnNuevoCliente.setPreferredSize(new Dimension(125, 36));

        JPanel panelDpiFila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        panelDpiFila.setOpaque(false);
        JLabel lblDpiTag = new JLabel("DPI:");
        lblDpiTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelDpiFila.add(lblDpiTag);
        panelDpiFila.add(txtDpiCliente);
        panelDpiFila.add(btnBuscarCliente);
        panelDpiFila.add(btnNuevoCliente);

        lblModoPedido.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblModoPedido.setForeground(TemaGestor.esModoOscuro() ? new Color(139, 233, 253) : new Color(37, 99, 235));
        panelDpiFila.add(Box.createHorizontalStrut(10));
        panelDpiFila.add(lblModoPedido);

        JPanel panelInfoCliente = new JPanel(new GridLayout(2, 2, 8, 4));
        panelInfoCliente.setOpaque(false);
        panelInfoCliente.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, TemaGestor.esModoOscuro() ? new Color(68, 71, 90) : new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 4, 6, 4)
        ));

        lblNombreCliente.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSaldoPuntos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSaldoPuntos.setForeground(new Color(16, 185, 129));
        lblTelefonoCliente.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panelInfoCliente.add(lblNombreCliente);
        panelInfoCliente.add(lblSaldoPuntos);
        panelInfoCliente.add(lblTelefonoCliente);

        JPanel panelDatosPedido = new JPanel(new GridLayout(2, 2, 12, 8));
        panelDatosPedido.setOpaque(false);

        txtFecha.setPreferredSize(new Dimension(160, 36));
        txtTotal.setText("0.00");
        txtTotal.setPreferredSize(new Dimension(140, 36));
        txtPuntosObtenidos.setText("0");
        txtPuntosObtenidos.setPreferredSize(new Dimension(140, 36));

        FabricaDaisyUI.aplicarCampoEstatico(txtFecha);
        FabricaDaisyUI.aplicarCampoEstatico(txtTotal);
        FabricaDaisyUI.aplicarCampoEstatico(txtPuntosObtenidos);

        cmbEstado.setPreferredSize(new Dimension(160, 36));
        FabricaDaisyUI.estilizarCampo(cmbEstado);

        panelDatosPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Estado del Pedido:", cmbEstado));
        panelDatosPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Fecha y Hora:", txtFecha));
        panelDatosPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Total a Pagar (Q):", txtTotal));
        panelDatosPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Puntos que Generará:", txtPuntosObtenidos));

        JPanel panelCamposPedido = new JPanel(new BorderLayout(8, 8));
        panelCamposPedido.setOpaque(false);
        panelCamposPedido.add(panelDpiFila, BorderLayout.NORTH);
        panelCamposPedido.add(panelInfoCliente, BorderLayout.CENTER);
        panelCamposPedido.add(panelDatosPedido, BorderLayout.SOUTH);

        JPanel tarjetaPedido = FabricaDaisyUI.crearTarjetaSeccion(
                "Cliente y Datos del Pedido",
                panelCamposPedido
        );
        panelContenedorNorte.add(tarjetaPedido);

        JPanel panelDetalleContenedor = new JPanel(new BorderLayout(8, 8));
        panelDetalleContenedor.setOpaque(false);

        JPanel panelDetalleCampos = new JPanel(new GridLayout(3, 2, 12, 8));
        panelDetalleCampos.setOpaque(false);

        cmbTipoItem.setPreferredSize(new Dimension(150, 36));
        cmbTipoItem.addActionListener(e -> {
            txtBuscarItem.setText("");
            actualizarComboItems();
        });

        txtBuscarItem.setPreferredSize(new Dimension(180, 36));
        txtBuscarItem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarComboItems();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarComboItems();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarComboItems();
            }
        });

        cmbItem.setPreferredSize(new Dimension(240, 36));
        cmbItem.addActionListener(e -> {
            if (!ignorarEventosCombo) {
                actualizarPrecioUnitario();
            }
        });

        txtPrecioUnitario.setText("0.00");
        txtPrecioUnitario.setPreferredSize(new Dimension(110, 36));
        FabricaDaisyUI.aplicarCampoEstatico(txtPrecioUnitario);

        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(85, 36));

        FabricaDaisyUI.estilizarCampo(cmbTipoItem);
        FabricaDaisyUI.estilizarCampo(cmbItem);
        FabricaDaisyUI.estilizarCampo(txtCantidad);

        JPanel panelEspacioVacio = new JPanel();
        panelEspacioVacio.setOpaque(false);

        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Tipo de Item:", cmbTipoItem));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Filtrar por nombre:", txtBuscarItem));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Item Seleccionado:", cmbItem));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Cantidad:", txtCantidad));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Precio Unitario (Q):", txtPrecioUnitario));
        panelDetalleCampos.add(panelEspacioVacio);

        JPanel panelDetalleAcciones = new JPanel(new BorderLayout(8, 0));
        panelDetalleAcciones.setOpaque(false);

        lblStockDisponible.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStockDisponible.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        panelDetalleAcciones.add(lblStockDisponible, BorderLayout.WEST);

        JPanel panelBotonesDet = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotonesDet.setOpaque(false);

        btnAgregarItem.setPreferredSize(new Dimension(145, 34));
        btnQuitarItem.setPreferredSize(new Dimension(145, 34));
        btnAgregarItem.setIconTextGap(8);
        btnQuitarItem.setIconTextGap(8);

        FabricaDaisyUI.aplicarBotonPrimario(btnAgregarItem);
        FabricaDaisyUI.aplicarBotonPeligro(btnQuitarItem);

        btnAgregarItem.addActionListener(e -> agregarDetalle());
        btnQuitarItem.addActionListener(e -> quitarDetalle());

        panelBotonesDet.add(btnAgregarItem);
        panelBotonesDet.add(btnQuitarItem);
        panelDetalleAcciones.add(panelBotonesDet, BorderLayout.EAST);

        panelDetalleContenedor.add(panelDetalleCampos, BorderLayout.CENTER);
        panelDetalleContenedor.add(panelDetalleAcciones, BorderLayout.SOUTH);

        JPanel tarjetaDetalle = FabricaDaisyUI.crearTarjetaSeccion(
                "Agregar al Pedido",
                panelDetalleContenedor
        );
        panelContenedorNorte.add(tarjetaDetalle);

        add(panelContenedorNorte, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 14, 0));
        panelTablas.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        panelTablas.setOpaque(false);

        modeloPedidos.setColumnIdentifiers(new String[]{"ID", "Cliente", "Fecha", "Total", "Puntos", "Estado"});
        clasificadorPedidos = new TableRowSorter<>(modeloPedidos);
        tablaPedidos.setModel(modeloPedidos);
        tablaPedidos.setRowSorter(clasificadorPedidos);
        tablaPedidos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tablaPedidos);

        TableColumnModel colModel = tablaPedidos.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(45);
        colModel.getColumn(0).setMaxWidth(60);
        colModel.getColumn(1).setPreferredWidth(180);
        colModel.getColumn(2).setPreferredWidth(140);
        colModel.getColumn(3).setPreferredWidth(85);
        colModel.getColumn(4).setPreferredWidth(70);
        colModel.getColumn(5).setPreferredWidth(95);

        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFilaPedido();
            }
        });
        tablaPedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaPedidos.getSelectedRow();
                    if (fila != -1) {
                        int filaModelo = tablaPedidos.convertRowIndexToModel(fila);
                        int idPed = (int) modeloPedidos.getValueAt(filaModelo, 0);
                        Pedidos p = pedidoService.buscarPorId(idPed);
                        if (p != null && "C".equalsIgnoreCase(p.getEstadoPed())) {
                            verFactura();
                        }
                    }
                }
            }
        });
        tablaPedidos.getColumnModel().getColumn(5).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());
        JScrollPane scrollPedidos = new JScrollPane(tablaPedidos);
        scrollPedidos.setBorder(BorderFactory.createEmptyBorder());

        JPanel panelBarraBusquedaPedidos = new JPanel(new BorderLayout(8, 0));
        panelBarraBusquedaPedidos.setOpaque(false);
        panelBarraBusquedaPedidos.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel lblBuscarPed = new JLabel("Buscar:");
        lblBuscarPed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton btnLimpiarBusquedaPed = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(14), e -> {
            txtBuscarPedido.setText("");
            filtrarTablaPedidos();
        });
        btnLimpiarBusquedaPed.setPreferredSize(new Dimension(95, 34));

        panelBarraBusquedaPedidos.add(lblBuscarPed, BorderLayout.WEST);
        panelBarraBusquedaPedidos.add(txtBuscarPedido, BorderLayout.CENTER);
        panelBarraBusquedaPedidos.add(btnLimpiarBusquedaPed, BorderLayout.EAST);

        txtBuscarPedido.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarTablaPedidos();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTablaPedidos();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTablaPedidos();
            }
        });

        JPanel panelContenidoPedidos = new JPanel(new BorderLayout(6, 6));
        panelContenidoPedidos.setOpaque(false);
        panelContenidoPedidos.add(panelBarraBusquedaPedidos, BorderLayout.NORTH);
        panelContenidoPedidos.add(scrollPedidos, BorderLayout.CENTER);

        JButton btnExportarPedidos = FabricaDaisyUI.crearBotonExportarCsv(e ->
                ExportadorCSV.exportarTabla(this, tablaPedidos, "Historial_Pedidos"));
        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> {
            if (clienteActual != null) {
                Cliente ref = clienteService.buscarClientePorId(clienteActual.getIdCli());
                if (ref != null) {
                    asignarClienteActual(ref);
                }
            }
            cargarCatalogosItems();
            actualizarComboItems();
            cargarTablaPedidos();
        });

        JPanel panelAccionesPedidos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelAccionesPedidos.setOpaque(false);
        panelAccionesPedidos.add(btnExportarPedidos);
        panelAccionesPedidos.add(btnRefrescar);

        JPanel tarjetaPedidos = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Historial de Pedidos",
                panelAccionesPedidos,
                panelContenidoPedidos
        );

        modeloDetalle.setColumnIdentifiers(new String[]{"ID", "Tipo", "Item", "Cantidad", "Precio Unitario", "Subtotal", "Puntos"});
        tablaDetalle.setModel(modeloDetalle);
        tablaDetalle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tablaDetalle);

        TableColumnModel colDetModel = tablaDetalle.getColumnModel();
        colDetModel.getColumn(0).setPreferredWidth(45);
        colDetModel.getColumn(0).setMaxWidth(60);
        colDetModel.getColumn(1).setPreferredWidth(85);
        colDetModel.getColumn(2).setPreferredWidth(170);
        colDetModel.getColumn(3).setPreferredWidth(65);
        colDetModel.getColumn(4).setPreferredWidth(95);
        colDetModel.getColumn(5).setPreferredWidth(85);
        colDetModel.getColumn(6).setPreferredWidth(65);

        tablaDetalle.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean detalleSeleccionado = tablaDetalle.getSelectedRow() != -1;
                boolean bloqueado = esPedidoBloqueado();
                btnQuitarItem.setEnabled(detalleSeleccionado && !bloqueado);
            }
        });

        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        scrollDetalle.setBorder(BorderFactory.createEmptyBorder());

        JPanel tarjetaDetalleTabla = FabricaDaisyUI.crearTarjetaSeccion(
                "Detalle del Pedido",
                scrollDetalle
        );

        panelTablas.add(tarjetaPedidos);
        panelTablas.add(tarjetaDetalleTabla);

        add(panelTablas, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));
        panelBotones.setOpaque(false);

        btnGuardar.setPreferredSize(new Dimension(150, 38));
        btnCobrar.setPreferredSize(new Dimension(150, 38));
        btnVerFactura.setPreferredSize(new Dimension(145, 38));
        btnModificar.setPreferredSize(new Dimension(135, 38));
        btnEliminar.setPreferredSize(new Dimension(135, 38));
        btnLimpiar.setPreferredSize(new Dimension(150, 38));
        btnRegresar.setPreferredSize(new Dimension(130, 38));

        btnGuardar.setIconTextGap(8);
        btnCobrar.setIconTextGap(8);
        btnVerFactura.setIconTextGap(8);
        btnModificar.setIconTextGap(8);
        btnEliminar.setIconTextGap(8);
        btnLimpiar.setIconTextGap(8);
        btnRegresar.setIconTextGap(8);

        FabricaDaisyUI.aplicarBotonPrimario(btnGuardar);
        FabricaDaisyUI.aplicarBotonSecundario(btnCobrar);
        FabricaDaisyUI.aplicarBotonAcento(btnVerFactura);
        FabricaDaisyUI.aplicarBotonNeutral(btnModificar);
        FabricaDaisyUI.aplicarBotonPeligro(btnEliminar);
        FabricaDaisyUI.aplicarBotonNeutral(btnLimpiar);
        FabricaDaisyUI.aplicarBotonNeutral(btnRegresar);

        btnGuardar.addActionListener(e -> guardarPedido());
        btnCobrar.addActionListener(e -> abrirCobro());
        btnVerFactura.addActionListener(e -> verFactura());
        btnModificar.addActionListener(e -> actualizarPedido());
        btnEliminar.addActionListener(e -> eliminarPedido());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCobrar);
        panelBotones.add(btnVerFactura);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRegresar);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private boolean esPedidoBloqueado() {
        if (idPedidoSeleccionado == null) {
            return false;
        }
        Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
        if (p == null) {
            return false;
        }
        String estado = p.getEstadoPed();
        return "C".equalsIgnoreCase(estado) || "A".equalsIgnoreCase(estado);
    }

    private void actualizarEstadoBotones(boolean seleccionActiva, boolean bloqueado) {
        boolean clienteValido = clienteActual != null && "A".equalsIgnoreCase(clienteActual.getEstadoCli());
        if (!seleccionActiva) {
            lblModoPedido.setText("Nuevo Pedido (Borrador)");
            lblModoPedido.setForeground(TemaGestor.esModoOscuro() ? new Color(139, 233, 253) : new Color(37, 99, 235));
            btnGuardar.setEnabled(clienteValido && !itemsBorrador.isEmpty());
            btnCobrar.setEnabled(clienteValido && !itemsBorrador.isEmpty() && tieneTotalPositivo());
            btnModificar.setEnabled(false);
            btnEliminar.setEnabled(false);
            btnVerFactura.setEnabled(false);
            btnQuitarItem.setEnabled(tablaDetalle.getSelectedRow() != -1);
            cmbEstado.setEnabled(false);
            cmbEstado.setSelectedItem("Pendiente");
            btnAgregarItem.setEnabled(true);
        } else {
            lblModoPedido.setText("Pedido #" + idPedidoSeleccionado);
            lblModoPedido.setForeground(bloqueado ? Color.GRAY : (TemaGestor.esModoOscuro() ? new Color(80, 250, 123) : new Color(16, 185, 129)));
            btnGuardar.setEnabled(false);
            btnModificar.setEnabled(!bloqueado);
            btnEliminar.setEnabled(true);
            btnCobrar.setEnabled(!bloqueado && tieneTotalPositivo());
            boolean esCobrado = false;
            if (idPedidoSeleccionado != null) {
                Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
                esCobrado = p != null && "C".equalsIgnoreCase(p.getEstadoPed());
            }
            btnVerFactura.setEnabled(esCobrado);
            btnQuitarItem.setEnabled(false);
            cmbEstado.setEnabled(!bloqueado);
            btnAgregarItem.setEnabled(!bloqueado);
        }
        actualizarPrecioUnitario();
    }

    private boolean tieneTotalPositivo() {
        try {
            String txt = txtTotal.getText().trim();
            if (txt.startsWith("Q")) {
                txt = txt.substring(1).trim();
            }
            int paren = txt.indexOf('(');
            if (paren != -1) {
                txt = txt.substring(0, paren).trim();
            }
            BigDecimal total = new BigDecimal(txt);
            return total.compareTo(BigDecimal.ZERO) > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private void buscarClientePorDpi() {
        String textoDpi = txtDpiCliente.getText().trim();
        String soloDigitos = FormatoTexto.soloDigitos(textoDpi);
        if (soloDigitos.isEmpty()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Por favor ingrese el número de DPI del cliente a buscar.");
            return;
        }
        Cliente c = clienteService.buscarPorDpi(soloDigitos);
        if (c == null) {
            boolean opcion = FabricaDaisyUI.mostrarConfirmacion(
                    this,
                    "Cliente No Encontrado",
                    "No se encontró ningún cliente con DPI: " + FormatoTexto.formatearDpi(soloDigitos) + "\n\n¿Desea registrar al cliente ahora?"
            );
            if (opcion) {
                GestorVentanas.abrirOEnfocar(ClienteView.class, () -> new ClienteView(this, soloDigitos));
            }
            return;
        }
        if (!"A".equalsIgnoreCase(c.getEstadoCli())) {
            FabricaDaisyUI.mostrarError(
                    this,
                    "Cliente Inactivo",
                    "El cliente " + c.getNombreCli() + " se encuentra INACTIVO.\nNo es posible registrar pedidos a clientes inactivos."
            );
            asignarClienteActual(null);
            return;
        }
        asignarClienteActual(c);
        FabricaDaisyUI.mostrarToastExito(this, "Cliente: " + c.getNombreCli());
    }

    private void registrarNuevoCliente() {
        String textoDpi = FormatoTexto.soloDigitos(txtDpiCliente.getText().trim());
        GestorVentanas.abrirOEnfocar(ClienteView.class, () -> new ClienteView(this, textoDpi));
    }

    private void asignarClienteActual(Cliente c) {
        this.clienteActual = c;
        if (c != null) {
            txtDpiCliente.setText(c.getDpiCli() != null ? FormatoTexto.formatearDpi(c.getDpiCli()) : "");
            lblNombreCliente.setText("Cliente: " + c.getNombreCli());
            lblNombreCliente.setForeground(TemaGestor.esModoOscuro() ? new Color(248, 250, 252) : new Color(15, 23, 42));
            lblTelefonoCliente.setText("Tel: " + (c.getTelefonoCli() != null && !c.getTelefonoCli().trim().isEmpty() ? c.getTelefonoCli() : "S/T"));
            int saldo = c.getSaldoPuntoCli() != null ? c.getSaldoPuntoCli().intValue() : 0;
            lblSaldoPuntos.setText("Saldo: " + saldo + " pts");
            lblSaldoPuntos.setForeground(new Color(16, 185, 129));
        } else {
            lblNombreCliente.setText("Ningún cliente seleccionado");
            lblNombreCliente.setForeground(Color.GRAY);
            lblTelefonoCliente.setText("Tel: -");
            lblSaldoPuntos.setText("Saldo: 0 pts");
            lblSaldoPuntos.setForeground(Color.GRAY);
        }
        if (idPedidoSeleccionado == null) {
            boolean clienteValido = c != null && "A".equalsIgnoreCase(c.getEstadoCli());
            btnGuardar.setEnabled(clienteValido && !itemsBorrador.isEmpty());
            btnCobrar.setEnabled(clienteValido && !itemsBorrador.isEmpty() && tieneTotalPositivo());
        }
    }

    private void cargarCatalogosItems() {
        cacheProductos.clear();
        catalogoProducto.clear();
        try {
            for (Productos p : productoService.listar()) {
                cacheProductos.put(p.getIdPro(), p);
                if ("A".equalsIgnoreCase(p.getActivoPro())) {
                    catalogoProducto.add(new OpcionCatalogo(p.getIdPro(), p.getNombrePro(), p.getPrecioPro()));
                }
            }
        } catch (RuntimeException ignored) {
        }

        cacheSandwich.clear();
        cacheRecetasSandwich.clear();
        catalogoSandwich.clear();
        try {
            for (Sandwich s : sandwichService.listarActivos()) {
                cacheSandwich.put(s.getIdSan(), s);
                catalogoSandwich.add(new OpcionCatalogo(s.getIdSan(), s.getNombreSan(), s.getPrecioSan()));
                cacheRecetasSandwich.put(s.getIdSan(), detalleSandwichService.listarPorSandwich(s.getIdSan()));
            }
        } catch (RuntimeException ignored) {
        }

        cacheMenu.clear();
        cacheComponentesMenu.clear();
        catalogoMenu.clear();
        try {
            for (Menus m : menuService.listarActivos()) {
                cacheMenu.put(m.getIdMen(), m);
                catalogoMenu.add(new OpcionCatalogo(m.getIdMen(), m.getNombreMen(), m.getPrecioMen()));
                cacheComponentesMenu.put(m.getIdMen(), detalleMenuService.listarPorMenu(m.getIdMen()));
            }
        } catch (RuntimeException ignored) {
        }

        cacheNombresClientes.clear();
        try {
            for (Cliente c : clienteService.listar()) {
                cacheNombresClientes.put(c.getIdCli(), c.getNombreCli());
            }
        } catch (RuntimeException ignored) {
        }
    }

    private void actualizarComboItems() {
        ignorarEventosCombo = true;
        cmbItem.removeAllItems();
        catalogoVisible.clear();
        int tipoIndex = cmbTipoItem.getSelectedIndex();
        List<OpcionCatalogo> listaFuente;
        if (tipoIndex == 0) {
            listaFuente = catalogoSandwich;
        } else if (tipoIndex == 1) {
            listaFuente = catalogoMenu;
        } else {
            listaFuente = catalogoProducto;
        }
        String filtro = txtBuscarItem.getText().trim().toLowerCase();
        for (OpcionCatalogo op : listaFuente) {
            if (filtro.isEmpty() || op.nombre.toLowerCase().contains(filtro)) {
                catalogoVisible.add(op);
                cmbItem.addItem(op.nombre);
            }
        }
        if (cmbItem.getItemCount() > 0) {
            cmbItem.setSelectedIndex(0);
        }
        ignorarEventosCombo = false;
        actualizarPrecioUnitario();
    }

    private void actualizarPrecioUnitario() {
        if (ignorarEventosCombo) {
            return;
        }
        int itemIndex = cmbItem.getSelectedIndex();
        if (itemIndex == -1 || itemIndex >= catalogoVisible.size()) {
            txtPrecioUnitario.setText("0.00");
            lblStockDisponible.setText("Stock: -");
            btnAgregarItem.setEnabled(false);
            return;
        }
        int tipoIndex = cmbTipoItem.getSelectedIndex();
        String tipoCodigo = tipoIndex == 0 ? TIPO_SANDWICH : (tipoIndex == 1 ? TIPO_MENU : TIPO_PRODUCTO);
        OpcionCatalogo opcion = catalogoVisible.get(itemIndex);
        txtPrecioUnitario.setText(opcion.precio.toPlainString());
        actualizarIndicadorStock(tipoCodigo, opcion.id);
    }

    private void cargarTablaPedidos() {
        modeloPedidos.setRowCount(0);
        try {
            List<Pedidos> lista = pedidoService.listar();
            for (Pedidos p : lista) {
                String nombreCliente = obtenerNombreCliente(p.getIdCliPed());
                String fechaTexto = p.getFechaPed() != null ? p.getFechaPed().format(formateadorFecha) : "";
                String estadoDesc = "C".equalsIgnoreCase(p.getEstadoPed()) ? "PAGADO" :
                        ("A".equalsIgnoreCase(p.getEstadoPed()) ? "ANULADO" : "PENDIENTE");
                modeloPedidos.addRow(new Object[]{
                        p.getIdPed(),
                        nombreCliente,
                        fechaTexto,
                        p.getTotalPed(),
                        p.getPuntosObtenidosPed(),
                        estadoDesc
                });
            }
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "No se pudo cargar la lista de pedidos: " + ex.getMessage());
        }
    }

    private String obtenerNombreCliente(int idCli) {
        String nombre = cacheNombresClientes.get(idCli);
        if (nombre != null) {
            return nombre;
        }
        Cliente c = clienteService.buscarClientePorId(idCli);
        if (c != null) {
            cacheNombresClientes.put(idCli, c.getNombreCli());
            return c.getNombreCli();
        }
        return "Cliente #" + idCli;
    }

    private void seleccionarFilaPedido() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila == -1) {
            return;
        }
        int filaModelo = tablaPedidos.convertRowIndexToModel(fila);
        int idPed = (int) modeloPedidos.getValueAt(filaModelo, 0);
        Pedidos p = pedidoService.buscarPorId(idPed);
        if (p == null) {
            limpiarFormulario();
            return;
        }

        idPedidoSeleccionado = p.getIdPed();
        itemsBorrador.clear();
        Cliente c = clienteService.buscarClientePorId(p.getIdCliPed());
        asignarClienteActual(c);
        txtFecha.setText(p.getFechaPed() != null ? p.getFechaPed().format(formateadorFecha) : "");
        String estDesc = "C".equalsIgnoreCase(p.getEstadoPed()) ? "PAGADO" : ("A".equalsIgnoreCase(p.getEstadoPed()) ? "ANULADO" : "PENDIENTE");
        txtTotal.setText("Q" + p.getTotalPed().setScale(2, RoundingMode.HALF_UP).toPlainString() + " (" + estDesc + ")");
        txtPuntosObtenidos.setText(String.valueOf(p.getPuntosObtenidosPed()));
        cmbEstado.setSelectedItem(estadoCompleto(p.getEstadoPed()));

        cargarDetalle(idPed);
        boolean bloqueado = "C".equalsIgnoreCase(p.getEstadoPed()) || "A".equalsIgnoreCase(p.getEstadoPed());
        actualizarEstadoBotones(true, bloqueado);
    }

    private void cargarDetalle(int idPed) {
        modeloDetalle.setRowCount(0);
        try {
            List<DetallesPedido> detalles = detallePedidoService.listarPorPedido(idPed);
            for (DetallesPedido d : detalles) {
                modeloDetalle.addRow(new Object[]{
                        d.getIdDet(),
                        descripcionTipo(d.getTipoItemDet()),
                        obtenerNombreItem(d.getTipoItemDet(), d.getIdItemDet()),
                        d.getCantidadDet(),
                        d.getPrecioUnitarioDet(),
                        d.getSubTotalDet(),
                        d.getPuntosGeneradosDet()
                });
            }
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "No se pudo cargar el detalle del pedido: " + ex.getMessage());
        }
    }

    private void refrescarTablaDetalleBorrador() {
        modeloDetalle.setRowCount(0);
        int indice = 1;
        for (ItemDetalleBorrador d : itemsBorrador) {
            modeloDetalle.addRow(new Object[]{
                    indice++,
                    descripcionTipo(d.tipoCodigo),
                    d.nombreItem,
                    d.cantidad,
                    d.precioUnitario,
                    d.subtotal,
                    d.puntosGenerados
            });
        }
    }

    private String descripcionTipo(String tipo) {
        if (TIPO_SANDWICH.equals(tipo)) {
            return "Sándwich";
        }
        if (TIPO_MENU.equals(tipo)) {
            return "Menú";
        }
        return "Producto";
    }

    private String obtenerNombreItem(String tipo, int idItem) {
        if (TIPO_SANDWICH.equals(tipo)) {
            Sandwich s = cacheSandwich.get(idItem);
            return s != null ? s.getNombreSan() : "Sándwich #" + idItem;
        }
        if (TIPO_MENU.equals(tipo)) {
            Menus m = cacheMenu.get(idItem);
            return m != null ? m.getNombreMen() : "Menú #" + idItem;
        }
        Productos p = cacheProductos.get(idItem);
        return p != null ? p.getNombrePro() : "Producto #" + idItem;
    }

    private void guardarPedido() {
        if (clienteActual == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Debe buscar y seleccionar un cliente activo.");
            return;
        }
        if (!"A".equalsIgnoreCase(clienteActual.getEstadoCli())) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "El cliente seleccionado se encuentra inactivo.");
            return;
        }
        if (idPedidoSeleccionado == null) {
            if (itemsBorrador.isEmpty()) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Agregue al menos un producto o sándwich antes de guardar.");
                return;
            }
            String errorStock = verificarDisponibilidadStockMemoria(null, 0, null);
            if (errorStock != null) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente", errorStock);
                return;
            }
            try {
                BigDecimal totalFinal = BigDecimal.ZERO;
                int puntosFinal = 0;
                for (ItemDetalleBorrador item : itemsBorrador) {
                    totalFinal = totalFinal.add(item.subtotal);
                    puntosFinal += item.puntosGenerados;
                }

                Pedidos pedido = new Pedidos();
                pedido.setIdCliPed(clienteActual.getIdCli());
                pedido.setFechaPed(LocalDateTime.now());
                pedido.setTotalPed(totalFinal);
                pedido.setPuntosObtenidosPed(puntosFinal);
                pedido.setEstadoPed("P");

                pedidoService.insertar(pedido);
                int idGenerado = pedido.getIdPed();
                if (idGenerado <= 0) {
                    List<Pedidos> lista = pedidoService.listarPorCliente(clienteActual.getIdCli());
                    if (!lista.isEmpty()) {
                        idGenerado = lista.get(0).getIdPed();
                        pedido.setIdPed(idGenerado);
                    }
                }

                for (ItemDetalleBorrador item : itemsBorrador) {
                    DetallesPedido det = new DetallesPedido();
                    det.setIdPedDet(idGenerado);
                    det.setTipoItemDet(item.tipoCodigo);
                    det.setIdItemDet(item.idItem);
                    det.setCantidadDet(item.cantidad);
                    det.setPrecioUnitarioDet(item.precioUnitario);
                    det.setSubTotalDet(item.subtotal);
                    det.setPuntosGeneradosDet(item.puntosGenerados);
                    detallePedidoService.insertar(det);
                }

                itemsBorrador.clear();
                cargarCatalogosItems();
                cargarTablaPedidos();
                seleccionarEnTabla(idGenerado);
                FabricaDaisyUI.mostrarExito(this, "Pedido Guardado", "El pedido #" + idGenerado + " fue guardado como Pendiente.");
                GestorVentanas.notificarCambio("PEDIDO");
            } catch (RuntimeException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al guardar pedido: " + ex.getMessage());
            }
        }
    }

    private void actualizarPedido() {
        if (idPedidoSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un pedido de la tabla.");
            return;
        }
        if (esPedidoBloqueado()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Operación Bloqueada", "No se puede modificar un pedido que ya está pagado o anulado.");
            return;
        }
        if (clienteActual == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Debe buscar y seleccionar un cliente para el pedido.");
            return;
        }
        try {
            int idPed = idPedidoSeleccionado;
            Pedidos pedido = pedidoService.buscarPorId(idPed);
            if (pedido == null) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "El pedido no fue encontrado.");
                return;
            }
            String nuevoEstado = estadoAbreviado((String) cmbEstado.getSelectedItem());
            if ("C".equalsIgnoreCase(nuevoEstado) && !"C".equalsIgnoreCase(pedido.getEstadoPed())) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Acción Inválida", "Para cobrar un pedido use el botón 'Cobrar / Pagar'.");
                cmbEstado.setSelectedItem(estadoCompleto(pedido.getEstadoPed()));
                return;
            }
            pedido.setIdCliPed(clienteActual.getIdCli());
            pedido.setEstadoPed(nuevoEstado);

            pedidoService.actualizar(pedido);
            FabricaDaisyUI.mostrarToastExito(this, "Pedido actualizado correctamente.");
            cargarTablaPedidos();
            seleccionarEnTabla(idPed);
            GestorVentanas.notificarCambio("PEDIDO");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al actualizar pedido: " + ex.getMessage());
        }
    }

    private void eliminarPedido() {
        if (idPedidoSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un pedido de la tabla.");
            return;
        }
        if (esPedidoBloqueado()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Operación Bloqueada", "No se puede eliminar un pedido que ya ha sido pagado o anulado.");
            return;
        }
        boolean confirmar = FabricaDaisyUI.mostrarConfirmacion(this, "Confirmar Eliminación", "¿Desea eliminar el pedido seleccionado y su detalle?");
        if (confirmar) {
            try {
                int idPed = idPedidoSeleccionado;
                detallePedidoService.eliminarPorPedido(idPed);
                pedidoService.eliminar(idPed);
                FabricaDaisyUI.mostrarToastExito(this, "Pedido eliminado correctamente.");
                cargarTablaPedidos();
                limpiarFormulario();
                GestorVentanas.notificarCambio("PEDIDO");
            } catch (RuntimeException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al eliminar pedido: " + ex.getMessage());
            }
        }
    }

    private void agregarDetalle() {
        if (esPedidoBloqueado()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Operación Bloqueada", "No se pueden agregar items a un pedido cerrado.");
            return;
        }
        int itemIndex = cmbItem.getSelectedIndex();
        if (itemIndex == -1 || itemIndex >= catalogoVisible.size()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Debe seleccionar un item para agregar.");
            return;
        }

        BigDecimal cantidad;
        try {
            cantidad = new BigDecimal(txtCantidad.getText().trim());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "La cantidad debe ser mayor que cero.");
                return;
            }
        } catch (NumberFormatException ex) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "La cantidad debe ser un número válido.");
            return;
        }

        int tipoIndex = cmbTipoItem.getSelectedIndex();
        String tipoCodigo = tipoIndex == 0 ? TIPO_SANDWICH : (tipoIndex == 1 ? TIPO_MENU : TIPO_PRODUCTO);
        OpcionCatalogo opcion = catalogoVisible.get(itemIndex);
        int idItem = opcion.id;
        String nombreItem = opcion.nombre;
        BigDecimal precioUnitario = opcion.precio;

        BigDecimal stockDisponible = calcularStockDisponibleItem(tipoCodigo, idItem);
        if (stockDisponible.compareTo(BigDecimal.ZERO) <= 0) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Agotado", "El item seleccionado se encuentra agotado o sin ingredientes en inventario.");
            return;
        }
        if (cantidad.compareTo(stockDisponible) > 0) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente", "La cantidad (" + cantidad.stripTrailingZeros().toPlainString() + ") supera las unidades disponibles (" + stockDisponible.intValue() + ").");
            return;
        }

        ItemVenta itemVenta;
        if (TIPO_MENU.equals(tipoCodigo)) {
            itemVenta = new MenuCompleto(idItem, nombreItem, precioUnitario, cantidad);
        } else {
            itemVenta = new ProductoIndividual(idItem, nombreItem, precioUnitario, cantidad, tipoCodigo);
        }
        BigDecimal subtotal = itemVenta.calcularSubtotal();
        int puntosGenerados = itemVenta.calcularPuntos();

        if (idPedidoSeleccionado == null) {
            String errorStock = verificarDisponibilidadStockMemoria(tipoCodigo, idItem, cantidad);
            if (errorStock != null) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente", errorStock);
                return;
            }

            boolean encontrado = false;
            for (ItemDetalleBorrador it : itemsBorrador) {
                if (it.tipoCodigo.equals(tipoCodigo) && it.idItem == idItem) {
                    it.cantidad = it.cantidad.add(cantidad);
                    if (TIPO_MENU.equals(tipoCodigo)) {
                        ItemVenta iv = new MenuCompleto(idItem, nombreItem, precioUnitario, it.cantidad);
                        it.subtotal = iv.calcularSubtotal();
                        it.puntosGenerados = iv.calcularPuntos();
                    } else {
                        ItemVenta iv = new ProductoIndividual(idItem, nombreItem, precioUnitario, it.cantidad, tipoCodigo);
                        it.subtotal = iv.calcularSubtotal();
                        it.puntosGenerados = iv.calcularPuntos();
                    }
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                itemsBorrador.add(new ItemDetalleBorrador(tipoCodigo, idItem, nombreItem, cantidad, precioUnitario, subtotal, puntosGenerados));
            }

            refrescarTablaDetalleBorrador();
            recalcularTotalesBorrador();
            txtCantidad.setText("1");
            FabricaDaisyUI.mostrarToastExito(this, "Agregado: " + nombreItem);
            boolean clienteValido = clienteActual != null && "A".equalsIgnoreCase(clienteActual.getEstadoCli());
            btnGuardar.setEnabled(clienteValido && !itemsBorrador.isEmpty());
            btnCobrar.setEnabled(clienteValido && !itemsBorrador.isEmpty() && tieneTotalPositivo());
            return;
        }

        String errorStock = verificarDisponibilidadStockPedidoBD(idPedidoSeleccionado, tipoCodigo, idItem, cantidad);
        if (errorStock != null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente", errorStock);
            return;
        }

        try {
            int idPed = idPedidoSeleccionado;
            DetallesPedido detalle = new DetallesPedido();
            detalle.setIdPedDet(idPed);
            detalle.setTipoItemDet(tipoCodigo);
            detalle.setIdItemDet(idItem);
            detalle.setCantidadDet(cantidad);
            detalle.setPrecioUnitarioDet(precioUnitario);
            detalle.setSubTotalDet(subtotal);
            detalle.setPuntosGeneradosDet(puntosGenerados);

            detallePedidoService.insertar(detalle);
            recalcularTotalesPedido(idPed);
            cargarDetalle(idPed);
            cargarTablaPedidos();
            seleccionarEnTabla(idPed);
            txtCantidad.setText("1");
            FabricaDaisyUI.mostrarToastExito(this, "Item agregado al pedido.");
            GestorVentanas.notificarCambio("PEDIDO");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al agregar item: " + ex.getMessage());
        }
    }

    private void recalcularTotalesBorrador() {
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        int nuevosPuntos = 0;
        for (ItemDetalleBorrador it : itemsBorrador) {
            nuevoTotal = nuevoTotal.add(it.subtotal);
            nuevosPuntos += it.puntosGenerados;
        }
        txtTotal.setText(nuevoTotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
        txtPuntosObtenidos.setText(String.valueOf(nuevosPuntos));
    }

    private void quitarDetalle() {
        if (esPedidoBloqueado()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Operación Bloqueada", "No se pueden quitar items de un pedido cerrado.");
            return;
        }
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione una fila del detalle para quitar.");
            return;
        }

        if (idPedidoSeleccionado == null) {
            if (fila < itemsBorrador.size()) {
                itemsBorrador.remove(fila);
                refrescarTablaDetalleBorrador();
                recalcularTotalesBorrador();
                boolean clienteValido = clienteActual != null && "A".equalsIgnoreCase(clienteActual.getEstadoCli());
                btnGuardar.setEnabled(clienteValido && !itemsBorrador.isEmpty());
                btnCobrar.setEnabled(clienteValido && !itemsBorrador.isEmpty() && tieneTotalPositivo());
                btnQuitarItem.setEnabled(false);
                FabricaDaisyUI.mostrarToastExito(this, "Item removido.");
            }
            return;
        }

        int idDet = (int) modeloDetalle.getValueAt(fila, 0);
        try {
            int idPed = idPedidoSeleccionado;
            detallePedidoService.eliminar(idDet);
            recalcularTotalesPedido(idPed);
            cargarDetalle(idPed);
            cargarTablaPedidos();
            seleccionarEnTabla(idPed);
            FabricaDaisyUI.mostrarToastExito(this, "Item quitado del pedido.");
            GestorVentanas.notificarCambio("PEDIDO");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al quitar item: " + ex.getMessage());
        }
    }

    private void recalcularTotalesPedido(int idPed) {
        List<DetallesPedido> lista = detallePedidoService.listarPorPedido(idPed);
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        int nuevosPuntos = 0;
        for (DetallesPedido d : lista) {
            nuevoTotal = nuevoTotal.add(d.getSubTotalDet());
            nuevosPuntos += d.getPuntosGeneradosDet();
        }

        Pedidos p = pedidoService.buscarPorId(idPed);
        if (p != null) {
            p.setTotalPed(nuevoTotal);
            p.setPuntosObtenidosPed(nuevosPuntos);
            pedidoService.actualizar(p);
            txtTotal.setText(nuevoTotal.toPlainString());
            txtPuntosObtenidos.setText(String.valueOf(nuevosPuntos));
        }
    }

    private void seleccionarEnTabla(Integer idPed) {
        if (idPed == null) {
            return;
        }
        for (int i = 0; i < modeloPedidos.getRowCount(); i++) {
            Object val = modeloPedidos.getValueAt(i, 0);
            if (val != null && val.toString().equals(idPed.toString())) {
                int filaVista = tablaPedidos.convertRowIndexToView(i);
                if (filaVista != -1) {
                    tablaPedidos.setRowSelectionInterval(filaVista, filaVista);
                    tablaPedidos.scrollRectToVisible(tablaPedidos.getCellRect(filaVista, 0, true));
                }
                return;
            }
        }
    }

    private void limpiarCamposSinDeseleccionar() {
        txtFecha.setText("");
        txtTotal.setText("0.00");
        txtPuntosObtenidos.setText("0");
        cmbEstado.setSelectedItem("Pendiente");
        txtCantidad.setText("1");
        itemsBorrador.clear();
        modeloDetalle.setRowCount(0);
        if (clienteInicial != null) {
            asignarClienteActual(clienteInicial);
        } else {
            asignarClienteActual(null);
        }
    }

    private void limpiarFormulario() {
        idPedidoSeleccionado = null;
        limpiarCamposSinDeseleccionar();
        tablaPedidos.clearSelection();
        actualizarEstadoBotones(false, false);
    }

    private void abrirCobro() {
        if (idPedidoSeleccionado == null) {
            if (clienteActual == null) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione o busque un cliente antes de cobrar.");
                return;
            }
            if (itemsBorrador.isEmpty()) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Agregue al menos un producto o sándwich al pedido antes de cobrar.");
                return;
            }
            String errorStock = verificarDisponibilidadStockMemoria(null, 0, null);
            if (errorStock != null) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente", errorStock);
                return;
            }
            try {
                BigDecimal totalFinal = BigDecimal.ZERO;
                int puntosFinal = 0;
                for (ItemDetalleBorrador item : itemsBorrador) {
                    totalFinal = totalFinal.add(item.subtotal);
                    puntosFinal += item.puntosGenerados;
                }

                Pedidos nuevoPed = new Pedidos();
                nuevoPed.setIdCliPed(clienteActual.getIdCli());
                nuevoPed.setFechaPed(LocalDateTime.now());
                nuevoPed.setTotalPed(totalFinal);
                nuevoPed.setPuntosObtenidosPed(puntosFinal);
                nuevoPed.setEstadoPed("P");

                pedidoService.insertar(nuevoPed);
                int idGenerado = nuevoPed.getIdPed();
                if (idGenerado <= 0) {
                    List<Pedidos> lista = pedidoService.listarPorCliente(clienteActual.getIdCli());
                    if (!lista.isEmpty()) {
                        idGenerado = lista.get(0).getIdPed();
                        nuevoPed.setIdPed(idGenerado);
                    }
                }

                for (ItemDetalleBorrador item : itemsBorrador) {
                    DetallesPedido det = new DetallesPedido();
                    det.setIdPedDet(idGenerado);
                    det.setTipoItemDet(item.tipoCodigo);
                    det.setIdItemDet(item.idItem);
                    det.setCantidadDet(item.cantidad);
                    det.setPrecioUnitarioDet(item.precioUnitario);
                    det.setSubTotalDet(item.subtotal);
                    det.setPuntosGeneradosDet(item.puntosGenerados);
                    detallePedidoService.insertar(det);
                }

                itemsBorrador.clear();
                cargarCatalogosItems();
                cargarTablaPedidos();
                seleccionarEnTabla(idGenerado);
                GestorVentanas.notificarCambio("PEDIDO");
                GestorVentanas.abrirOEnfocar(PagoView.class, () -> new PagoView(this, nuevoPed));
            } catch (RuntimeException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al procesar pedido previo al cobro: " + ex.getMessage());
            }
            return;
        }

        Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
        if (p == null) {
            FabricaDaisyUI.mostrarError(this, "Error", "El pedido no fue encontrado.");
            return;
        }
        if ("C".equalsIgnoreCase(p.getEstadoPed())) {
            boolean ver = FabricaDaisyUI.mostrarConfirmacion(this, "Pedido Ya Pagado", "El pedido ya se encuentra pagado.\n¿Desea ver su factura?");
            if (ver) {
                new FacturaView(this, idPedidoSeleccionado).setVisible(true);
            }
            return;
        }
        if (p.getTotalPed().compareTo(BigDecimal.ZERO) <= 0) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "No se puede cobrar un pedido con total Q0.00. Agregue items primero.");
            return;
        }

        String errorStock = verificarDisponibilidadStockPedidoBD(idPedidoSeleccionado, null, 0, null);
        if (errorStock != null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente para Cobro", errorStock);
            return;
        }

        GestorVentanas.abrirOEnfocar(PagoView.class, () -> new PagoView(this, p));
    }

    private void verFactura() {
        if (idPedidoSeleccionado == null) {
            return;
        }
        Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
        if (p == null || !"C".equalsIgnoreCase(p.getEstadoPed())) {
            return;
        }
        new FacturaView(this, idPedidoSeleccionado).setVisible(true);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }

    private String estadoAbreviado(String estadoCompleto) {
        if ("Cobrado".equalsIgnoreCase(estadoCompleto)) {
            return "C";
        }
        if ("Anulado".equalsIgnoreCase(estadoCompleto)) {
            return "A";
        }
        return "P";
    }

    private String estadoCompleto(String estadoAbreviado) {
        if ("C".equalsIgnoreCase(estadoAbreviado)) {
            return "Cobrado";
        }
        if ("A".equalsIgnoreCase(estadoAbreviado)) {
            return "Anulado";
        }
        return "Pendiente";
    }

    private void actualizarIndicadorStock(String tipoCodigo, int idItem) {
        try {
            boolean pedidoHabilitado = (idPedidoSeleccionado == null) || !esPedidoBloqueado();
            if (TIPO_SANDWICH.equals(tipoCodigo)) {
                List<DetalleSandwich> ingList = cacheRecetasSandwich.get(idItem);
                if (ingList == null || ingList.isEmpty()) {
                    lblStockDisponible.setText("● Sin receta configurada (0 disp.)");
                    lblStockDisponible.setForeground(new Color(239, 68, 68));
                    btnAgregarItem.setEnabled(false);
                    return;
                }
            } else if (TIPO_MENU.equals(tipoCodigo)) {
                List<DetalleMenu> compList = cacheComponentesMenu.get(idItem);
                if (compList == null || compList.isEmpty()) {
                    lblStockDisponible.setText("● Sin receta configurada (0 disp.)");
                    lblStockDisponible.setForeground(new Color(239, 68, 68));
                    btnAgregarItem.setEnabled(false);
                    return;
                }
            }
            BigDecimal stock = calcularStockDisponibleItem(tipoCodigo, idItem);
            if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                lblStockDisponible.setText("● Stock: AGOTADO (0 disp.)");
                lblStockDisponible.setForeground(new Color(239, 68, 68));
                btnAgregarItem.setEnabled(false);
            } else if (stock.compareTo(new BigDecimal(5)) <= 0) {
                lblStockDisponible.setText("● Stock bajo: " + stock.intValue() + " disp.");
                lblStockDisponible.setForeground(new Color(245, 158, 11));
                btnAgregarItem.setEnabled(pedidoHabilitado);
            } else {
                lblStockDisponible.setText("● Stock disponible: " + stock.intValue() + " unid.");
                lblStockDisponible.setForeground(TemaGestor.esModoOscuro() ? new Color(80, 250, 123) : new Color(5, 150, 105));
                btnAgregarItem.setEnabled(pedidoHabilitado);
            }
        } catch (Exception ex) {
            lblStockDisponible.setText("Stock: -");
            btnAgregarItem.setEnabled((idPedidoSeleccionado == null) || !esPedidoBloqueado());
        }
    }

    private BigDecimal calcularStockDisponibleItem(String tipoCodigo, int idItem) {
        if (TIPO_PRODUCTO.equals(tipoCodigo)) {
            Productos prod = cacheProductos.get(idItem);
            return (prod != null && prod.getExistenciaPro() != null) ? prod.getExistenciaPro() : BigDecimal.ZERO;
        } else if (TIPO_SANDWICH.equals(tipoCodigo)) {
            List<DetalleSandwich> ingList = cacheRecetasSandwich.get(idItem);
            if (ingList == null || ingList.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal minPosible = null;
            for (DetalleSandwich ing : ingList) {
                Productos prod = cacheProductos.get(ing.getIdProDet());
                BigDecimal exist = (prod != null && prod.getExistenciaPro() != null) ? prod.getExistenciaPro() : BigDecimal.ZERO;
                BigDecimal cantIng = (ing.getCantidadDet() != null && ing.getCantidadDet().compareTo(BigDecimal.ZERO) > 0)
                        ? ing.getCantidadDet() : BigDecimal.ONE;
                BigDecimal posible = exist.divide(cantIng, 0, RoundingMode.FLOOR);
                if (minPosible == null || posible.compareTo(minPosible) < 0) {
                    minPosible = posible;
                }
            }
            return minPosible != null ? minPosible : BigDecimal.ZERO;
        } else if (TIPO_MENU.equals(tipoCodigo)) {
            List<DetalleMenu> compList = cacheComponentesMenu.get(idItem);
            if (compList == null || compList.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal minPosible = null;
            for (DetalleMenu comp : compList) {
                BigDecimal cantComp = (comp.getCantidadDet() != null && comp.getCantidadDet().compareTo(BigDecimal.ZERO) > 0)
                        ? comp.getCantidadDet() : BigDecimal.ONE;
                BigDecimal posible = BigDecimal.ZERO;
                if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                    Productos prod = cacheProductos.get(comp.getIdItemDet());
                    BigDecimal exist = (prod != null && prod.getExistenciaPro() != null) ? prod.getExistenciaPro() : BigDecimal.ZERO;
                    posible = exist.divide(cantComp, 0, RoundingMode.FLOOR);
                } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                    BigDecimal stockSan = calcularStockDisponibleItem(TIPO_SANDWICH, comp.getIdItemDet());
                    posible = stockSan.divide(cantComp, 0, RoundingMode.FLOOR);
                }
                if (minPosible == null || posible.compareTo(minPosible) < 0) {
                    minPosible = posible;
                }
            }
            return minPosible != null ? minPosible : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    private void desglosarRequerimientosItem(Map<Integer, BigDecimal> acumulador, String tipo, int idItem, BigDecimal cantidad) {
        if (TIPO_PRODUCTO.equals(tipo)) {
            acumulador.put(idItem, acumulador.getOrDefault(idItem, BigDecimal.ZERO).add(cantidad));
        } else if (TIPO_SANDWICH.equals(tipo)) {
            List<DetalleSandwich> ingList = cacheRecetasSandwich.get(idItem);
            if (ingList != null) {
                for (DetalleSandwich ing : ingList) {
                    BigDecimal requerido = (ing.getCantidadDet() != null ? ing.getCantidadDet() : BigDecimal.ONE).multiply(cantidad);
                    acumulador.put(ing.getIdProDet(), acumulador.getOrDefault(ing.getIdProDet(), BigDecimal.ZERO).add(requerido));
                }
            }
        } else if (TIPO_MENU.equals(tipo)) {
            List<DetalleMenu> compList = cacheComponentesMenu.get(idItem);
            if (compList != null) {
                for (DetalleMenu comp : compList) {
                    BigDecimal cantComp = (comp.getCantidadDet() != null ? comp.getCantidadDet() : BigDecimal.ONE).multiply(cantidad);
                    if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                        acumulador.put(comp.getIdItemDet(), acumulador.getOrDefault(comp.getIdItemDet(), BigDecimal.ZERO).add(cantComp));
                    } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                        List<DetalleSandwich> ingSand = cacheRecetasSandwich.get(comp.getIdItemDet());
                        if (ingSand != null) {
                            for (DetalleSandwich ing : ingSand) {
                                BigDecimal reqIng = (ing.getCantidadDet() != null ? ing.getCantidadDet() : BigDecimal.ONE).multiply(cantComp);
                                acumulador.put(ing.getIdProDet(), acumulador.getOrDefault(ing.getIdProDet(), BigDecimal.ZERO).add(reqIng));
                            }
                        }
                    }
                }
            }
        }
    }

    private String verificarDisponibilidadStockMemoria(String nuevoTipo, int nuevoIdItem, BigDecimal nuevaCant) {
        Map<Integer, BigDecimal> requeridos = new HashMap<>();
        for (ItemDetalleBorrador it : itemsBorrador) {
            BigDecimal cant = it.cantidad != null ? it.cantidad : BigDecimal.ONE;
            desglosarRequerimientosItem(requeridos, it.tipoCodigo, it.idItem, cant);
        }
        if (nuevoTipo != null && nuevoIdItem > 0 && nuevaCant != null && nuevaCant.compareTo(BigDecimal.ZERO) > 0) {
            desglosarRequerimientosItem(requeridos, nuevoTipo, nuevoIdItem, nuevaCant);
        }

        for (Map.Entry<Integer, BigDecimal> entrada : requeridos.entrySet()) {
            int idPro = entrada.getKey();
            BigDecimal req = entrada.getValue();
            Productos prod = cacheProductos.get(idPro);
            if (prod != null) {
                BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
                if (exist.compareTo(req) < 0) {
                    return "Stock insuficiente de: " + prod.getNombrePro()
                            + "\nExistencia disponible: " + exist.stripTrailingZeros().toPlainString()
                            + "\nRequerido para el pedido: " + req.stripTrailingZeros().toPlainString();
                }
            }
        }
        return null;
    }

    private String verificarDisponibilidadStockPedidoBD(int idPedido, String nuevoTipo, int nuevoIdItem, BigDecimal nuevaCant) {
        Map<Integer, BigDecimal> requeridos = new HashMap<>();
        List<DetallesPedido> detallesExistentes = detallePedidoService.listarPorPedido(idPedido);
        if (detallesExistentes != null) {
            for (DetallesPedido d : detallesExistentes) {
                BigDecimal cant = d.getCantidadDet() != null ? d.getCantidadDet() : BigDecimal.ONE;
                desglosarRequerimientosItem(requeridos, d.getTipoItemDet(), d.getIdItemDet(), cant);
            }
        }
        if (nuevoTipo != null && nuevoIdItem > 0 && nuevaCant != null && nuevaCant.compareTo(BigDecimal.ZERO) > 0) {
            desglosarRequerimientosItem(requeridos, nuevoTipo, nuevoIdItem, nuevaCant);
        }

        for (Map.Entry<Integer, BigDecimal> entrada : requeridos.entrySet()) {
            int idPro = entrada.getKey();
            BigDecimal req = entrada.getValue();
            Productos prod = cacheProductos.get(idPro);
            if (prod != null) {
                BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
                if (exist.compareTo(req) < 0) {
                    return "Stock insuficiente de: " + prod.getNombrePro()
                            + "\nExistencia actual: " + exist.stripTrailingZeros().toPlainString()
                            + "\nRequerido para el pedido: " + req.stripTrailingZeros().toPlainString();
                }
            }
        }
        return null;
    }

    private void filtrarTablaPedidos() {
        if (clasificadorPedidos == null) {
            return;
        }
        String texto = txtBuscarPedido.getText().trim();
        if (texto.isEmpty()) {
            clasificadorPedidos.setRowFilter(null);
            return;
        }
        String textoMin = texto.toLowerCase();
        clasificadorPedidos.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
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

    @Override
    public void actualizarDatos() {
        Integer idActual = idPedidoSeleccionado;
        cargarCatalogosItems();
        actualizarComboItems();
        cargarTablaPedidos();
        if (idActual != null) {
            seleccionarEnTabla(idActual);
            cargarDetalle(idActual);
        }
        if (clienteActual != null) {
            Cliente fresco = clienteService.buscarClientePorId(clienteActual.getIdCli());
            asignarClienteActual(fresco);
        }
    }
}
