package vista;

import model.Cliente;
import model.DetallesPedido;
import model.Menus;
import model.Pedidos;
import model.Productos;
import model.Sandwich;
import service.ClienteService;
import service.DetallePedidoService;
import service.MenuService;
import service.PedidoService;
import service.ProductoService;
import service.SandwichService;
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
import java.util.List;

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

        JPanel panelDetalleAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelDetalleAcciones.setOpaque(false);

        btnAgregarItem.setPreferredSize(new Dimension(145, 34));
        btnQuitarItem.setPreferredSize(new Dimension(145, 34));
        btnAgregarItem.setIconTextGap(8);
        btnQuitarItem.setIconTextGap(8);

        FabricaDaisyUI.aplicarBotonPrimario(btnAgregarItem);
        FabricaDaisyUI.aplicarBotonPeligro(btnQuitarItem);

        btnAgregarItem.addActionListener(e -> agregarDetalle());
        btnQuitarItem.addActionListener(e -> quitarDetalle());

        panelDetalleAcciones.add(btnAgregarItem);
        panelDetalleAcciones.add(btnQuitarItem);

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
        scrollPedidos.setPreferredSize(new Dimension(540, 320));
        scrollPedidos.setBorder(BorderFactory.createEmptyBorder());

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

        JPanel tarjetaPedidos = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Historial de Pedidos",
                btnRefrescar,
                scrollPedidos
        );

        modeloDetalle.setColumnIdentifiers(new String[]{"ID", "Tipo", "Item", "Cantidad", "Precio Unitario", "Subtotal", "Puntos"});
        tablaDetalle.setModel(modeloDetalle);
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
        scrollDetalle.setPreferredSize(new Dimension(540, 320));
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
        btnGuardar.setEnabled(!seleccionActiva);
        btnModificar.setEnabled(seleccionActiva && !bloqueado);
        btnEliminar.setEnabled(seleccionActiva);
        btnCobrar.setEnabled(seleccionActiva && !bloqueado && tieneTotalPositivo());
        btnVerFactura.setEnabled(seleccionActiva);
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
            JOptionPane.showMessageDialog(this, "Por favor ingrese el número de DPI del cliente a buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente c = clienteService.buscarPorDpi(soloDigitos);
        if (c == null) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "No se encontró ningún cliente con DPI: " + FormatoTexto.formatearDpi(soloDigitos) + "\n\n¿Desea registrar al cliente ahora?",
                    "Cliente No Encontrado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (opcion == JOptionPane.YES_OPTION) {
                new ClienteView(this, soloDigitos).setVisible(true);
            }
            return;
        }
        if (!"A".equalsIgnoreCase(c.getEstadoCli())) {
            JOptionPane.showMessageDialog(
                    this,
                    "El cliente " + c.getNombreCli() + " se encuentra INACTIVO.\nNo es posible registrar pedidos a clientes inactivos.",
                    "Cliente Inactivo",
                    JOptionPane.ERROR_MESSAGE
            );
            asignarClienteActual(null);
            return;
        }
        asignarClienteActual(c);
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
            return;
        }
        int tipoIndex = cmbTipoItem.getSelectedIndex();
        BigDecimal precio = BigDecimal.ZERO;
        if (tipoIndex == 0 && itemIndex < preciosSandwich.size()) {
            precio = preciosSandwich.get(itemIndex);
        } else if (tipoIndex == 1 && itemIndex < preciosMenu.size()) {
            precio = preciosMenu.get(itemIndex);
        } else if (tipoIndex == 2 && itemIndex < preciosProducto.size()) {
            precio = preciosProducto.get(itemIndex);
        }
        txtPrecioUnitario.setText(precio.toPlainString());
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
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de pedidos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "No se pudo cargar el detalle del pedido: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Debe buscar y seleccionar un cliente activo antes de guardar el pedido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!"A".equalsIgnoreCase(clienteActual.getEstadoCli())) {
            JOptionPane.showMessageDialog(this, "El cliente seleccionado se encuentra inactivo. No se pueden registrar pedidos.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Pedido creado correctamente. Ahora puede agregarle items.");
            cargarTablaPedidos();
            seleccionarEnTabla(idGenerado);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarPedido() {
        if (idPedidoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (esPedidoBloqueado()) {
            JOptionPane.showMessageDialog(this, "No se puede modificar un pedido que ya está pagado o anulado.", "Operación Bloqueada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (clienteActual == null) {
            JOptionPane.showMessageDialog(this, "Debe buscar y seleccionar un cliente para el pedido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int idPed = idPedidoSeleccionado;
            Pedidos pedido = pedidoService.buscarPorId(idPed);
            if (pedido == null) {
                JOptionPane.showMessageDialog(this, "El pedido no fue encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            pedido.setIdCliPed(clienteActual.getIdCli());
            pedido.setEstadoPed(estadoAbreviado((String) cmbEstado.getSelectedItem()));

            pedidoService.actualizar(pedido);
            JOptionPane.showMessageDialog(this, "Pedido actualizado correctamente.");
            cargarTablaPedidos();
            seleccionarEnTabla(idPed);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPedido() {
        if (idPedidoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (esPedidoBloqueado()) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar un pedido que ya ha sido pagado o anulado.", "Operación Bloqueada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el pedido seleccionado y su detalle?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                int idPed = idPedidoSeleccionado;
                detallePedidoService.eliminarPorPedido(idPed);
                pedidoService.eliminar(idPed);
                JOptionPane.showMessageDialog(this, "Pedido eliminado correctamente.");
                cargarTablaPedidos();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarDetalle() {
        if (idPedidoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero debe crear o seleccionar un pedido para agregar items.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (esPedidoBloqueado()) {
            JOptionPane.showMessageDialog(this, "No se pueden agregar items a un pedido pagado o anulado.", "Operación Bloqueada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cmbItem.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un item para agregar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal cantidad;
        try {
            cantidad = new BigDecimal(txtCantidad.getText().trim());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
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
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarDetalle() {
        if (esPedidoBloqueado()) {
            JOptionPane.showMessageDialog(this, "No se pueden quitar items de un pedido pagado o anulado.", "Operación Bloqueada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila del detalle para quitar.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al quitar item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla para procesar su cobro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Pedidos p = pedidoService.buscarPorId(idPedidoSeleccionado);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "El pedido no fue encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("C".equalsIgnoreCase(p.getEstadoPed())) {
            int resp = JOptionPane.showConfirmDialog(this, "El pedido ya se encuentra pagado / completado.\n¿Desea ver su factura?", "Pedido Pagado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (resp == JOptionPane.YES_OPTION) {
                new FacturaView(this, idPedidoSeleccionado).setVisible(true);
            }
            return;
        }
        if (p.getTotalPed().compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "No se puede cobrar un pedido con total Q0.00. Agregue items al pedido primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PagoView ventana = new PagoView(this, p);
        ventana.setVisible(true);
    }

    private void verFactura() {
        if (idPedidoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla para ver su factura.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
}
