package vista;

import model.Cliente;
import model.Pedidos;
import service.CanjesService;
import service.ClienteService;
import service.PedidoService;
import util.FormatoTexto;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class DashboardView extends JFrame {

    private final ClienteService clienteService;
    private final PedidoService pedidoService;
    private final CanjesService canjesService;

    private final JLabel lblTotalClientes = new JLabel("0");
    private final JLabel lblTotalPedidos = new JLabel("0");
    private final JLabel lblTotalPuntos = new JLabel("0");
    private final JLabel lblTotalCanjes = new JLabel("0");

    private final JComboBox<String> cmbTemas = new JComboBox<>(TemaGestor.obtenerNombresTemas());
    private final JTextField txtDpiBusqueda = FabricaDaisyUI.crearCampoTexto("DPI...", 16);
    private final JButton btnBuscarCliente = FabricaDaisyUI.crearBotonPrimario("Buscar", Icons.search(16), e -> buscarClienteRapido());
    private final JButton btnLimpiarBusqueda = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), e -> limpiarBusquedaCliente());

    private final JPanel panelContenidoFicha = new JPanel(new BorderLayout(8, 8));
    private final JLabel lblNombreClienteFicha = new JLabel("Ningún cliente seleccionado");
    private final JLabel lblDpiClienteFicha = new JLabel("DPI: -");
    private final JLabel lblTelefonoClienteFicha = new JLabel("Tel: -");
    private final JLabel lblCorreoClienteFicha = new JLabel("Correo: -");
    private final JLabel lblDireccionClienteFicha = new JLabel("Dirección: -");
    private final JLabel lblSaldoPuntosFicha = new JLabel("Saldo: 0 pts");
    private final JLabel lblEstadoClienteFicha = new JLabel("ESTADO: -");
    private final JButton btnPedidoConCliente = FabricaDaisyUI.crearBotonPrimario("Nuevo Pedido", Icons.shoppingBag(16), e -> iniciarPedidoConClienteActual());
    private final JButton btnCanjeConCliente = FabricaDaisyUI.crearBotonSecundario("Canjear Puntos", Icons.gift(16), e -> iniciarCanjeConClienteActual());

    private Cliente clienteConsultado = null;

    private final DefaultTableModel modeloUltimosPedidos = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaUltimosPedidos = new JTable(modeloUltimosPedidos);
    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardView() {
        super("Pan, Puntos y Premios - Panel de Control");
        this.clienteService = new ClienteService();
        this.pedidoService = new PedidoService();
        this.canjesService = new CanjesService();

        iniciarComponentes();
        cargarMetricas();
        cargarUltimosPedidos();
        limpiarBusquedaCliente();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 840);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelSuperior = new JPanel(new BorderLayout(12, 10));
        panelSuperior.setBorder(new EmptyBorder(14, 24, 6, 24));
        panelSuperior.setOpaque(false);

        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTitulos.setOpaque(false);

        JLabel lblTitulo = new JLabel("PAN, PUNTOS Y PREMIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.putClientProperty("FlatLaf.style", "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión de Pedidos");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.putClientProperty("FlatLaf.style", "[light]foreground: #64748b; [dark]foreground: #bd93f9");

        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSubtitulo);

        JPanel panelSuperiorDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        panelSuperiorDerecha.setOpaque(false);

        JLabel lblTemaEti = new JLabel("Tema:");
        lblTemaEti.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTemaEti.putClientProperty("FlatLaf.style", "[light]foreground: #334155; [dark]foreground: #f8f8f2");
        cmbTemas.setSelectedItem(TemaGestor.getTemaActual());
        cmbTemas.setPreferredSize(new Dimension(160, 38));
        FabricaDaisyUI.estilizarCampo(cmbTemas);
        cmbTemas.addActionListener(e -> {
            String seleccionado = (String) cmbTemas.getSelectedItem();
            if (seleccionado != null && !seleccionado.equals(TemaGestor.getTemaActual())) {
                TemaGestor.aplicarTema(seleccionado);
                mostrarFichaCliente(clienteConsultado);
            }
        });

        JButton btnActualizarTodo = FabricaDaisyUI.crearBotonRefrescarIcono(e -> {
            cargarMetricas();
            cargarUltimosPedidos();
            if (clienteConsultado != null) {
                Cliente ref = clienteService.buscarClientePorId(clienteConsultado.getIdCli());
                mostrarFichaCliente(ref);
            }
        });

        panelSuperiorDerecha.add(lblTemaEti);
        panelSuperiorDerecha.add(cmbTemas);
        panelSuperiorDerecha.add(btnActualizarTodo);

        panelSuperior.add(panelTitulos, BorderLayout.WEST);
        panelSuperior.add(panelSuperiorDerecha, BorderLayout.EAST);

        JPanel panelMetricas = new JPanel(new GridLayout(1, 4, 16, 0));
        panelMetricas.setBorder(new EmptyBorder(4, 24, 10, 24));
        panelMetricas.setOpaque(false);

        panelMetricas.add(FabricaDaisyUI.crearTarjetaEstadistica("Clientes Registrados", lblTotalClientes, new Color(16, 185, 129), new Color(80, 250, 123)));
        panelMetricas.add(FabricaDaisyUI.crearTarjetaEstadistica("Total de Pedidos", lblTotalPedidos, new Color(99, 102, 241), new Color(139, 233, 253)));
        panelMetricas.add(FabricaDaisyUI.crearTarjetaEstadistica("Puntos en Circulación", lblTotalPuntos, new Color(217, 119, 6), new Color(241, 250, 140)));
        panelMetricas.add(FabricaDaisyUI.crearTarjetaEstadistica("Premios Canjeados", lblTotalCanjes, new Color(236, 72, 153), new Color(255, 121, 198)));

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);
        panelNorte.add(panelSuperior, BorderLayout.NORTH);
        panelNorte.add(panelMetricas, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 18, 0));
        panelCentro.setBorder(new EmptyBorder(2, 24, 6, 24));
        panelCentro.setOpaque(false);

        JPanel panelGrid6 = new JPanel(new GridLayout(2, 3, 10, 10));
        panelGrid6.setOpaque(false);
        panelGrid6.setPreferredSize(new Dimension(0, 110));

        JButton btnModuloPedido = FabricaDaisyUI.crearBotonPrimario("Nuevo Pedido", Icons.shoppingBag(18), e -> abrirPedido());
        JButton btnModuloCanje = FabricaDaisyUI.crearBotonSecundario("Canjear Puntos", Icons.gift(18), e -> abrirCanjes());
        JButton btnModuloPagos = FabricaDaisyUI.crearBotonAcento("Cobros y Pagos", Icons.creditCard(18), e -> abrirPagos());
        JButton btnModuloClientes = FabricaDaisyUI.crearBotonNeutral("Clientes", Icons.user(18), e -> abrirAdministracionCliente());
        JButton btnModuloInventario = FabricaDaisyUI.crearBotonSecundario("Inventario", Icons.refreshCw(18), e -> abrirInventario());
        JButton btnModuloReportes = FabricaDaisyUI.crearBotonAcento("Reportes", Icons.receipt(18), e -> abrirReportes());
        JButton btnModuloAdmin = FabricaDaisyUI.crearBotonNeutral("Administración General", Icons.utensils(18), e -> abrirAdministracion());

        btnModuloPedido.putClientProperty("Boton.radio", 999);
        btnModuloCanje.putClientProperty("Boton.radio", 999);
        btnModuloPagos.putClientProperty("Boton.radio", 999);
        btnModuloClientes.putClientProperty("Boton.radio", 999);
        btnModuloInventario.putClientProperty("Boton.radio", 999);
        btnModuloReportes.putClientProperty("Boton.radio", 999);
        btnModuloAdmin.putClientProperty("Boton.radio", 999);
        btnPedidoConCliente.putClientProperty("Boton.radio", 999);
        btnCanjeConCliente.putClientProperty("Boton.radio", 999);

        btnModuloPedido.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnModuloCanje.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnModuloPagos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnModuloClientes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnModuloInventario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnModuloReportes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnModuloAdmin.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnModuloAdmin.setPreferredSize(new Dimension(0, 38));

        panelGrid6.add(btnModuloPedido);
        panelGrid6.add(btnModuloCanje);
        panelGrid6.add(btnModuloPagos);
        panelGrid6.add(btnModuloClientes);
        panelGrid6.add(btnModuloInventario);
        panelGrid6.add(btnModuloReportes);

        JPanel panelContenedorBotones = new JPanel(new BorderLayout(0, 10));
        panelContenedorBotones.setOpaque(false);
        panelContenedorBotones.add(panelGrid6, BorderLayout.CENTER);
        panelContenedorBotones.add(btnModuloAdmin, BorderLayout.SOUTH);

        JPanel panelModulosAcciones = new JPanel(new GridBagLayout());
        panelModulosAcciones.setOpaque(false);
        GridBagConstraints gbcMod = new GridBagConstraints();
        gbcMod.gridx = 0;
        gbcMod.gridy = 0;
        gbcMod.weightx = 1.0;
        gbcMod.weighty = 0.0;
        gbcMod.fill = GridBagConstraints.HORIZONTAL;
        gbcMod.anchor = GridBagConstraints.NORTH;
        panelModulosAcciones.add(panelContenedorBotones, gbcMod);

        JPanel tarjetaModulos = FabricaDaisyUI.crearTarjetaSeccion("Módulos Principales", panelModulosAcciones);
        panelCentro.add(tarjetaModulos);

        JPanel panelBarraDpi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        panelBarraDpi.setOpaque(false);

        JLabel lblDpiEti = new JLabel("DPI:");
        lblDpiEti.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDpiEti.putClientProperty("FlatLaf.style", "[light]foreground: #334155; [dark]foreground: #f8f8f2");
        ((AbstractDocument) txtDpiBusqueda.getDocument()).setDocumentFilter(FormatoTexto.filtroDpi());
        txtDpiBusqueda.setPreferredSize(new Dimension(170, 36));
        txtDpiBusqueda.addActionListener(e -> buscarClienteRapido());

        btnBuscarCliente.setPreferredSize(new Dimension(110, 36));
        btnLimpiarBusqueda.setPreferredSize(new Dimension(110, 36));

        panelBarraDpi.add(lblDpiEti);
        panelBarraDpi.add(txtDpiBusqueda);
        panelBarraDpi.add(btnBuscarCliente);
        panelBarraDpi.add(btnLimpiarBusqueda);

        panelContenidoFicha.setOpaque(false);
        panelContenidoFicha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaGestor.esModoOscuro() ? new Color(68, 71, 90) : new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 8, 8, 8)
        ));

        JPanel panelContenedorCliente = new JPanel(new BorderLayout(0, 10));
        panelContenedorCliente.setOpaque(false);
        panelContenedorCliente.add(panelBarraDpi, BorderLayout.NORTH);
        panelContenedorCliente.add(panelContenidoFicha, BorderLayout.CENTER);

        JPanel panelConsultaCliente = new JPanel(new GridBagLayout());
        panelConsultaCliente.setOpaque(false);
        GridBagConstraints gbcCli = new GridBagConstraints();
        gbcCli.gridx = 0;
        gbcCli.gridy = 0;
        gbcCli.weightx = 1.0;
        gbcCli.weighty = 0.0;
        gbcCli.fill = GridBagConstraints.HORIZONTAL;
        gbcCli.anchor = GridBagConstraints.NORTH;
        panelConsultaCliente.add(panelContenedorCliente, gbcCli);

        JPanel tarjetaConsultaCliente = FabricaDaisyUI.crearTarjetaSeccion("Búsqueda de Cliente", panelConsultaCliente);
        panelCentro.add(tarjetaConsultaCliente);

        add(panelCentro, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout(8, 8));
        panelSur.setBorder(new EmptyBorder(4, 24, 14, 24));
        panelSur.setOpaque(false);

        String[] columnasPedidos = {"No. Pedido", "Cliente", "Total", "Puntos", "Estado", "Fecha"};
        modeloUltimosPedidos.setColumnIdentifiers(columnasPedidos);
        tablaUltimosPedidos.setModel(modeloUltimosPedidos);
        tablaUltimosPedidos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        FabricaDaisyUI.estilizarTabla(tablaUltimosPedidos);

        TableColumnModel colModel = tablaUltimosPedidos.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(80);
        colModel.getColumn(0).setMaxWidth(100);
        colModel.getColumn(1).setPreferredWidth(220);
        colModel.getColumn(2).setPreferredWidth(90);
        colModel.getColumn(3).setPreferredWidth(120);
        colModel.getColumn(4).setPreferredWidth(100);
        colModel.getColumn(5).setPreferredWidth(150);

        JScrollPane scrollUltimos = new JScrollPane(tablaUltimosPedidos);
        scrollUltimos.setPreferredSize(new Dimension(800, 160));
        scrollUltimos.setBorder(BorderFactory.createEmptyBorder());

        JPanel tarjetaUltimos = FabricaDaisyUI.crearTarjetaSeccion("Últimos Pedidos", scrollUltimos);
        panelSur.add(tarjetaUltimos, BorderLayout.CENTER);

        JPanel panelPie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        panelPie.setOpaque(false);
        JButton btnSalir = FabricaDaisyUI.crearBotonPeligro("Salir", Icons.logOut(16), e -> System.exit(0));
        btnSalir.putClientProperty("Boton.radio", 999);
        btnSalir.setPreferredSize(new Dimension(190, 36));
        panelPie.add(btnSalir);
        panelSur.add(panelPie, BorderLayout.SOUTH);

        add(panelSur, BorderLayout.SOUTH);
    }

    private void buscarClienteRapido() {
        String texto = txtDpiBusqueda.getText().trim();
        String soloDigitos = FormatoTexto.soloDigitos(texto);
        if (soloDigitos.isEmpty()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Por favor ingrese el número de DPI a consultar.");
            return;
        }
        Cliente c = clienteService.buscarPorDpi(soloDigitos);
        if (c == null) {
            boolean opcion = FabricaDaisyUI.mostrarConfirmacion(
                    this,
                    "Cliente No Encontrado",
                    "No se encontró ningún cliente con DPI: " + FormatoTexto.formatearDpi(soloDigitos) + "\n\n¿Desea registrar al nuevo cliente?"
            );
            if (opcion) {
                new ClienteView(this, soloDigitos).setVisible(true);
            }
            limpiarBusquedaCliente();
            return;
        }
        mostrarFichaCliente(c);
    }

    private void mostrarFichaCliente(Cliente c) {
        this.clienteConsultado = c;
        panelContenidoFicha.removeAll();
        if (c == null) {
            JLabel lblVacio = new JLabel("<html><center>Ingrese un DPI y presione <b>Buscar</b> para consultar la información del cliente.</center></html>", SwingConstants.CENTER);
            lblVacio.setForeground(Color.GRAY);
            lblVacio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            panelContenidoFicha.add(lblVacio, BorderLayout.CENTER);
            panelContenidoFicha.revalidate();
            panelContenidoFicha.repaint();
            return;
        }

        JPanel panelDatos = new JPanel(new GridLayout(3, 2, 12, 6));
        panelDatos.setOpaque(false);

        boolean oscuro = TemaGestor.esModoOscuro();
        Color fgPrincipal = oscuro ? new Color(248, 248, 242) : new Color(15, 23, 42);
        Color fgSecundario = oscuro ? new Color(203, 213, 225) : new Color(71, 85, 105);

        lblNombreClienteFicha.setText(c.getNombreCli());
        lblNombreClienteFicha.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNombreClienteFicha.setForeground(fgPrincipal);

        boolean activo = "A".equalsIgnoreCase(c.getEstadoCli());
        lblEstadoClienteFicha.setText(activo ? "CLIENTE ACTIVO" : "CLIENTE INACTIVO");
        lblEstadoClienteFicha.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstadoClienteFicha.setForeground(activo ? new Color(16, 185, 129) : new Color(239, 68, 68));

        int saldo = c.getSaldoPuntoCli() != null ? c.getSaldoPuntoCli().intValue() : 0;
        lblSaldoPuntosFicha.setText("Saldo: " + saldo + " pts");
        lblSaldoPuntosFicha.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSaldoPuntosFicha.setForeground(new Color(16, 185, 129));

        lblDpiClienteFicha.setText("DPI: " + (c.getDpiCli() != null ? FormatoTexto.formatearDpi(c.getDpiCli()) : "S/D"));
        lblDpiClienteFicha.setForeground(fgSecundario);

        lblTelefonoClienteFicha.setText("Tel: " + (c.getTelefonoCli() != null && !c.getTelefonoCli().trim().isEmpty() ? c.getTelefonoCli() : "S/T"));
        lblTelefonoClienteFicha.setForeground(fgSecundario);

        lblCorreoClienteFicha.setText("Correo: " + (c.getCorreoCli() != null ? c.getCorreoCli() : "-"));
        lblCorreoClienteFicha.setForeground(fgSecundario);

        panelDatos.add(lblNombreClienteFicha);
        panelDatos.add(lblSaldoPuntosFicha);
        panelDatos.add(lblDpiClienteFicha);
        panelDatos.add(lblEstadoClienteFicha);
        panelDatos.add(lblTelefonoClienteFicha);
        panelDatos.add(lblCorreoClienteFicha);

        JPanel panelBotonesFicha = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        panelBotonesFicha.setOpaque(false);
        btnPedidoConCliente.setPreferredSize(new Dimension(180, 36));
        btnCanjeConCliente.setPreferredSize(new Dimension(160, 36));

        btnPedidoConCliente.setEnabled(activo);
        btnCanjeConCliente.setEnabled(activo);

        panelBotonesFicha.add(btnPedidoConCliente);
        panelBotonesFicha.add(btnCanjeConCliente);

        panelContenidoFicha.add(panelDatos, BorderLayout.CENTER);
        panelContenidoFicha.add(panelBotonesFicha, BorderLayout.SOUTH);
        panelContenidoFicha.revalidate();
        panelContenidoFicha.repaint();
    }

    private void limpiarBusquedaCliente() {
        txtDpiBusqueda.setText("");
        mostrarFichaCliente(null);
    }

    private void iniciarPedidoConClienteActual() {
        if (clienteConsultado == null) {
            return;
        }
        PedidoView ventana = new PedidoView(this, clienteConsultado);
        ventana.setVisible(true);
    }

    private void iniciarCanjeConClienteActual() {
        if (clienteConsultado == null) {
            return;
        }
        CanjeView ventana = new CanjeView(this, clienteConsultado);
        ventana.setVisible(true);
    }

    private void cargarMetricas() {
        try {
            List<Cliente> clientes = clienteService.listar();
            List<Pedidos> pedidos = pedidoService.listar();
            int totalPuntos = 0;
            for (Cliente c : clientes) {
                if (c.getSaldoPuntoCli() != null) {
                    totalPuntos += c.getSaldoPuntoCli().intValue();
                }
            }
            int totalCanjes = canjesService.listar().size();

            lblTotalClientes.setText(String.valueOf(clientes.size()));
            lblTotalPedidos.setText(String.valueOf(pedidos.size()));
            lblTotalPuntos.setText(totalPuntos + " pts");
            lblTotalCanjes.setText(String.valueOf(totalCanjes));
        } catch (RuntimeException ex) {
            lblTotalClientes.setText("0");
            lblTotalPedidos.setText("0");
            lblTotalPuntos.setText("0 pts");
            lblTotalCanjes.setText("0");
        }
    }

    private void cargarUltimosPedidos() {
        modeloUltimosPedidos.setRowCount(0);
        try {
            List<Pedidos> pedidos = pedidoService.listar();
            Collections.reverse(pedidos);
            int limite = Math.min(pedidos.size(), 8);
            for (int i = 0; i < limite; i++) {
                Pedidos p = pedidos.get(i);
                Cliente c = clienteService.buscarClientePorId(p.getIdCliPed());
                String nombreCli = c != null ? c.getNombreCli() : "Cliente #" + p.getIdCliPed();
                String fechaTexto = p.getFechaPed() != null ? p.getFechaPed().format(formateadorFecha) : "";
                String estadoTexto = "P".equalsIgnoreCase(p.getEstadoPed()) ? "Pendiente" :
                        ("C".equalsIgnoreCase(p.getEstadoPed()) ? "Cobrado" :
                                ("A".equalsIgnoreCase(p.getEstadoPed()) ? "Anulado" : "Entregado"));

                modeloUltimosPedidos.addRow(new Object[]{
                        p.getIdPed(),
                        nombreCli,
                        "Q " + (p.getTotalPed() != null ? p.getTotalPed().toPlainString() : "0.00"),
                        p.getPuntosObtenidosPed() + " pts",
                        estadoTexto,
                        fechaTexto
                });
            }
        } catch (RuntimeException ex) {
        }
    }

    private void abrirPedido() {
        PedidoView ventana = new PedidoView(this, clienteConsultado);
        ventana.setVisible(true);
    }

    private void abrirCanjes() {
        CanjeView ventana = new CanjeView(this, clienteConsultado);
        ventana.setVisible(true);
    }

    private void abrirPagos() {
        new PagoView(this).setVisible(true);
    }

    private void abrirAdministracionCliente() {
        new ClienteView(this).setVisible(true);
    }

    private void abrirAdministracion() {
        new AdministracionView(this).setVisible(true);
    }

    private void abrirInventario() {
        new InventarioView(this).setVisible(true);
    }

    private void abrirReportes() {
        new ReportesView(this).setVisible(true);
    }
}
