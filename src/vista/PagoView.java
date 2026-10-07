package vista;

import model.Cliente;
import model.Pagos;
import model.pagos.Pago;
import model.pagos.PagoEfectivo;
import model.pagos.PagoTarjeta;
import model.Pedidos;
import service.ClienteService;
import service.PagoService;
import service.PedidoService;
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
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PagoView extends JFrame implements Actualizable {

    private final PagoService pagoService;
    private final PedidoService pedidoService;
    private final ClienteService clienteService;

    private final Window parent;
    private final Pedidos pedidoInicial;
    private Integer idPagoSeleccionado = null;
    private boolean ignorarEventoComboMetodo = false;

    private final JComboBox<String> cmbPedido = new JComboBox<>();
    private final JTextField txtCliente = new JTextField();
    private final JTextField txtTotalPagar = new JTextField();
    private final JComboBox<String> cmbMetodoPago = new JComboBox<>(new String[]{"Efectivo", "Tarjeta Crédito/Débito", "Transferencia"});
    private final JTextField txtMontoRecibido = new JTextField();
    private final JTextField txtCambio = new JTextField();
    private final JTextField txtReferencia = new JTextField();

    private final JButton btnProcesar = FabricaDaisyUI.crearBotonPrimario("Procesar Pago", Icons.creditCard(16), null);
    private final JButton btnVerFactura = FabricaDaisyUI.crearBotonAcento("Ver Factura", Icons.receipt(16), null);
    private final JButton btnEliminar = FabricaDaisyUI.crearBotonPeligro("Eliminar", Icons.trash(16), null);
    private final JButton btnLimpiar = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), null);
    private final JButton btnRegresar = FabricaDaisyUI.crearBotonNeutral("Volver", Icons.arrowLeft(16), null);
    private final JButton btnTerminalTarjeta = FabricaDaisyUI.crearBotonSecundario("Cobrar con Tarjeta", Icons.creditCard(16), null);

    private final List<Integer> idsPedido = new ArrayList<>();
    private final List<BigDecimal> totalesPedido = new ArrayList<>();
    private final List<Integer> idsClientePedido = new ArrayList<>();
    private final List<String> estadosPedido = new ArrayList<>();

    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DefaultTableModel modeloPagos = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaPagos = new JTable(modeloPagos);

    public PagoView() {
        this(null, null);
    }

    public PagoView(Window parent) {
        this(parent, null);
    }

    public PagoView(Window parent, Pedidos pedidoInicial) {
        super("Gestión y Cobro de Pedidos");
        this.parent = parent;
        this.pedidoInicial = pedidoInicial;
        this.pagoService = new PagoService();
        this.pedidoService = new PedidoService();
        this.clienteService = new ClienteService();

        GestorVentanas.registrarVentana(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                GestorVentanas.desregistrarVentana(PagoView.class);
            }
        });

        iniciarComponentes();
        cargarPedidos();
        cargarTablaPagos();
        seleccionarPedidoInicial();
        actualizarEstadoBotones(false);
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedorPrincipal = new JPanel(new BorderLayout(12, 12));
        panelContenedorPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        panelContenedorPrincipal.setOpaque(false);

        JPanel panelCamposCobro = new JPanel(new GridLayout(2, 4, 14, 10));
        panelCamposCobro.setOpaque(false);

        txtCliente.setPreferredSize(new Dimension(220, 36));
        txtTotalPagar.setText("0.00");
        txtTotalPagar.setPreferredSize(new Dimension(140, 36));
        txtCambio.setText("0.00");
        txtCambio.setPreferredSize(new Dimension(140, 36));

        FabricaDaisyUI.aplicarCampoEstatico(txtCliente);
        FabricaDaisyUI.aplicarCampoEstatico(txtTotalPagar);
        FabricaDaisyUI.aplicarCampoEstatico(txtCambio);

        txtMontoRecibido.setText("0.00");
        txtMontoRecibido.setPreferredSize(new Dimension(140, 36));
        txtMontoRecibido.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calcularCambio();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calcularCambio();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calcularCambio();
            }
        });

        btnTerminalTarjeta.setPreferredSize(new Dimension(190, 36));
        btnTerminalTarjeta.setEnabled(false);
        btnTerminalTarjeta.addActionListener(e -> abrirSimuladorTarjeta());

        txtReferencia.setPreferredSize(new Dimension(160, 36));
        cmbMetodoPago.setPreferredSize(new Dimension(180, 36));
        cmbMetodoPago.addActionListener(e -> {
            if (ignorarEventoComboMetodo) {
                return;
            }
            String sel = (String) cmbMetodoPago.getSelectedItem();
            boolean esTarjeta = sel != null && sel.toLowerCase().contains("tarjeta");
            int idx = cmbPedido.getSelectedIndex();
            boolean pendienteValido = false;
            if (idx != -1 && idx < estadosPedido.size() && idx < totalesPedido.size()) {
                pendienteValido = "P".equalsIgnoreCase(estadosPedido.get(idx)) && totalesPedido.get(idx).compareTo(BigDecimal.ZERO) > 0;
            }
            btnTerminalTarjeta.setEnabled(esTarjeta && pendienteValido);
            if (esTarjeta && pendienteValido) {
                BigDecimal tot = totalesPedido.get(idx);
                txtMontoRecibido.setText(tot.toPlainString());
                txtCambio.setText("0.00");
                abrirSimuladorTarjeta();
            } else if (!esTarjeta) {
                if (txtReferencia.getText().trim().startsWith("AUTH-")) {
                    txtReferencia.setText("");
                }
            }
        });

        cmbPedido.setPreferredSize(new Dimension(280, 36));
        cmbPedido.addActionListener(e -> seleccionarPedidoDeCombo());

        FabricaDaisyUI.estilizarCampo(cmbPedido);
        FabricaDaisyUI.estilizarCampo(cmbMetodoPago);
        FabricaDaisyUI.estilizarCampo(txtMontoRecibido);
        FabricaDaisyUI.estilizarCampo(txtReferencia);

        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("Pedido:", cmbPedido));
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("Cliente:", txtCliente));
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("Total a Pagar (Q):", txtTotalPagar));
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("Método de Pago:", cmbMetodoPago));
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("Monto Recibido (Q):", txtMontoRecibido));
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("Cambio (Q):", txtCambio));
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("No. Referencia / Auth:", txtReferencia));
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("Terminal POS:", btnTerminalTarjeta));

        JPanel tarjetaCobro = FabricaDaisyUI.crearTarjetaSeccion(
                "Cobro de Pedido",
                panelCamposCobro
        );
        panelContenedorPrincipal.add(tarjetaCobro, BorderLayout.NORTH);

        modeloPagos.setColumnIdentifiers(new String[]{"ID Pago", "ID Pedido", "Fecha", "Método", "Monto Recibido", "Cambio", "Referencia", "Estado"});
        tablaPagos.setModel(modeloPagos);
        tablaPagos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tablaPagos);

        TableColumnModel colModel = tablaPagos.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(50);
        colModel.getColumn(0).setMaxWidth(65);
        colModel.getColumn(1).setPreferredWidth(65);
        colModel.getColumn(1).setMaxWidth(80);
        colModel.getColumn(2).setPreferredWidth(140);
        colModel.getColumn(3).setPreferredWidth(130);
        colModel.getColumn(4).setPreferredWidth(110);
        colModel.getColumn(5).setPreferredWidth(95);
        colModel.getColumn(6).setPreferredWidth(120);
        colModel.getColumn(7).setPreferredWidth(95);

        tablaPagos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFilaPago();
            }
        });
        tablaPagos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verFactura();
                }
            }
        });

        tablaPagos.getColumnModel().getColumn(7).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());

        JScrollPane scrollPagos = new JScrollPane(tablaPagos);
        scrollPagos.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> {
            cargarPedidos();
            cargarTablaPagos();
        });

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Historial de Pagos",
                btnRefrescar,
                scrollPagos
        );
        panelContenedorPrincipal.add(tarjetaTabla, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(4, 16, 14, 16));
        panelBotones.setOpaque(false);

        btnProcesar.setPreferredSize(new Dimension(160, 38));
        btnVerFactura.setPreferredSize(new Dimension(145, 38));
        btnEliminar.setPreferredSize(new Dimension(135, 38));
        btnLimpiar.setPreferredSize(new Dimension(130, 38));
        btnRegresar.setPreferredSize(new Dimension(130, 38));

        btnProcesar.setIconTextGap(8);
        btnVerFactura.setIconTextGap(8);
        btnEliminar.setIconTextGap(8);
        btnLimpiar.setIconTextGap(8);
        btnRegresar.setIconTextGap(8);

        FabricaDaisyUI.aplicarBotonPrimario(btnProcesar);
        FabricaDaisyUI.aplicarBotonAcento(btnVerFactura);
        FabricaDaisyUI.aplicarBotonPeligro(btnEliminar);
        FabricaDaisyUI.aplicarBotonNeutral(btnLimpiar);
        FabricaDaisyUI.aplicarBotonNeutral(btnRegresar);

        btnProcesar.addActionListener(e -> procesarCobro());
        btnVerFactura.addActionListener(e -> verFactura());
        btnEliminar.addActionListener(e -> eliminarPago());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnProcesar);
        panelBotones.add(btnVerFactura);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRegresar);

        add(panelContenedorPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void actualizarEstadoBotones(boolean seleccionActiva) {
        btnEliminar.setEnabled(seleccionActiva);
        boolean cobradoEnCombo = false;
        int idx = cmbPedido.getSelectedIndex();
        if (idx != -1 && idx < estadosPedido.size()) {
            cobradoEnCombo = "C".equalsIgnoreCase(estadosPedido.get(idx));
        }
        btnVerFactura.setEnabled(seleccionActiva || cobradoEnCombo);
    }

    private void cargarPedidos() {
        int idxSeleccionado = cmbPedido.getSelectedIndex();
        Integer idActual = null;
        if (idxSeleccionado != -1 && idxSeleccionado < idsPedido.size()) {
            idActual = idsPedido.get(idxSeleccionado);
        }

        cmbPedido.removeAllItems();
        idsPedido.clear();
        totalesPedido.clear();
        idsClientePedido.clear();
        estadosPedido.clear();

        try {
            List<Pedidos> lista = pedidoService.listar();
            for (Pedidos p : lista) {
                Cliente c = clienteService.buscarClientePorId(p.getIdCliPed());
                String nombreCliente = c != null ? c.getNombreCli() : "Cliente #" + p.getIdCliPed();
                String estadoSufijo = "C".equalsIgnoreCase(p.getEstadoPed()) ? " [PAGADO]" : ("A".equalsIgnoreCase(p.getEstadoPed()) ? " [ANULADO]" : "");
                String etiqueta = "Pedido #" + p.getIdPed() + " - " + nombreCliente + " (Q" + p.getTotalPed() + ")" + estadoSufijo;
                cmbPedido.addItem(etiqueta);
                idsPedido.add(p.getIdPed());
                totalesPedido.add(p.getTotalPed());
                idsClientePedido.add(p.getIdCliPed());
                estadosPedido.add(p.getEstadoPed());
            }

            if (idActual != null) {
                for (int i = 0; i < idsPedido.size(); i++) {
                    if (idsPedido.get(i).equals(idActual)) {
                        cmbPedido.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error de Datos", "No se pudieron cargar los pedidos: " + ex.getMessage());
        }
    }

    private void seleccionarPedidoInicial() {
        if (pedidoInicial == null) {
            return;
        }
        for (int i = 0; i < idsPedido.size(); i++) {
            if (idsPedido.get(i) == pedidoInicial.getIdPed()) {
                cmbPedido.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarPedidoDeCombo() {
        int index = cmbPedido.getSelectedIndex();
        if (index == -1 || index >= idsPedido.size()) {
            txtCliente.setText("");
            txtTotalPagar.setText("0.00");
            txtMontoRecibido.setText("0.00");
            txtCambio.setText("0.00");
            btnProcesar.setEnabled(false);
            btnTerminalTarjeta.setEnabled(false);
            return;
        }

        int idCli = idsClientePedido.get(index);
        Cliente cliente = clienteService.buscarClientePorId(idCli);
        txtCliente.setText(cliente != null ? cliente.getNombreCli() : "Cliente #" + idCli);

        BigDecimal total = totalesPedido.get(index);
        String estado = estadosPedido.get(index);

        if ("C".equalsIgnoreCase(estado)) {
            txtTotalPagar.setText(total.toPlainString() + " (PAGADO)");
            btnProcesar.setEnabled(false);
            btnTerminalTarjeta.setEnabled(false);
        } else if ("A".equalsIgnoreCase(estado)) {
            txtTotalPagar.setText(total.toPlainString() + " (ANULADO)");
            btnProcesar.setEnabled(false);
            btnTerminalTarjeta.setEnabled(false);
        } else {
            txtTotalPagar.setText(total.toPlainString());
            boolean tieneMontoValido = total.compareTo(BigDecimal.ZERO) > 0;
            btnProcesar.setEnabled(tieneMontoValido);
            btnTerminalTarjeta.setEnabled(tieneMontoValido);
        }

        txtMontoRecibido.setText(total.toPlainString());
        actualizarEstadoBotones(tablaPagos.getSelectedRow() != -1);
        calcularCambio();
    }

    private void calcularCambio() {
        int index = cmbPedido.getSelectedIndex();
        if (index != -1 && index < estadosPedido.size()) {
            String est = estadosPedido.get(index);
            if ("C".equalsIgnoreCase(est) || "A".equalsIgnoreCase(est)) {
                btnProcesar.setEnabled(false);
                btnTerminalTarjeta.setEnabled(false);
                return;
            }
        }

        try {
            BigDecimal total = index >= 0 && index < totalesPedido.size() ? totalesPedido.get(index) : BigDecimal.ZERO;
            BigDecimal recibido = new BigDecimal(txtMontoRecibido.getText().trim().isEmpty() ? "0" : txtMontoRecibido.getText().trim());
            BigDecimal cambio = recibido.subtract(total);
            if (cambio.compareTo(BigDecimal.ZERO) < 0) {
                txtCambio.setText("0.00 (Falta Q" + total.subtract(recibido).toPlainString() + ")");
                btnProcesar.setEnabled(false);
            } else {
                txtCambio.setText(cambio.toPlainString());
                btnProcesar.setEnabled(total.compareTo(BigDecimal.ZERO) > 0);
            }
        } catch (NumberFormatException ex) {
            txtCambio.setText("0.00");
            btnProcesar.setEnabled(false);
        }
    }

    private void cargarTablaPagos() {
        modeloPagos.setRowCount(0);
        try {
            List<Pagos> lista = pagoService.listar();
            for (Pagos p : lista) {
                String fechaTexto = p.getFechaPag() != null ? p.getFechaPag().format(formateadorFecha) : "";
                modeloPagos.addRow(new Object[]{
                        p.getIdPad(),
                        p.getIdPedPag(),
                        fechaTexto,
                        metodoCompleto(p.getMetodoPagoPag()),
                        p.getMontoRecibidoPag(),
                        p.getCambioPag(),
                        p.getNumeroReferenciaPag() != null ? p.getNumeroReferenciaPag() : "",
                        "P".equalsIgnoreCase(p.getEstadoPagoPag()) ? "PAGADO" : "ANULADO"
                });
            }
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error de Datos", "No se pudo cargar el historial de pagos: " + ex.getMessage());
        }
    }

    private void seleccionarFilaPago() {
        int fila = tablaPagos.getSelectedRow();
        if (fila == -1) {
            idPagoSeleccionado = null;
            txtReferencia.setText("");
            setMetodoPagoSinDisparar(0);
            seleccionarPedidoDeCombo();
            actualizarEstadoBotones(false);
            return;
        }
        int filaModelo = tablaPagos.convertRowIndexToModel(fila);
        idPagoSeleccionado = (int) modeloPagos.getValueAt(filaModelo, 0);
        int idPed = (int) modeloPagos.getValueAt(filaModelo, 1);
        for (int i = 0; i < idsPedido.size(); i++) {
            if (idsPedido.get(i) == idPed) {
                cmbPedido.setSelectedIndex(i);
                break;
            }
        }
        String met = String.valueOf(modeloPagos.getValueAt(filaModelo, 3));
        setMetodoPagoSinDisparar(met);
        txtMontoRecibido.setText(String.valueOf(modeloPagos.getValueAt(filaModelo, 4)));
        txtReferencia.setText(String.valueOf(modeloPagos.getValueAt(filaModelo, 6)));
        btnProcesar.setEnabled(false);
        btnTerminalTarjeta.setEnabled(false);
        actualizarEstadoBotones(true);
    }

    private void setMetodoPagoSinDisparar(Object item) {
        ignorarEventoComboMetodo = true;
        if (item instanceof Integer idx) {
            cmbMetodoPago.setSelectedIndex(idx);
        } else if (item != null) {
            cmbMetodoPago.setSelectedItem(item.toString());
        }
        ignorarEventoComboMetodo = false;
    }

    private void abrirSimuladorTarjeta() {
        int index = cmbPedido.getSelectedIndex();
        if (index == -1 || index >= idsPedido.size()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Selección Requerida", "Seleccione primero un pedido para ingresar los datos de la tarjeta.");
            return;
        }

        int idPed = idsPedido.get(index);
        Pedidos pedido = pedidoService.buscarPorId(idPed);
        if (pedido == null) {
            FabricaDaisyUI.mostrarError(this, "Error", "El pedido no fue encontrado.");
            return;
        }

        if ("C".equalsIgnoreCase(pedido.getEstadoPed())) {
            FabricaDaisyUI.mostrarInformacion(this, "Aviso", "Este pedido ya se encuentra pagado.");
            btnProcesar.setEnabled(false);
            btnTerminalTarjeta.setEnabled(false);
            return;
        }

        BigDecimal total = pedido.getTotalPed();
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Total Inválido", "No se puede procesar cobro con tarjeta para un pedido con total Q0.00.");
            return;
        }

        SimuladorTarjetaDialog simDialog = new SimuladorTarjetaDialog(this, idPed, total);
        simDialog.setVisible(true);

        if (simDialog.isAprobada()) {
            String ref = simDialog.getCodigoAutorizacion() + " (" + simDialog.getTarjetaEnmascarada() + ")";
            setMetodoPagoSinDisparar("Tarjeta Crédito/Débito");
            txtReferencia.setText(ref);
            txtMontoRecibido.setText(total.toPlainString());
            txtCambio.setText("0.00");
            ejecutarPagoFinal(pedido, total, total, BigDecimal.ZERO, "TC", ref);
        } else {
            setMetodoPagoSinDisparar(0);
        }
    }

    private void procesarCobro() {
        int index = cmbPedido.getSelectedIndex();
        if (index == -1 || index >= idsPedido.size()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "Seleccione un pedido válido para cobrar.");
            return;
        }

        int idPed = idsPedido.get(index);
        Pedidos pedido = pedidoService.buscarPorId(idPed);
        if (pedido == null) {
            FabricaDaisyUI.mostrarError(this, "Error", "El pedido no fue encontrado.");
            return;
        }

        if ("C".equalsIgnoreCase(pedido.getEstadoPed())) {
            FabricaDaisyUI.mostrarInformacion(this, "Aviso", "Este pedido ya fue pagado.");
            btnProcesar.setEnabled(false);
            btnTerminalTarjeta.setEnabled(false);
            return;
        }

        BigDecimal total = pedido.getTotalPed();
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "No se puede procesar el cobro de un pedido con total Q0.00.");
            return;
        }

        String metodo = metodoAbreviado((String) cmbMetodoPago.getSelectedItem());
        boolean esTarjeta = "TC".equalsIgnoreCase(metodo) || "T".equalsIgnoreCase(metodo)
                || (cmbMetodoPago.getSelectedItem() != null && cmbMetodoPago.getSelectedItem().toString().toLowerCase().contains("tarjeta"));

        if (esTarjeta) {
            String referencia = txtReferencia.getText().trim();
            if (!referencia.startsWith("AUTH-")) {
                abrirSimuladorTarjeta();
                return;
            }
            ejecutarPagoFinal(pedido, total, total, BigDecimal.ZERO, "TC", referencia);
            return;
        }

        BigDecimal montoRecibido;
        try {
            montoRecibido = new BigDecimal(txtMontoRecibido.getText().trim());
            if (montoRecibido.compareTo(total) < 0) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Validación",
                        "El monto recibido (Q" + montoRecibido + ") es menor que el total a pagar (Q" + total + ").");
                return;
            }
        } catch (NumberFormatException ex) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "El monto recibido debe ser un número válido.");
            return;
        }

        BigDecimal cambio = montoRecibido.subtract(total);
        ejecutarPagoFinal(pedido, total, montoRecibido, cambio, "E", txtReferencia.getText().trim());
    }

    private void ejecutarPagoFinal(Pedidos pedido, BigDecimal total, BigDecimal recibido, BigDecimal cambio, String metodo, String referencia) {
        try {
            Pago pago;
            if ("TC".equalsIgnoreCase(metodo) || "T".equalsIgnoreCase(metodo)) {
                pago = new PagoTarjeta(pedido.getIdPed(), total, referencia, "VISA", null);
            } else {
                pago = new PagoEfectivo(pedido.getIdPed(), total, recibido);
            }

            Cliente cliente = clienteService.buscarClientePorId(pedido.getIdCliPed());
            pagoService.procesarPago(pago, pedido, cliente);

            FabricaDaisyUI.mostrarExito(this, "Cobro Completado con Éxito",
                    "El cobro del pedido ha sido registrado correctamente.\n\n"
                    + "• No. Pedido: #" + pedido.getIdPed() + " (Estado: PAGADO)\n"
                    + "• Método de Pago: " + ("TC".equalsIgnoreCase(metodo) ? "Tarjeta Crédito/Débito" : "Efectivo") + "\n"
                    + (referencia != null && !referencia.isEmpty() ? "• No. Autorización / Ref: " + referencia + "\n" : "")
                    + "• Total Cobrado: Q" + total.toPlainString() + "\n"
                    + "• Cambio Entregado: Q" + cambio.toPlainString() + "\n"
                    + "• Puntos Fidelidad Sumados: " + pedido.getPuntosObtenidosPed() + " pts");

            cargarPedidos();
            cargarTablaPagos();
            limpiarFormulario();
            btnProcesar.setEnabled(false);
            btnTerminalTarjeta.setEnabled(false);

            GestorVentanas.notificarCambio("PAGO");

            FacturaView factura = new FacturaView(this, pedido.getIdPed());
            factura.setVisible(true);
        } catch (Exception ex) {
            FabricaDaisyUI.mostrarError(this, "Error al Procesar Cobro", ex.getMessage());
        }
    }

    private void verFactura() {
        int fila = tablaPagos.getSelectedRow();
        if (fila != -1) {
            int idPed = (int) modeloPagos.getValueAt(fila, 1);
            new FacturaView(this, idPed).setVisible(true);
            return;
        }
        int index = cmbPedido.getSelectedIndex();
        if (index != -1 && index < idsPedido.size()) {
            String est = estadosPedido.get(index);
            if (!"C".equalsIgnoreCase(est)) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "El pedido seleccionado aún no ha sido cobrado. Las facturas solo se generan para pedidos pagados.");
                return;
            }
            int idPed = idsPedido.get(index);
            new FacturaView(this, idPed).setVisible(true);
            return;
        }
        FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un pago de la tabla o un pedido cobrado para ver su factura.");
    }

    private void eliminarPago() {
        if (idPagoSeleccionado == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione un pago de la tabla.");
            return;
        }
        boolean confirmar = FabricaDaisyUI.mostrarConfirmacion(this, "Confirmar Anulación", "¿Desea eliminar el registro de pago seleccionado?");
        if (confirmar) {
            try {
                pagoService.eliminar(idPagoSeleccionado);
                FabricaDaisyUI.mostrarExito(this, "Pago Eliminado", "El pago seleccionado ha sido eliminado correctamente.");
                cargarTablaPagos();
                limpiarFormulario();
                GestorVentanas.notificarCambio("PAGO");
            } catch (RuntimeException ex) {
                FabricaDaisyUI.mostrarError(this, "Error al Eliminar", ex.getMessage());
            }
        }
    }

    private void limpiarFormulario() {
        idPagoSeleccionado = null;
        txtReferencia.setText("");
        setMetodoPagoSinDisparar(0);
        btnTerminalTarjeta.setEnabled(false);
        tablaPagos.clearSelection();
        seleccionarPedidoDeCombo();
        actualizarEstadoBotones(false);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }

    private String metodoAbreviado(String metodoCompleto) {
        if (metodoCompleto != null && metodoCompleto.toLowerCase().contains("tarjeta")) {
            return "TC";
        }
        if (metodoCompleto != null && metodoCompleto.toLowerCase().contains("transferencia")) {
            return "TR";
        }
        return "E";
    }

    private String metodoCompleto(String metodoAbreviado) {
        if (metodoAbreviado != null) {
            String m = metodoAbreviado.toUpperCase().trim();
            if ("TC".equals(m) || "T".equals(m) || "TARJETA".equals(m)) {
                return "Tarjeta Crédito/Débito";
            }
            if ("TR".equals(m) || "TRANSFERENCIA".equals(m)) {
                return "Transferencia";
            }
        }
        return "Efectivo";
    }

    @Override
    public void actualizarDatos() {
        int idx = cmbPedido.getSelectedIndex();
        Integer idAntes = null;
        if (idx != -1 && idx < idsPedido.size()) {
            idAntes = idsPedido.get(idx);
        }
        cargarPedidos();
        cargarTablaPagos();
        if (idAntes != null) {
            for (int i = 0; i < idsPedido.size(); i++) {
                if (idsPedido.get(i).equals(idAntes)) {
                    cmbPedido.setSelectedIndex(i);
                    seleccionarPedidoDeCombo();
                    break;
                }
            }
        }
    }
}
