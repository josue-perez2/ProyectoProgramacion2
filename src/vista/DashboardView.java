package vista;

import model.Cliente;
import service.ClienteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardView extends JFrame {

    private final ClienteService clienteService;

    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtDpi = new JTextField();

    private final JButton btnSeleccionar = new JButton("Seleccionar");
    private final JButton btnPedido = new JButton("Nuevo Pedido");
    private final JButton btnAdministracion = new JButton("Administracion");
    private final JButton btnAdministracionCliente = new JButton("Administracion Cliente");

    private final JLabel lblEstado = new JLabel("Cliente seleccionado: (ninguno)");

    private final JTable tabla = new JTable() {
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
    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private Cliente clienteSeleccionado;

    public DashboardView() {
        super("Dashboard");
        this.clienteService = new ClienteService();
        this.clienteSeleccionado = null;
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 3, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Cliente seleccionado"));

        txtId.setEditable(false);
        txtId.setPreferredSize(new Dimension(200, 25));
        txtNombre.setEditable(false);
        txtNombre.setPreferredSize(new Dimension(200, 25));
        txtDpi.setEditable(false);
        txtDpi.setPreferredSize(new Dimension(200, 25));

        panelFormulario.add(crearCampo("ID:", txtId));
        panelFormulario.add(crearCampo("Nombre:", txtNombre));
        panelFormulario.add(crearCampo("DPI:", txtDpi));

        add(panelFormulario, BorderLayout.NORTH);

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Seleccion de clientes"));

        String[] columnas = {"ID", "DPI", "Nombre", "Saldo Puntos", "Estado"};
        modeloTabla.setColumnIdentifiers(columnas);
        tabla.setModel(modeloTabla);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        panelInferior.add(lblEstado, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnSeleccionar.setPreferredSize(new Dimension(130, 30));
        btnPedido.setPreferredSize(new Dimension(140, 30));
        btnAdministracion.setPreferredSize(new Dimension(150, 30));
        btnAdministracionCliente.setPreferredSize(new Dimension(200, 30));

        btnSeleccionar.addActionListener(e -> confirmarSeleccion());
        btnPedido.addActionListener(e -> abrirPedido());
        btnAdministracion.addActionListener(e -> abrirAdministracion());
        btnAdministracionCliente.addActionListener(e -> abrirAdministracionCliente());

        panelBotones.add(btnSeleccionar);
        panelBotones.add(btnPedido);
        panelBotones.add(btnAdministracion);
        panelBotones.add(btnAdministracionCliente);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panelInferior.add(panelBotones, BorderLayout.CENTER);

        add(panelInferior, BorderLayout.SOUTH);
        add(panelTabla, BorderLayout.CENTER);
    }

    private JPanel crearCampo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<Cliente> clientes = clienteService.listar();
            for (Cliente c : clientes) {
                modeloTabla.addRow(new Object[]{
                        c.getIdCli(),
                        c.getDpiCli(),
                        c.getNombreCli(),
                        c.getSaldoPuntoCli(),
                        c.getEstadoCli()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de clientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            return;
        }
        txtId.setText(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        txtDpi.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
    }

    private void confirmarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            clienteSeleccionado = clienteService.buscarClientePorId(Integer.parseInt(txtId.getText().trim()));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID del cliente no es valido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar el cliente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (clienteSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "El cliente ya no existe en la base de datos.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lblEstado.setText("Cliente seleccionado: " + clienteSeleccionado.getIdCli()
                + " - " + clienteSeleccionado.getNombreCli()
                + " (DPI " + clienteSeleccionado.getDpiCli() + ")");
        lblEstado.setForeground(new Color(0, 128, 0));
    }

    private void abrirPedido() {
        PedidoView ventana = new PedidoView(this, clienteSeleccionado);
        ventana.setVisible(true);
    }

    private void abrirAdministracion() {
        new AdministracionView(this).setVisible(true);
    }

    private void abrirAdministracionCliente() {
        ClienteView ventana = new ClienteView(this);
        ventana.setVisible(true);
    }
}
