package vista;

import model.Cliente;
import model.Pagos;
import model.Pedidos;
import service.ClienteService;
import service.PagoService;
import service.PedidoService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
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

    private final JTextField txtIdPago = new JTextField();
    private final JComboBox<String> cmbPedido = new JComboBox<>();
    private final JTextField txtCliente = new JTextField();
    private final JTextField txtTotalPagar = new JTextField();
    private final JComboBox<String> cmbMetodoPago = new JComboBox<>(new String[]{"EF", "TC", "TR"});
    private final JTextField txtMontoRecibido = new JTextField();
    private final JTextField txtCambio = new JTextField();
    private final JTextField txtReferencia = new JTextField();
    private final JComboBox<String> cmbEstadoPago = new JComboBox<>(new String[]{"P", "A"});

    private final JButton btnProcesar = new JButton("Procesar Pago");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("Regresar");

    private final List<Integer> idsPedido = new ArrayList<>();
    private final List<BigDecimal> totalesPedido = new ArrayList<>();
    private final List<Integer> idsClientePedido = new ArrayList<>();

    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JTable tablaPagos = new JTable() {
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
    private final DefaultTableModel modeloPagos = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public PagoView() {
        this(null, null);
    }

    public PagoView(Window parent) {
        this(parent, null);
    }

    public PagoView(Window parent, Pedidos pedidoInicial) {
        super("Gestión y Cobro de Pagos");
        this.parent = parent;
        this.pedidoInicial = pedidoInicial;
        this.pagoService = new PagoService();
        this.pedidoService = new PedidoService();
        this.clienteService = new ClienteService();

        iniciarComponentes();
        cargarPedidos();
        cargarTablaPagos();
        seleccionarPedidoInicial();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 4, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Cobro / Pago"));

        txtIdPago.setEditable(false);
        txtIdPago.setPreferredSize(new Dimension(150, 25));

        txtCliente.setEditable(false);
        txtCliente.setPreferredSize(new Dimension(200, 25));

        txtTotalPagar.setEditable(false);
        txtTotalPagar.setText("0.00");
        txtTotalPagar.setPreferredSize(new Dimension(150, 25));

        txtCambio.setEditable(false);
        txtCambio.setText("0.00");
        txtCambio.setPreferredSize(new Dimension(150, 25));

        txtMontoRecibido.setText("0.00");
        txtMontoRecibido.setPreferredSize(new Dimension(150, 25));
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

        txtReferencia.setPreferredSize(new Dimension(150, 25));

        cmbPedido.setPreferredSize(new Dimension(240, 25));
        cmbPedido.addActionListener(e -> seleccionarPedidoDeCombo());

        panelFormulario.add(crearCampo("ID Pago:", txtIdPago));
        panelFormulario.add(crearCampo("Pedido a Cobrar:", cmbPedido));
        panelFormulario.add(crearCampo("Cliente:", txtCliente));
        panelFormulario.add(crearCampo("Total a Pagar:", txtTotalPagar));
        panelFormulario.add(crearCampo("Método de Pago:", cmbMetodoPago));
        panelFormulario.add(crearCampo("Monto Recibido:", txtMontoRecibido));
        panelFormulario.add(crearCampo("Cambio / Vuelto:", txtCambio));
        panelFormulario.add(crearCampo("No. Referencia:", txtReferencia));

        add(panelFormulario, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Historial de Pagos"));

        modeloPagos.setColumnIdentifiers(new String[]{"ID Pago", "ID Pedido", "Fecha", "Método", "Monto Recibido", "Cambio", "Referencia", "Estado"});
        tablaPagos.setModel(modeloPagos);
        tablaPagos.setFillsViewportHeight(true);
        tablaPagos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPagos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFilaPago();
            }
        });

        panelTabla.add(new JScrollPane(tablaPagos), BorderLayout.CENTER);
        panelCentral.add(panelTabla, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnProcesar.setPreferredSize(new Dimension(140, 30));
        btnEliminar.setPreferredSize(new Dimension(110, 30));
        btnLimpiar.setPreferredSize(new Dimension(110, 30));
        btnRegresar.setPreferredSize(new Dimension(110, 30));

        btnProcesar.addActionListener(e -> procesarCobro());
        btnEliminar.addActionListener(e -> eliminarPago());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnProcesar);
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

    private void cargarPedidos() {
        cmbPedido.removeAllItems();
        idsPedido.clear();
        totalesPedido.clear();
        idsClientePedido.clear();

        try {
            List<Pedidos> lista = pedidoService.listar();
            for (Pedidos p : lista) {
                Cliente c = clienteService.buscarClientePorId(p.getIdCliPed());
                String nombreCliente = c != null ? c.getNombreCli() : "Cliente " + p.getIdCliPed();
                String etiqueta = "Pedido #" + p.getIdPed() + " - " + nombreCliente + " (Q" + p.getTotalPed() + " - " + p.getEstadoPed() + ")";
                cmbPedido.addItem(etiqueta);
                idsPedido.add(p.getIdPed());
                totalesPedido.add(p.getTotalPed());
                idsClientePedido.add(p.getIdCliPed());
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
        if (index == -1 || index >= idsClientePedido.size() || index >= totalesPedido.size()) {
            txtCliente.setText("");
            txtTotalPagar.setText("0.00");
            txtMontoRecibido.setText("0.00");
            txtCambio.setText("0.00");
            return;
        }
        int idCli = idsClientePedido.get(index);
        Cliente c = clienteService.buscarClientePorId(idCli);
        txtCliente.setText(c != null ? c.getNombreCli() + " (DPI: " + c.getDpiCli() + ")" : "Cliente " + idCli);

        BigDecimal total = totalesPedido.get(index);
        txtTotalPagar.setText(total.toPlainString());
        txtMontoRecibido.setText(total.toPlainString());
        calcularCambio();
    }

    private void calcularCambio() {
        try {
            BigDecimal total = new BigDecimal(txtTotalPagar.getText().trim());
            BigDecimal recibido = new BigDecimal(txtMontoRecibido.getText().trim());
            BigDecimal cambio = recibido.subtract(total);
            if (cambio.compareTo(BigDecimal.ZERO) < 0) {
                txtCambio.setText("0.00");
            } else {
                txtCambio.setText(cambio.toPlainString());
            }
        } catch (Exception ex) {
            txtCambio.setText("0.00");
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
                        p.getMetodoPagoPag(),
                        p.getMontoRecibidoPag(),
                        p.getCambioPag(),
                        p.getNumeroReferenciaPag() != null ? p.getNumeroReferenciaPag() : "",
                        p.getEstadoPagoPag()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de pagos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFilaPago() {
        int fila = tablaPagos.getSelectedRow();
        if (fila == -1) {
            return;
        }
        txtIdPago.setText(String.valueOf(modeloPagos.getValueAt(fila, 0)));
        int idPed = (int) modeloPagos.getValueAt(fila, 1);
        for (int i = 0; i < idsPedido.size(); i++) {
            if (idsPedido.get(i) == idPed) {
                cmbPedido.setSelectedIndex(i);
                break;
            }
        }
        cmbMetodoPago.setSelectedItem(String.valueOf(modeloPagos.getValueAt(fila, 3)));
        txtMontoRecibido.setText(String.valueOf(modeloPagos.getValueAt(fila, 4)));
        txtCambio.setText(String.valueOf(modeloPagos.getValueAt(fila, 5)));
        txtReferencia.setText(String.valueOf(modeloPagos.getValueAt(fila, 6)));
        cmbEstadoPago.setSelectedItem(String.valueOf(modeloPagos.getValueAt(fila, 7)));
    }

    private void procesarCobro() {
        int index = cmbPedido.getSelectedIndex();
        if (index == -1 || index >= idsPedido.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para cobrar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPed = idsPedido.get(index);
        Pedidos pedido = pedidoService.buscarPorId(idPed);
        if (pedido == null) {
            JOptionPane.showMessageDialog(this, "El pedido no fue encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pagos pagoExistente = pagoService.buscarPorPedido(idPed);
        if (pagoExistente != null) {
            JOptionPane.showMessageDialog(this, "El pedido #" + idPed + " ya tiene un pago registrado (Pago #" + pagoExistente.getIdPad() + ").", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal montoRecibido;
        try {
            montoRecibido = new BigDecimal(txtMontoRecibido.getText().trim());
            if (montoRecibido.compareTo(pedido.getTotalPed()) < 0) {
                JOptionPane.showMessageDialog(this, "El monto recibido no puede ser menor al total a pagar.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El monto recibido debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal cambio = montoRecibido.subtract(pedido.getTotalPed());
        String metodo = (String) cmbMetodoPago.getSelectedItem();
        String referencia = txtReferencia.getText().trim();

        if (("TC".equals(metodo) || "TR".equals(metodo)) && referencia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Para pagos con tarjeta o transferencia se requiere un número de referencia.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

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

            if (parent instanceof PedidoView) {
                ((PedidoView) parent).dispose();
                new PedidoView(null).setVisible(true);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar el pago: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPago() {
        if (txtIdPago.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pago de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el registro de pago seleccionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                int idPag = Integer.parseInt(txtIdPago.getText().trim());
                pagoService.eliminar(idPag);
                JOptionPane.showMessageDialog(this, "Pago eliminado correctamente.");
                cargarTablaPagos();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar pago: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        txtIdPago.setText("");
        txtReferencia.setText("");
        cmbMetodoPago.setSelectedIndex(0);
        cmbEstadoPago.setSelectedIndex(0);
        tablaPagos.clearSelection();
        seleccionarPedidoDeCombo();
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
