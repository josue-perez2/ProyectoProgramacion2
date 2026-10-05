package vista;

import model.Cliente;
import model.Pagos;
import model.Pedidos;
import service.ClienteService;
import service.PagoService;
import service.PedidoService;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PagoView extends JFrame {

    private final PagoService pagoService;
    private final PedidoService pedidoService;
    private final ClienteService clienteService;

    private final Window parent;
    private final Pedidos pedidoInicial;
    private Integer idPagoSeleccionado = null;

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

        txtReferencia.setPreferredSize(new Dimension(160, 36));
        cmbMetodoPago.setPreferredSize(new Dimension(180, 36));

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
        panelCamposCobro.add(FabricaDaisyUI.crearCampoConEtiqueta("No. Referencia:", txtReferencia));

        JPanel tarjetaCobro = FabricaDaisyUI.crearTarjetaSeccion(
                "Cobro de Pedido",
                panelCamposCobro
        );
        panelContenedorPrincipal.add(tarjetaCobro, BorderLayout.NORTH);

        modeloPagos.setColumnIdentifiers(new String[]{"ID Pago", "ID Pedido", "Fecha", "Método", "Monto Recibido", "Cambio", "Referencia", "Estado"});
        tablaPagos.setModel(modeloPagos);
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
        btnVerFactura.setEnabled(seleccionActiva || cmbPedido.getSelectedIndex() != -1);
    }

    private void cargarPedidos() {
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
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los pedidos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
        } else if ("A".equalsIgnoreCase(estado)) {
            txtTotalPagar.setText(total.toPlainString() + " (ANULADO)");
            btnProcesar.setEnabled(false);
        } else {
            txtTotalPagar.setText(total.toPlainString());
            btnProcesar.setEnabled(total.compareTo(BigDecimal.ZERO) > 0);
        }

        txtMontoRecibido.setText(total.toPlainString());
        calcularCambio();
    }

    private void calcularCambio() {
        int index = cmbPedido.getSelectedIndex();
        if (index != -1 && index < estadosPedido.size()) {
            String est = estadosPedido.get(index);
            if ("C".equalsIgnoreCase(est) || "A".equalsIgnoreCase(est)) {
                btnProcesar.setEnabled(false);
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
            JOptionPane.showMessageDialog(this, "No se pudo cargar el historial de pagos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFilaPago() {
        int fila = tablaPagos.getSelectedRow();
        if (fila == -1) {
            idPagoSeleccionado = null;
            txtReferencia.setText("");
            cmbMetodoPago.setSelectedIndex(0);
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
        cmbMetodoPago.setSelectedItem(String.valueOf(modeloPagos.getValueAt(filaModelo, 3)));
        txtMontoRecibido.setText(String.valueOf(modeloPagos.getValueAt(filaModelo, 4)));
        txtReferencia.setText(String.valueOf(modeloPagos.getValueAt(filaModelo, 6)));
        actualizarEstadoBotones(true);
    }

    private void procesarCobro() {
        int index = cmbPedido.getSelectedIndex();
        if (index == -1 || index >= idsPedido.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido válido para cobrar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPed = idsPedido.get(index);
        Pedidos pedido = pedidoService.buscarPorId(idPed);
        if (pedido == null) {
            JOptionPane.showMessageDialog(this, "El pedido no fue encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("C".equalsIgnoreCase(pedido.getEstadoPed())) {
            JOptionPane.showMessageDialog(this, "Este pedido ya fue pagado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        BigDecimal total = pedido.getTotalPed();
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "No se puede procesar el cobro de un pedido con total Q0.00.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal montoRecibido;
        try {
            montoRecibido = new BigDecimal(txtMontoRecibido.getText().trim());
            if (montoRecibido.compareTo(total) < 0) {
                JOptionPane.showMessageDialog(this,
                        "El monto recibido (Q" + montoRecibido + ") es menor que el total a pagar (Q" + total + ").",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El monto recibido debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal cambio = montoRecibido.subtract(total);
        String metodo = metodoAbreviado((String) cmbMetodoPago.getSelectedItem());
        String referencia = txtReferencia.getText().trim();

        try {
            Pagos pago = new Pagos();
            pago.setIdPedPag(idPed);
            pago.setFechaPag(LocalDateTime.now());
            pago.setMetodoPagoPag(metodo);
            pago.setMontoRecibidoPag(montoRecibido);
            pago.setCambioPag(cambio);
            pago.setNumeroReferenciaPag(referencia.isEmpty() ? null : referencia);
            pago.setEstadoPagoPag("P");

            Cliente cliente = clienteService.buscarClientePorId(pedido.getIdCliPed());
            pagoService.procesarPago(pago, pedido, cliente);

            JOptionPane.showMessageDialog(this, "Cobro completado con éxito.\n"
                    + "Pedido #" + idPed + " marcado como Pagado.\n"
                    + "Puntos acreditados: " + pedido.getPuntosObtenidosPed() + "\n"
                    + "Cambio a entregar: Q" + cambio.toPlainString());

            cargarPedidos();
            cargarTablaPagos();
            limpiarFormulario();

            FacturaView factura = new FacturaView(this, idPed);
            factura.setVisible(true);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar el pago: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            int idPed = idsPedido.get(index);
            new FacturaView(this, idPed).setVisible(true);
            return;
        }
        JOptionPane.showMessageDialog(this, "Seleccione un pago de la tabla o un pedido para ver su factura.", "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void eliminarPago() {
        if (idPagoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un pago de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el registro de pago seleccionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                pagoService.eliminar(idPagoSeleccionado);
                JOptionPane.showMessageDialog(this, "Pago eliminado correctamente.");
                cargarTablaPagos();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar pago: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        idPagoSeleccionado = null;
        txtReferencia.setText("");
        cmbMetodoPago.setSelectedIndex(0);
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
        if ("Tarjeta Crédito/Débito".equalsIgnoreCase(metodoCompleto)) {
            return "TC";
        }
        if ("Transferencia".equalsIgnoreCase(metodoCompleto)) {
            return "TR";
        }
        return "EF";
    }

    private String metodoCompleto(String metodoAbreviado) {
        if ("TC".equalsIgnoreCase(metodoAbreviado) || "TARJETA".equalsIgnoreCase(metodoAbreviado)) {
            return "Tarjeta Crédito/Débito";
        }
        if ("TR".equalsIgnoreCase(metodoAbreviado) || "TRANSFERENCIA".equalsIgnoreCase(metodoAbreviado)) {
            return "Transferencia";
        }
        return "Efectivo";
    }
}
