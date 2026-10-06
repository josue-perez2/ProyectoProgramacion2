package vista;

import model.Cliente;
import model.DetalleMenu;
import model.DetallesPedido;
import model.DetalleSandwich;
import model.Menus;
import model.Pedidos;
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
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedidoView extends JFrame {

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
    private final JComboBox<String> cmbItem = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField();
    private final JTextField txtPrecioUnitario = new JTextField();
    private final JLabel lblStockDisponible = new JLabel("Stock: -");

    private final JButton btnGuardar = FabricaDaisyUI.crearBotonPrimario("Guardar Pedido", Icons.save(16), null);
    private final JButton btnCobrar = FabricaDaisyUI.crearBotonSecundario("Cobrar / Pagar", Icons.creditCard(16), null);
    private final JButton btnVerFactura = FabricaDaisyUI.crearBotonAcento("Ver Factura", Icons.receipt(16), null);
    private final JButton btnModificar = FabricaDaisyUI.crearBotonNeutral("Modificar", Icons.edit(16), null);
    private final JButton btnEliminar = FabricaDaisyUI.crearBotonPeligro("Eliminar", Icons.trash(16), null);
    private final JButton btnLimpiar = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), null);
    private final JButton btnRegresar = FabricaDaisyUI.crearBotonNeutral("Volver", Icons.arrowLeft(16), null);

    private final JButton btnAgregarItem = FabricaDaisyUI.crearBotonPrimario("Agregar", Icons.plus(14), null);
    private final JButton btnQuitarItem = FabricaDaisyUI.crearBotonPeligro("Quitar", Icons.trash(14), null);

    private final List<Integer> idsSandwich = new ArrayList<>();
    private final List<BigDecimal> preciosSandwich = new ArrayList<>();
    private final List<Integer> idsMenu = new ArrayList<>();
    private final List<BigDecimal> preciosMenu = new ArrayList<>();
    private final List<Integer> idsProducto = new ArrayList<>();
    private final List<BigDecimal> preciosProducto = new ArrayList<>();

    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        txtDpiCliente.setPreferredSize(new Dimension(170, 36));
        txtDpiCliente.addActionListener(e -> buscarClientePorDpi());

        btnBuscarCliente.setPreferredSize(new Dimension(88, 36));
        btnNuevoCliente.setPreferredSize(new Dimension(98, 36));

        JPanel panelDpiFila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        panelDpiFila.setOpaque(false);
        JLabel lblDpiTag = new JLabel("DPI:");
        lblDpiTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelDpiFila.add(lblDpiTag);
        txtDpiCliente.setPreferredSize(new Dimension(160, 36));
        btnBuscarCliente.setPreferredSize(new Dimension(110, 36));
        btnNuevoCliente.setPreferredSize(new Dimension(125, 36));
        panelDpiFila.add(txtDpiCliente);
        panelDpiFila.add(btnBuscarCliente);
        panelDpiFila.add(btnNuevoCliente);

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

        JPanel panelDetalleCampos = new JPanel(new GridLayout(2, 2, 12, 8));
        panelDetalleCampos.setOpaque(false);

        cmbTipoItem.setPreferredSize(new Dimension(150, 36));
        cmbTipoItem.addActionListener(e -> actualizarComboItems());

        cmbItem.setPreferredSize(new Dimension(240, 36));
        cmbItem.addActionListener(e -> actualizarPrecioUnitario());

        txtPrecioUnitario.setText("0.00");
        txtPrecioUnitario.setPreferredSize(new Dimension(110, 36));
        FabricaDaisyUI.aplicarCampoEstatico(txtPrecioUnitario);

        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(85, 36));

        FabricaDaisyUI.estilizarCampo(cmbTipoItem);
        FabricaDaisyUI.estilizarCampo(cmbItem);
        FabricaDaisyUI.estilizarCampo(txtCantidad);

        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Tipo de Item:", cmbTipoItem));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Item Seleccionado:", cmbItem));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Precio Unitario (Q):", txtPrecioUnitario));
        panelDetalleCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Cantidad:", txtCantidad));

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
        tablaPedidos.setModel(modeloPedidos);
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
                    verFactura();
                }
            }
        });
        tablaPedidos.getColumnModel().getColumn(5).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());
        JScrollPane scrollPedidos = new JScrollPane(tablaPedidos);
        scrollPedidos.setBorder(BorderFactory.createEmptyBorder());

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
            cargarTablaPedidos();
        });

        JPanel panelAccionesPedidos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelAccionesPedidos.setOpaque(false);
        panelAccionesPedidos.add(btnExportarPedidos);
        panelAccionesPedidos.add(btnRefrescar);

        JPanel tarjetaPedidos = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Historial de Pedidos",
                panelAccionesPedidos,
                scrollPedidos
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
        btnLimpiar.setPreferredSize(new Dimension(130, 38));
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
        btnGuardar.setEnabled(!seleccionActiva && clienteValido);
        btnModificar.setEnabled(seleccionActiva && !bloqueado);
        btnEliminar.setEnabled(seleccionActiva);
        btnCobrar.setEnabled(seleccionActiva && !bloqueado && tieneTotalPositivo());
        boolean esCobrado = false;
        if (seleccionActiva && idPedidoSeleccionado != null) {
            Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
            esCobrado = p != null && "C".equalsIgnoreCase(p.getEstadoPed());
        }
        btnVerFactura.setEnabled(esCobrado);
        btnAgregarItem.setEnabled(seleccionActiva && !bloqueado);
        btnQuitarItem.setEnabled(false);
    }

    private boolean tieneTotalPositivo() {
        try {
            BigDecimal total = new BigDecimal(txtTotal.getText().trim());
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
                new ClienteView(this, soloDigitos).setVisible(true);
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
        FabricaDaisyUI.mostrarToastExito(this, "Cliente seleccionado: " + c.getNombreCli());
    }

    private void registrarNuevoCliente() {
        String textoDpi = FormatoTexto.soloDigitos(txtDpiCliente.getText().trim());
        new ClienteView(this, textoDpi).setVisible(true);
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
            btnGuardar.setEnabled(c != null && "A".equalsIgnoreCase(c.getEstadoCli()));
        }
    }

    private void cargarCatalogosItems() {
        idsSandwich.clear();
        preciosSandwich.clear();
        try {
            for (Sandwich s : sandwichService.listarActivos()) {
                idsSandwich.add(s.getIdSan());
                preciosSandwich.add(s.getPrecioSan());
            }
        } catch (RuntimeException ignored) {
        }

        idsMenu.clear();
        preciosMenu.clear();
        try {
            for (Menus m : menuService.listarActivos()) {
                idsMenu.add(m.getIdMen());
                preciosMenu.add(m.getPrecioMen());
            }
        } catch (RuntimeException ignored) {
        }

        idsProducto.clear();
        preciosProducto.clear();
        try {
            for (Productos p : productoService.listar()) {
                if ("A".equalsIgnoreCase(p.getActivoPro())) {
                    idsProducto.add(p.getIdPro());
                    preciosProducto.add(p.getPrecioPro());
                }
            }
        } catch (RuntimeException ignored) {
        }
    }

    private void actualizarComboItems() {
        cmbItem.removeAllItems();
        int tipoIndex = cmbTipoItem.getSelectedIndex();
        if (tipoIndex == 0) {
            try {
                for (Sandwich s : sandwichService.listarActivos()) {
                    cmbItem.addItem(s.getNombreSan());
                }
            } catch (RuntimeException ignored) {
            }
        } else if (tipoIndex == 1) {
            try {
                for (Menus m : menuService.listarActivos()) {
                    cmbItem.addItem(m.getNombreMen());
                }
            } catch (RuntimeException ignored) {
            }
        } else {
            try {
                for (Productos p : productoService.listar()) {
                    if ("A".equalsIgnoreCase(p.getActivoPro())) {
                        cmbItem.addItem(p.getNombrePro());
                    }
                }
            } catch (RuntimeException ignored) {
            }
        }
        actualizarPrecioUnitario();
    }

    private void actualizarPrecioUnitario() {
        int itemIndex = cmbItem.getSelectedIndex();
        if (itemIndex == -1) {
            txtPrecioUnitario.setText("0.00");
            lblStockDisponible.setText("Stock: -");
            return;
        }
        int tipoIndex = cmbTipoItem.getSelectedIndex();
        BigDecimal precio = BigDecimal.ZERO;
        String tipoCodigo = tipoIndex == 0 ? TIPO_SANDWICH : (tipoIndex == 1 ? TIPO_MENU : TIPO_PRODUCTO);
        int idItem = -1;
        if (tipoIndex == 0 && itemIndex < preciosSandwich.size()) {
            precio = preciosSandwich.get(itemIndex);
            idItem = idsSandwich.get(itemIndex);
        } else if (tipoIndex == 1 && itemIndex < preciosMenu.size()) {
            precio = preciosMenu.get(itemIndex);
            idItem = idsMenu.get(itemIndex);
        } else if (tipoIndex == 2 && itemIndex < preciosProducto.size()) {
            precio = preciosProducto.get(itemIndex);
            idItem = idsProducto.get(itemIndex);
        }
        txtPrecioUnitario.setText(precio.toPlainString());

        if (idItem != -1) {
            actualizarIndicadorStock(tipoCodigo, idItem);
        } else {
            lblStockDisponible.setText("Stock: -");
        }
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
        Cliente c = clienteService.buscarClientePorId(idCli);
        return c != null ? c.getNombreCli() : "Cliente #" + idCli;
    }

    private void seleccionarFilaPedido() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila == -1) {
            idPedidoSeleccionado = null;
            limpiarCamposSinDeseleccionar();
            actualizarEstadoBotones(false, false);
            return;
        }
        int filaModelo = tablaPedidos.convertRowIndexToModel(fila);
        int idPed = (int) modeloPedidos.getValueAt(filaModelo, 0);
        Pedidos p = pedidoService.buscarPorId(idPed);
        if (p == null) {
            idPedidoSeleccionado = null;
            limpiarCamposSinDeseleccionar();
            actualizarEstadoBotones(false, false);
            return;
        }

        idPedidoSeleccionado = p.getIdPed();
        Cliente c = clienteService.buscarClientePorId(p.getIdCliPed());
        asignarClienteActual(c);
        txtFecha.setText(p.getFechaPed() != null ? p.getFechaPed().format(formateadorFecha) : "");
        txtTotal.setText(p.getTotalPed().toPlainString());
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
            for (Sandwich s : sandwichService.listar()) {
                if (s.getIdSan() == idItem) {
                    return s.getNombreSan();
                }
            }
            return "Sándwich #" + idItem;
        }
        if (TIPO_MENU.equals(tipo)) {
            for (Menus m : menuService.listar()) {
                if (m.getIdMen() == idItem) {
                    return m.getNombreMen();
                }
            }
            return "Menú #" + idItem;
        }
        for (Productos p : productoService.listar()) {
            if (p.getIdPro() == idItem) {
                return p.getNombrePro();
            }
        }
        return "Producto #" + idItem;
    }

    private void guardarPedido() {
        if (clienteActual == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Debe buscar y seleccionar un cliente activo antes de guardar el pedido.");
            return;
        }
        if (!"A".equalsIgnoreCase(clienteActual.getEstadoCli())) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "El cliente seleccionado se encuentra inactivo. No se pueden registrar pedidos.");
            return;
        }
        try {
            Pedidos pedido = new Pedidos();
            pedido.setIdCliPed(clienteActual.getIdCli());
            pedido.setFechaPed(LocalDateTime.now());
            pedido.setTotalPed(BigDecimal.ZERO);
            pedido.setPuntosObtenidosPed(0);
            pedido.setEstadoPed(estadoAbreviado((String) cmbEstado.getSelectedItem()));

            pedidoService.insertar(pedido);
            int idGenerado = pedido.getIdPed();
            if (idGenerado <= 0 && clienteActual != null) {
                List<Pedidos> lista = pedidoService.listarPorCliente(clienteActual.getIdCli());
                if (!lista.isEmpty()) {
                    idGenerado = lista.get(0).getIdPed();
                    pedido.setIdPed(idGenerado);
                }
            }
            FabricaDaisyUI.mostrarExito(this, "Pedido Creado", "Pedido creado correctamente. Ahora puede agregarle items.");
            cargarTablaPedidos();
            seleccionarEnTabla(idGenerado);
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al guardar pedido: " + ex.getMessage());
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
            pedido.setIdCliPed(clienteActual.getIdCli());
            pedido.setEstadoPed(estadoAbreviado((String) cmbEstado.getSelectedItem()));

            pedidoService.actualizar(pedido);
            FabricaDaisyUI.mostrarToastExito(this, "Pedido actualizado correctamente.");
            cargarTablaPedidos();
            seleccionarEnTabla(idPed);
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
            } catch (RuntimeException ex) {
                FabricaDaisyUI.mostrarError(this, "Error", "Error al eliminar pedido: " + ex.getMessage());
            }
        }
    }

    private void agregarDetalle() {
        if (idPedidoSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Primero debe crear o seleccionar un pedido para agregar items.");
            return;
        }
        if (esPedidoBloqueado()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Operación Bloqueada", "No se pueden agregar items a un pedido pagado o anulado.");
            return;
        }
        if (cmbItem.getSelectedIndex() == -1) {
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
        int itemIndex = cmbItem.getSelectedIndex();

        String tipoCodigo = tipoIndex == 0 ? TIPO_SANDWICH : (tipoIndex == 1 ? TIPO_MENU : TIPO_PRODUCTO);
        int idItem = tipoIndex == 0 ? idsSandwich.get(itemIndex) : (tipoIndex == 1 ? idsMenu.get(itemIndex) : idsProducto.get(itemIndex));
        BigDecimal precioUnitario = new BigDecimal(txtPrecioUnitario.getText().trim());
        BigDecimal subtotal = precioUnitario.multiply(cantidad);
        int puntosGenerados = 0;
        if (TIPO_SANDWICH.equals(tipoCodigo)) {
            puntosGenerados = cantidad.intValue() * 2;
        } else if (TIPO_MENU.equals(tipoCodigo)) {
            puntosGenerados = cantidad.intValue() * 8;
        } else {
            puntosGenerados = 0;
        }

        String errorStock = verificarDisponibilidadStock(idPedidoSeleccionado, tipoCodigo, idItem, cantidad);
        if (errorStock != null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente", "No se puede agregar el item al pedido:\n\n" + errorStock);
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
            FabricaDaisyUI.mostrarToastExito(this, "Item agregado al pedido.");
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al agregar item: " + ex.getMessage());
        }
    }

    private void quitarDetalle() {
        if (esPedidoBloqueado()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Operación Bloqueada", "No se pueden quitar items de un pedido pagado o anulado.");
            return;
        }
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione una fila del detalle para quitar.");
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
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un pedido de la tabla para procesar su cobro.");
            return;
        }
        Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
        if (p == null) {
            FabricaDaisyUI.mostrarError(this, "Error", "El pedido no fue encontrado.");
            return;
        }
        if ("C".equalsIgnoreCase(p.getEstadoPed())) {
            boolean ver = FabricaDaisyUI.mostrarConfirmacion(this, "Pedido Ya Pagado", "El pedido ya se encuentra pagado / completado.\n¿Desea ver su factura?");
            if (ver) {
                new FacturaView(this, idPedidoSeleccionado).setVisible(true);
            }
            return;
        }
        if (p.getTotalPed().compareTo(BigDecimal.ZERO) <= 0) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "No se puede cobrar un pedido con total Q0.00. Agregue items al pedido primero.");
            return;
        }

        String errorStock = verificarDisponibilidadStock(idPedidoSeleccionado, null, 0, null);
        if (errorStock != null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Stock Insuficiente para Cobro", "No se puede proceder al cobro del pedido:\n\n" + errorStock);
            return;
        }

        PagoView ventana = new PagoView(this, p);
        ventana.setVisible(true);
    }

    private void verFactura() {
        if (idPedidoSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un pedido cobrado de la tabla para ver su factura.");
            return;
        }
        Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
        if (p == null || !"C".equalsIgnoreCase(p.getEstadoPed())) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "El pedido seleccionado no ha sido cobrado. Solo los pedidos pagados cuentan con factura.");
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
            BigDecimal stock = calcularStockDisponibleItem(tipoCodigo, idItem);
            if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                lblStockDisponible.setText("● Stock: AGOTADO (0 dispon.)");
                lblStockDisponible.setForeground(new Color(239, 68, 68));
            } else if (stock.compareTo(new BigDecimal(5)) <= 0) {
                lblStockDisponible.setText("● Stock bajo: " + stock.intValue() + " dispon.");
                lblStockDisponible.setForeground(new Color(245, 158, 11));
            } else {
                lblStockDisponible.setText("● Stock disponible: " + stock.intValue() + " unid.");
                lblStockDisponible.setForeground(TemaGestor.esModoOscuro() ? new Color(80, 250, 123) : new Color(5, 150, 105));
            }
        } catch (Exception ex) {
            lblStockDisponible.setText("Stock: -");
        }
    }

    private BigDecimal calcularStockDisponibleItem(String tipoCodigo, int idItem) {
        if (TIPO_PRODUCTO.equals(tipoCodigo)) {
            Productos prod = productoService.buscarPorId(idItem);
            return (prod != null && prod.getExistenciaPro() != null) ? prod.getExistenciaPro() : BigDecimal.ZERO;
        } else if (TIPO_SANDWICH.equals(tipoCodigo)) {
            List<DetalleSandwich> ingList = detalleSandwichService.listarPorSandwich(idItem);
            if (ingList == null || ingList.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal minPosible = null;
            for (DetalleSandwich ing : ingList) {
                Productos prod = productoService.buscarPorId(ing.getIdProDet());
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
            List<DetalleMenu> compList = detalleMenuService.listarPorMenu(idItem);
            if (compList == null || compList.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal minPosible = null;
            for (DetalleMenu comp : compList) {
                BigDecimal cantComp = (comp.getCantidadDet() != null && comp.getCantidadDet().compareTo(BigDecimal.ZERO) > 0)
                        ? comp.getCantidadDet() : BigDecimal.ONE;
                BigDecimal posible = BigDecimal.ZERO;
                if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                    Productos prod = productoService.buscarPorId(comp.getIdItemDet());
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
            List<DetalleSandwich> ingList = detalleSandwichService.listarPorSandwich(idItem);
            if (ingList != null) {
                for (DetalleSandwich ing : ingList) {
                    BigDecimal requerido = (ing.getCantidadDet() != null ? ing.getCantidadDet() : BigDecimal.ONE).multiply(cantidad);
                    acumulador.put(ing.getIdProDet(), acumulador.getOrDefault(ing.getIdProDet(), BigDecimal.ZERO).add(requerido));
                }
            }
        } else if (TIPO_MENU.equals(tipo)) {
            List<DetalleMenu> compList = detalleMenuService.listarPorMenu(idItem);
            if (compList != null) {
                for (DetalleMenu comp : compList) {
                    BigDecimal cantComp = (comp.getCantidadDet() != null ? comp.getCantidadDet() : BigDecimal.ONE).multiply(cantidad);
                    if ("P".equalsIgnoreCase(comp.getTipoItemDet())) {
                        acumulador.put(comp.getIdItemDet(), acumulador.getOrDefault(comp.getIdItemDet(), BigDecimal.ZERO).add(cantComp));
                    } else if ("S".equalsIgnoreCase(comp.getTipoItemDet())) {
                        List<DetalleSandwich> ingSand = detalleSandwichService.listarPorSandwich(comp.getIdItemDet());
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

    private String verificarDisponibilidadStock(int idPedido, String nuevoTipo, int nuevoIdItem, BigDecimal nuevaCant) {
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
            Productos prod = productoService.buscarPorId(idPro);
            if (prod != null) {
                BigDecimal exist = prod.getExistenciaPro() != null ? prod.getExistenciaPro() : BigDecimal.ZERO;
                if (exist.compareTo(req) < 0) {
                    return "Stock insuficiente de: " + prod.getNombrePro()
                            + "\nExistencia actual: " + exist
                            + "\nRequerido para el pedido: " + req;
                }
            }
        }
        return null;
    }
}
