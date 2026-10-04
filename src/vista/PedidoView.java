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
import vista.util.FabricaDaisyUI;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
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
    private Integer idPedidoSeleccionado = null;

    private final JComboBox<String> cmbCliente = new JComboBox<>();
    private final JTextField txtFecha = new JTextField();
    private final JTextField txtTotal = new JTextField();
    private final JTextField txtPuntosObtenidos = new JTextField();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Pendiente", "Cobrado", "Anulado"});

    private final JComboBox<String> cmbTipoItem = new JComboBox<>(new String[]{"Sándwich", "Menú", "Producto"});
    private final JComboBox<String> cmbItem = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField();
    private final JTextField txtPrecioUnitario = new JTextField();

    private final JButton btnGuardar = new JButton("Guardar Pedido");
    private final JButton btnCobrar = new JButton("Cobrar / Pagar");
    private final JButton btnVerFactura = new JButton("Ver Factura");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("← Volver");

    private final JButton btnAgregarItem = new JButton("+ Agregar Item");
    private final JButton btnQuitarItem = new JButton("- Quitar Item");

    private final List<Integer> idsCliente = new ArrayList<>();
    private final List<Integer> idsSandwich = new ArrayList<>();
    private final List<BigDecimal> preciosSandwich = new ArrayList<>();
    private final List<Integer> idsMenu = new ArrayList<>();
    private final List<BigDecimal> preciosMenu = new ArrayList<>();
    private final List<Integer> idsProducto = new ArrayList<>();
    private final List<BigDecimal> preciosProducto = new ArrayList<>();

    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JTable tablaPedidos = new JTable() {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(TemaGestor.esModoOscuro() ? new Color(98, 114, 164) : Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Sin pedidos registrados";
                int x = (getWidth() - fm.stringWidth(mensaje)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(mensaje, x, y);
            }
        }
    };
    private final DefaultTableModel modeloPedidos = new DefaultTableModel() {
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
                g2.setColor(TemaGestor.esModoOscuro() ? new Color(98, 114, 164) : Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Seleccione un pedido para ver su detalle";
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
        cargarClientes();
        cargarCatalogosItems();
        actualizarComboItems();
        cargarTablaPedidos();
        seleccionarClienteInicial();
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

        JPanel panelCamposPedido = new JPanel(new GridLayout(3, 2, 12, 8));
        panelCamposPedido.setOpaque(false);

        txtFecha.setPreferredSize(new Dimension(160, 36));
        txtTotal.setText("0.00");
        txtTotal.setPreferredSize(new Dimension(140, 36));
        txtPuntosObtenidos.setText("0");
        txtPuntosObtenidos.setPreferredSize(new Dimension(140, 36));

        FabricaDaisyUI.aplicarCampoEstatico(txtFecha);
        FabricaDaisyUI.aplicarCampoEstatico(txtTotal);
        FabricaDaisyUI.aplicarCampoEstatico(txtPuntosObtenidos);

        cmbCliente.setPreferredSize(new Dimension(280, 36));
        cmbEstado.setPreferredSize(new Dimension(160, 36));

        FabricaDaisyUI.estilizarCampo(cmbCliente);
        FabricaDaisyUI.estilizarCampo(cmbEstado);

        panelCamposPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Cliente Registrado:", cmbCliente));
        panelCamposPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Estado del Pedido:", cmbEstado));
        panelCamposPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Fecha y Hora:", txtFecha));
        panelCamposPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Total a Pagar (Q):", txtTotal));
        panelCamposPedido.add(FabricaDaisyUI.crearCampoConEtiqueta("Puntos Obtenidos:", txtPuntosObtenidos));

        JPanel tarjetaPedido = FabricaDaisyUI.crearTarjetaSeccion(
                "Datos del Pedido",
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
            cargarClientes();
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

        btnGuardar.setPreferredSize(new Dimension(145, 38));
        btnCobrar.setPreferredSize(new Dimension(150, 38));
        btnVerFactura.setPreferredSize(new Dimension(145, 38));
        btnModificar.setPreferredSize(new Dimension(135, 38));
        btnEliminar.setPreferredSize(new Dimension(135, 38));
        btnLimpiar.setPreferredSize(new Dimension(130, 38));
        btnRegresar.setPreferredSize(new Dimension(130, 38));

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

    private void cargarClientes() {
        cmbCliente.removeAllItems();
        idsCliente.clear();
        try {
            List<Cliente> clientes = clienteService.listar();
            for (Cliente c : clientes) {
                cmbCliente.addItem(c.getNombreCli());
                idsCliente.add(c.getIdCli());
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los clientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarClienteInicial() {
        if (clienteInicial == null) {
            return;
        }
        for (int i = 0; i < idsCliente.size(); i++) {
            if (idsCliente.get(i) == clienteInicial.getIdCli()) {
                cmbCliente.setSelectedIndex(i);
                return;
            }
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
            actualizarEstadoBotones(false, false);
            return;
        }
        int idPed = (int) modeloPedidos.getValueAt(fila, 0);
        Pedidos p = pedidoService.buscarPorId(idPed);
        if (p == null) {
            actualizarEstadoBotones(false, false);
            return;
        }

        idPedidoSeleccionado = p.getIdPed();
        seleccionarClientePorId(p.getIdCliPed());
        txtFecha.setText(p.getFechaPed() != null ? p.getFechaPed().format(formateadorFecha) : "");
        txtTotal.setText(p.getTotalPed().toPlainString());
        txtPuntosObtenidos.setText(String.valueOf(p.getPuntosObtenidosPed()));
        cmbEstado.setSelectedItem(estadoCompleto(p.getEstadoPed()));

        cargarDetalle(idPed);
        boolean bloqueado = "C".equalsIgnoreCase(p.getEstadoPed()) || "A".equalsIgnoreCase(p.getEstadoPed());
        actualizarEstadoBotones(true, bloqueado);
    }

    private void seleccionarClientePorId(int idCli) {
        for (int i = 0; i < idsCliente.size(); i++) {
            if (idsCliente.get(i) == idCli) {
                cmbCliente.setSelectedIndex(i);
                return;
            }
        }
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
        if (cmbCliente.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Pedidos pedido = new Pedidos();
            pedido.setIdCliPed(idsCliente.get(cmbCliente.getSelectedIndex()));
            pedido.setFechaPed(LocalDateTime.now());
            pedido.setTotalPed(BigDecimal.ZERO);
            pedido.setPuntosObtenidosPed(0);
            pedido.setEstadoPed(estadoAbreviado((String) cmbEstado.getSelectedItem()));

            pedidoService.insertar(pedido);
            JOptionPane.showMessageDialog(this, "Pedido creado correctamente. Ahora puede agregarle items.");
            cargarTablaPedidos();
            seleccionarEnTabla(pedido.getIdPed());
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
        try {
            Pedidos pedido = pedidoService.buscarPorId(idPedidoSeleccionado);
            if (pedido == null) {
                JOptionPane.showMessageDialog(this, "El pedido no fue encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            pedido.setIdCliPed(idsCliente.get(cmbCliente.getSelectedIndex()));
            pedido.setEstadoPed(estadoAbreviado((String) cmbEstado.getSelectedItem()));

            pedidoService.actualizar(pedido);
            JOptionPane.showMessageDialog(this, "Pedido actualizado correctamente.");
            cargarTablaPedidos();
            seleccionarEnTabla(idPedidoSeleccionado);
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
                detallePedidoService.eliminarPorPedido(idPedidoSeleccionado);
                pedidoService.eliminar(idPedidoSeleccionado);
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
        int puntosGenerados = subtotal.divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).intValue();

        try {
            DetallesPedido detalle = new DetallesPedido();
            detalle.setIdPedDet(idPedidoSeleccionado);
            detalle.setTipoItemDet(tipoCodigo);
            detalle.setIdItemDet(idItem);
            detalle.setCantidadDet(cantidad);
            detalle.setPrecioUnitarioDet(precioUnitario);
            detalle.setSubTotalDet(subtotal);
            detalle.setPuntosGeneradosDet(puntosGenerados);

            detallePedidoService.insertar(detalle);
            recalcularTotalesPedido(idPedidoSeleccionado);
            cargarDetalle(idPedidoSeleccionado);
            cargarTablaPedidos();
            seleccionarEnTabla(idPedidoSeleccionado);
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
            detallePedidoService.eliminar(idDet);
            recalcularTotalesPedido(idPedidoSeleccionado);
            cargarDetalle(idPedidoSeleccionado);
            cargarTablaPedidos();
            seleccionarEnTabla(idPedidoSeleccionado);
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

    private void seleccionarEnTabla(int idPed) {
        for (int i = 0; i < modeloPedidos.getRowCount(); i++) {
            if ((int) modeloPedidos.getValueAt(i, 0) == idPed) {
                tablaPedidos.setRowSelectionInterval(i, i);
                tablaPedidos.scrollRectToVisible(tablaPedidos.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void limpiarFormulario() {
        idPedidoSeleccionado = null;
        txtFecha.setText("");
        txtTotal.setText("0.00");
        txtPuntosObtenidos.setText("0");
        cmbEstado.setSelectedItem("Pendiente");
        txtCantidad.setText("1");
        tablaPedidos.clearSelection();
        modeloDetalle.setRowCount(0);
        if (clienteInicial != null) {
            seleccionarClienteInicial();
        }
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
