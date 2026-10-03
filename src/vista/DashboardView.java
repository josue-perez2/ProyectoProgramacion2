package vista;

import model.Cliente;
import model.Pedidos;
import service.ClienteService;
import service.PedidoService;
import vista.util.FabricaDaisyUI;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class DashboardView extends JFrame {

    private final ClienteService clienteService;
    private final PedidoService pedidoService;

    private final JLabel lblTotalClientes = new JLabel("0");
    private final JLabel lblTotalPedidos = new JLabel("0");
    private final JLabel lblTotalPuntos = new JLabel("0");

    private final JTextField txtBuscar = FabricaDaisyUI.crearCampoTexto("Buscar cliente por DPI o nombre...", 25);
    private final JComboBox<String> cmbTemas = new JComboBox<>(TemaGestor.obtenerNombresTemas());

    private final JLabel lblEstadoSeleccion = new JLabel("Seleccione un cliente para iniciar un pedido");
    private final JButton btnPedido = FabricaDaisyUI.crearBotonPrimario("+ Nuevo Pedido", e -> abrirPedido());

    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private TableRowSorter<DefaultTableModel> clasificador;

    private Cliente clienteSeleccionado;

    public DashboardView() {
        super("Sistema de Ventas");
        this.clienteService = new ClienteService();
        this.pedidoService = new PedidoService();
        this.clienteSeleccionado = null;

        iniciarComponentes();
        cargarMetricas();
        cargarTabla();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 780);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(16, 16));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelSuperior = new JPanel(new BorderLayout(12, 12));
        panelSuperior.setBorder(new EmptyBorder(16, 24, 10, 24));
        panelSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("Punto de Venta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.putClientProperty("FlatLaf.style", "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");

        JPanel panelTemaSelector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panelTemaSelector.setOpaque(false);
        JLabel lblTemaEti = new JLabel("Tema:");
        lblTemaEti.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTemaEti.putClientProperty("FlatLaf.style", "[light]foreground: #475569; [dark]foreground: #f8f8f2");

        cmbTemas.setSelectedItem(TemaGestor.getTemaActual());
        cmbTemas.setPreferredSize(new Dimension(180, 34));
        FabricaDaisyUI.estilizarCampo(cmbTemas);
        cmbTemas.addActionListener(e -> {
            String seleccionado = (String) cmbTemas.getSelectedItem();
            if (seleccionado != null && !seleccionado.equals(TemaGestor.getTemaActual())) {
                TemaGestor.aplicarTema(seleccionado);
            }
        });
        panelTemaSelector.add(lblTemaEti);
        panelTemaSelector.add(cmbTemas);

        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(panelTemaSelector, BorderLayout.EAST);

        JPanel panelMetricas = new JPanel(new GridLayout(1, 3, 18, 0));
        panelMetricas.setBorder(new EmptyBorder(4, 24, 12, 24));
        panelMetricas.setOpaque(false);

        panelMetricas.add(FabricaDaisyUI.crearTarjetaEstadistica("Clientes", lblTotalClientes, new Color(16, 185, 129), new Color(80, 250, 123)));
        panelMetricas.add(FabricaDaisyUI.crearTarjetaEstadistica("Pedidos", lblTotalPedidos, new Color(99, 102, 241), new Color(139, 233, 253)));
        panelMetricas.add(FabricaDaisyUI.crearTarjetaEstadistica("Puntos", lblTotalPuntos, new Color(217, 119, 6), new Color(241, 250, 140)));

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);
        panelNorte.add(panelSuperior, BorderLayout.NORTH);
        panelNorte.add(panelMetricas, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setBorder(new EmptyBorder(2, 24, 8, 24));
        panelCentro.setOpaque(false);

        JPanel panelFiltro = new JPanel(new BorderLayout(12, 8));
        panelFiltro.setBorder(new EmptyBorder(0, 0, 10, 0));
        panelFiltro.setOpaque(false);

        JPanel panelBuscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBuscador.setOpaque(false);
        JLabel lblLupa = new JLabel("Buscar:");
        lblLupa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLupa.putClientProperty("FlatLaf.style", "[light]foreground: #475569; [dark]foreground: #f8f8f2");

        txtBuscar.setPreferredSize(new Dimension(360, 36));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarTabla();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTabla();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTabla();
            }
        });

        panelBuscador.add(lblLupa);
        panelBuscador.add(txtBuscar);

        lblEstadoSeleccion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstadoSeleccion.putClientProperty("FlatLaf.style", "[light]foreground: #64748b; [dark]foreground: #bd93f9");

        panelFiltro.add(panelBuscador, BorderLayout.WEST);
        panelFiltro.add(lblEstadoSeleccion, BorderLayout.EAST);
        panelCentro.add(panelFiltro, BorderLayout.NORTH);

        String[] columnas = {"ID", "DPI", "Nombre", "Dirección", "Teléfono", "Puntos acumulados", "Estado"};
        modeloTabla.setColumnIdentifiers(columnas);
        clasificador = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(clasificador);
        FabricaDaisyUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(6).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());

        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(50);
        colModel.getColumn(0).setMaxWidth(70);
        colModel.getColumn(1).setPreferredWidth(140);
        colModel.getColumn(2).setPreferredWidth(210);
        colModel.getColumn(3).setPreferredWidth(240);
        colModel.getColumn(4).setPreferredWidth(110);
        colModel.getColumn(5).setPreferredWidth(95);
        colModel.getColumn(6).setPreferredWidth(105);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarClienteDeFila();
            }
        });

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirPedido();
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> {
            cargarMetricas();
            cargarTabla();
        });

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton("Clientes", btnRefrescar, scrollTabla);

        panelCentro.add(tarjetaTabla, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 14));
        panelInferior.setOpaque(false);
        panelInferior.putClientProperty("FlatLaf.style", "[light]border: 1,0,0,0,#e2e8f0; [dark]border: 1,0,0,0,#44475a");

        btnPedido.setEnabled(false);
        JButton btnPagos = FabricaDaisyUI.crearBotonSecundario("Cobros y Facturación", e -> abrirPagos());
        JButton btnClientes = FabricaDaisyUI.crearBotonAcento("Clientes", e -> abrirAdministracionCliente());
        JButton btnCatalogos = FabricaDaisyUI.crearBotonNeutral("Administración", e -> abrirAdministracion());
        JButton btnSalir = FabricaDaisyUI.crearBotonPeligro("Salir", e -> System.exit(0));

        btnPedido.setPreferredSize(new Dimension(160, 38));
        btnPagos.setPreferredSize(new Dimension(195, 38));
        btnClientes.setPreferredSize(new Dimension(135, 38));
        btnCatalogos.setPreferredSize(new Dimension(135, 38));
        btnSalir.setPreferredSize(new Dimension(110, 38));

        panelInferior.add(btnPedido);
        panelInferior.add(btnPagos);
        panelInferior.add(btnClientes);
        panelInferior.add(btnCatalogos);
        panelInferior.add(btnSalir);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarMetricas() {
        try {
            List<Cliente> clientes = clienteService.listar();
            List<Pedidos> pedidos = pedidoService.listar();

            lblTotalClientes.setText(String.valueOf(clientes.size()));
            lblTotalPedidos.setText(String.valueOf(pedidos.size()));

            BigDecimal puntosTotales = BigDecimal.ZERO;
            for (Cliente c : clientes) {
                if (c.getSaldoPuntoCli() != null) {
                    puntosTotales = puntosTotales.add(c.getSaldoPuntoCli());
                }
            }
            lblTotalPuntos.setText(puntosTotales.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() + " pts");
        } catch (RuntimeException ex) {
            lblTotalClientes.setText("0");
            lblTotalPedidos.setText("0");
            lblTotalPuntos.setText("0 pts");
        }
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
                        c.getDireccionCli(),
                        c.getTelefonoCli(),
                        c.getSaldoPuntoCli(),
                        "A".equalsIgnoreCase(c.getEstadoCli()) ? "ACTIVO" : "INACTIVO"
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la lista de clientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarTabla() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            clasificador.setRowFilter(null);
        } else {
            clasificador.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
        }
    }

    private void seleccionarClienteDeFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            clienteSeleccionado = null;
            btnPedido.setEnabled(false);
            lblEstadoSeleccion.setText("Seleccione un cliente para iniciar un pedido");
            lblEstadoSeleccion.putClientProperty("FlatLaf.style", "[light]foreground: #64748b; [dark]foreground: #bd93f9");
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        int idCli = (int) modeloTabla.getValueAt(filaModelo, 0);
        clienteSeleccionado = clienteService.buscarClientePorId(idCli);

        if (clienteSeleccionado != null) {
            btnPedido.setEnabled(true);
            lblEstadoSeleccion.setText("✓ " + clienteSeleccionado.getNombreCli());
            lblEstadoSeleccion.putClientProperty("FlatLaf.style", "[light]foreground: #059669; [dark]foreground: #50fa7b");
        }
    }

    private void abrirPedido() {
        if (clienteSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un cliente de la tabla antes de iniciar un pedido.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PedidoView ventana = new PedidoView(this, clienteSeleccionado);
        ventana.setVisible(true);
    }

    private void abrirPagos() {
        new PagoView(this).setVisible(true);
    }

    private void abrirAdministracion() {
        new AdministracionView(this).setVisible(true);
    }

    private void abrirAdministracionCliente() {
        ClienteView ventana = new ClienteView(this);
        ventana.setVisible(true);
    }
}
