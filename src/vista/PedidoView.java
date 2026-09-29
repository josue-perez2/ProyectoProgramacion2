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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    private final JTextField txtId = new JTextField();
    private final JComboBox<String> cmbCliente = new JComboBox<>();
    private final JTextField txtFecha = new JTextField();
    private final JTextField txtTotal = new JTextField();
    private final JTextField txtPuntosObtenidos = new JTextField();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"P", "C", "A"});

    private final JComboBox<String> cmbTipoItem = new JComboBox<>(new String[]{"Sandwich", "Menu", "Producto"});
    private final JComboBox<String> cmbItem = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField();
    private final JTextField txtPrecioUnitario = new JTextField();

    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCobrar = new JButton("Cobrar / Pagar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("Regresar");

    private final JButton btnAgregarItem = new JButton("Agregar Detalle");
    private final JButton btnQuitarItem = new JButton("Quitar Detalle");

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
                g2.setColor(Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Sin datos registrados";
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
                g2.setColor(Color.GRAY);
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
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 6, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Pedido"));

        txtId.setEditable(false);
        txtId.setPreferredSize(new Dimension(100, 25));
        txtFecha.setEditable(false);
        txtFecha.setPreferredSize(new Dimension(150, 25));
        txtTotal.setEditable(false);
        txtTotal.setText("0.00");
        txtTotal.setPreferredSize(new Dimension(100, 25));
        txtPuntosObtenidos.setEditable(false);
        txtPuntosObtenidos.setText("0");
        txtPuntosObtenidos.setPreferredSize(new Dimension(80, 25));

        panelFormulario.add(crearCampo("ID Pedido:", txtId));
        panelFormulario.add(crearCampo("Cliente:", cmbCliente));
        panelFormulario.add(crearCampo("Fecha:", txtFecha));
        panelFormulario.add(crearCampo("Total:", txtTotal));
        panelFormulario.add(crearCampo("Puntos Obtenidos:", txtPuntosObtenidos));
        panelFormulario.add(crearCampo("Estado:", cmbEstado));

        JPanel panelSuperior = new JPanel(new BorderLayout(5, 5));
        panelSuperior.add(panelFormulario, BorderLayout.CENTER);

        JPanel panelDetalleForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelDetalleForm.setBorder(BorderFactory.createTitledBorder("Agregar Items al Pedido (Sandwich, Menu o Producto)"));

        cmbTipoItem.setPreferredSize(new Dimension(120, 25));
        cmbTipoItem.addActionListener(e -> actualizarComboItems());

        cmbItem.setPreferredSize(new Dimension(240, 25));
        cmbItem.addActionListener(e -> actualizarPrecioUnitario());

        txtCantidad.setText("1");
        txtCantidad.setPreferredSize(new Dimension(60, 25));

        txtPrecioUnitario.setEditable(false);
        txtPrecioUnitario.setText("0.00");
        txtPrecioUnitario.setPreferredSize(new Dimension(80, 25));

        btnAgregarItem.setPreferredSize(new Dimension(140, 30));
        btnQuitarItem.setPreferredSize(new Dimension(140, 30));
        btnAgregarItem.addActionListener(e -> agregarDetalle());
        btnQuitarItem.addActionListener(e -> quitarDetalle());

        panelDetalleForm.add(new JLabel("Tipo:"));
        panelDetalleForm.add(cmbTipoItem);
        panelDetalleForm.add(new JLabel("Item:"));
        panelDetalleForm.add(cmbItem);
        panelDetalleForm.add(new JLabel("Precio:"));
        panelDetalleForm.add(txtPrecioUnitario);
        panelDetalleForm.add(new JLabel("Cantidad:"));
        panelDetalleForm.add(txtCantidad);
        panelDetalleForm.add(btnAgregarItem);
        panelDetalleForm.add(btnQuitarItem);

        panelSuperior.add(panelDetalleForm, BorderLayout.SOUTH);

        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(2, 1, 10, 10));
        panelTablas.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel panelTablaPedidos = new JPanel(new BorderLayout());
        panelTablaPedidos.setBorder(BorderFactory.createTitledBorder("Listado de Pedidos"));

        modeloPedidos.setColumnIdentifiers(new String[]{"ID", "Cliente", "Fecha", "Total", "Puntos", "Estado"});
        tablaPedidos.setModel(modeloPedidos);
        tablaPedidos.setFillsViewportHeight(true);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFilaPedido();
            }
        });
        panelTablaPedidos.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);

        JPanel panelTablaDetalle = new JPanel(new BorderLayout());
        panelTablaDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del Pedido seleccionado"));

        modeloDetalle.setColumnIdentifiers(new String[]{"ID", "ID Pedido", "Tipo", "Item", "Cantidad", "Precio Unitario", "Subtotal", "Puntos"});
        tablaDetalle.setModel(modeloDetalle);
        tablaDetalle.setFillsViewportHeight(true);
        tablaDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelTablaDetalle.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);

        panelTablas.add(panelTablaPedidos);
        panelTablas.add(panelTablaDetalle);

        add(panelTablas, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnGuardar.setPreferredSize(new Dimension(110, 30));
        btnCobrar.setPreferredSize(new Dimension(130, 30));
        btnModificar.setPreferredSize(new Dimension(110, 30));
        btnEliminar.setPreferredSize(new Dimension(110, 30));
        btnLimpiar.setPreferredSize(new Dimension(110, 30));
        btnRegresar.setPreferredSize(new Dimension(110, 30));

        btnGuardar.addActionListener(e -> guardarPedido());
        btnCobrar.addActionListener(e -> abrirCobro());
        btnModificar.addActionListener(e -> actualizarPedido());
        btnEliminar.addActionListener(e -> eliminarPedido());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCobrar);
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

    private void cargarClientes() {
        cmbCliente.removeAllItems();
        idsCliente.clear();
        try {
            List<Cliente> clientes = clienteService.listar();
            for (Cliente c : clientes) {
                cmbCliente.addItem(c.getNombreCli() + " (DPI: " + c.getDpiCli() + ")");
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
                    cmbItem.addItem(s.getNombreSan() + " (" + s.getCodigoSan() + ")");
                }
            } catch (RuntimeException ignored) {
            }
        } else if (tipoIndex == 1) {
            try {
                for (Menus m : menuService.listarActivos()) {
                    cmbItem.addItem(m.getNombreMen() + " (" + m.getCodigoMen() + ")");
                }
            } catch (RuntimeException ignored) {
            }
        } else {
            try {
                for (Productos p : productoService.listar()) {
                    if ("A".equalsIgnoreCase(p.getActivoPro())) {
                        cmbItem.addItem(p.getNombrePro() + " (" + p.getCodigoPro() + ")");
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
                modeloPedidos.addRow(new Object[]{
                        p.getIdPed(),
                        nombreCliente,
                        fechaTexto,
                        p.getTotalPed(),
                        p.getPuntosObtenidosPed(),
                        p.getEstadoPed()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de pedidos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerNombreCliente(int idCli) {
        for (int i = 0; i < idsCliente.size(); i++) {
            if (idsCliente.get(i) == idCli) {
                return cmbCliente.getItemAt(i);
            }
        }
        return "Cliente " + idCli;
    }

    private void seleccionarFilaPedido() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila == -1) {
            return;
        }
        int idPed = (int) modeloPedidos.getValueAt(fila, 0);
        Pedidos p = pedidoService.buscarPorId(idPed);
        if (p == null) {
            return;
        }

        txtId.setText(String.valueOf(p.getIdPed()));
        seleccionarClientePorId(p.getIdCliPed());
        txtFecha.setText(p.getFechaPed() != null ? p.getFechaPed().format(formateadorFecha) : "");
        txtTotal.setText(p.getTotalPed().toPlainString());
        txtPuntosObtenidos.setText(String.valueOf(p.getPuntosObtenidosPed()));
        cmbEstado.setSelectedItem(p.getEstadoPed());

        cargarDetalle(idPed);
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
                        d.getIdPedDet(),
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
            return "Sandwich";
        }
        if (TIPO_MENU.equals(tipo)) {
            return "Menu";
        }
        return "Producto";
    }

    private String obtenerNombreItem(String tipo, int idItem) {
        if (TIPO_SANDWICH.equals(tipo)) {
            for (int i = 0; i < idsSandwich.size(); i++) {
                if (idsSandwich.get(i) == idItem && i < cmbItem.getItemCount()) {
                    return "Sandwich " + idItem;
                }
            }
            return "Sandwich " + idItem;
        }
        if (TIPO_MENU.equals(tipo)) {
            return "Menu " + idItem;
        }
        return "Producto " + idItem;
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
            pedido.setEstadoPed((String) cmbEstado.getSelectedItem());

            pedidoService.insertar(pedido);
            JOptionPane.showMessageDialog(this, "Pedido creado correctamente. Ahora puede agregarle items.");
            cargarTablaPedidos();
            seleccionarEnTabla(pedido.getIdPed());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarPedido() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int idPed = Integer.parseInt(txtId.getText().trim());
            Pedidos pedido = pedidoService.buscarPorId(idPed);
            if (pedido == null) {
                JOptionPane.showMessageDialog(this, "El pedido no fue encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            pedido.setIdCliPed(idsCliente.get(cmbCliente.getSelectedIndex()));
            pedido.setEstadoPed((String) cmbEstado.getSelectedItem());

            pedidoService.actualizar(pedido);
            JOptionPane.showMessageDialog(this, "Pedido actualizado correctamente.");
            cargarTablaPedidos();
            seleccionarEnTabla(idPed);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPedido() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el pedido seleccionado y su detalle?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                int idPed = Integer.parseInt(txtId.getText().trim());
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
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero debe crear o seleccionar un pedido para agregar items.", "Aviso", JOptionPane.WARNING_MESSAGE);
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

        int idPed = Integer.parseInt(txtId.getText().trim());
        int tipoIndex = cmbTipoItem.getSelectedIndex();
        int itemIndex = cmbItem.getSelectedIndex();

        String tipoCodigo = tipoIndex == 0 ? TIPO_SANDWICH : (tipoIndex == 1 ? TIPO_MENU : TIPO_PRODUCTO);
        int idItem = tipoIndex == 0 ? idsSandwich.get(itemIndex) : (tipoIndex == 1 ? idsMenu.get(itemIndex) : idsProducto.get(itemIndex));
        BigDecimal precioUnitario = new BigDecimal(txtPrecioUnitario.getText().trim());
        BigDecimal subtotal = precioUnitario.multiply(cantidad);
        int puntosGenerados = subtotal.divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).intValue();

        try {
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
        int fila = tablaDetalle.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila del detalle para quitar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idDet = (int) modeloDetalle.getValueAt(fila, 0);
        int idPed = Integer.parseInt(txtId.getText().trim());
        try {
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
        txtId.setText("");
        txtFecha.setText("");
        txtTotal.setText("0.00");
        txtPuntosObtenidos.setText("0");
        cmbEstado.setSelectedItem("P");
        txtCantidad.setText("1");
        tablaPedidos.clearSelection();
        modeloDetalle.setRowCount(0);
        if (clienteInicial != null) {
            seleccionarClienteInicial();
        }
    }

    private void abrirCobro() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla para procesar su cobro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idPed = Integer.parseInt(txtId.getText().trim());
        Pedidos p = pedidoService.buscarPorId(idPed);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "El pedido no fue encontrado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("C".equalsIgnoreCase(p.getEstadoPed())) {
            JOptionPane.showMessageDialog(this, "El pedido ya se encuentra pagado / completado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PagoView ventana = new PagoView(this, p);
        ventana.setVisible(true);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
