package vista;

import model.Canjes;
import model.Cliente;
import model.Recompensas;
import service.CanjesService;
import service.ClienteService;
import service.RecompensaService;
import util.FormatoTexto;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CanjeView extends JFrame {

    private final CanjesService canjesService;
    private final ClienteService clienteService;
    private final RecompensaService recompensaService;

    private final Window parent;
    private Cliente clienteActual = null;

    private final JTextField txtDpiCliente = FabricaDaisyUI.crearCampoTexto("DPI", 13);
    private final JButton btnBuscarCliente = FabricaDaisyUI.crearBotonPrimario("Buscar", Icons.search(16), e -> buscarClientePorDpi());
    private final JButton btnLimpiarCliente = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), e -> limpiarCliente());

    private final JLabel lblNombreCliente = new JLabel("Ningún cliente seleccionado");
    private final JLabel lblDpiCliente = new JLabel("DPI: -");
    private final JLabel lblTelefonoCliente = new JLabel("Tel: -");
    private final JLabel lblSaldoPuntos = new JLabel("Saldo: 0 pts");
    private final JLabel lblEstadoCliente = new JLabel("ESTADO: -");
    private final JLabel lblResumenSeleccion = new JLabel("Seleccione una recompensa para canjear");

    private final JButton btnConfirmarCanje = FabricaDaisyUI.crearBotonPrimario("Confirmar Canje", Icons.gift(16), e -> confirmarCanje());
    private final JButton btnHistorialCanjes = FabricaDaisyUI.crearBotonSecundario("Ver Historial", Icons.eye(16), e -> verHistorialCanjes());
    private final JButton btnRegresar = FabricaDaisyUI.crearBotonNeutral("Volver", Icons.arrowLeft(16), e -> regresar());
    private final vista.util.GraficoPastelPuntos graficoPuntosCanje = new vista.util.GraficoPastelPuntos();

    private final List<Recompensas> listaRecompensasMemoria = new ArrayList<>();
    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DefaultTableModel modeloRecompensas = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tablaRecompensas = new JTable(modeloRecompensas);

    public CanjeView() {
        this(null, null);
    }

    public CanjeView(Window parent) {
        this(parent, null);
    }

    public CanjeView(Window parent, Cliente clienteInicial) {
        super("Canje de Recompensas");
        this.parent = parent;
        this.canjesService = new CanjesService();
        this.clienteService = new ClienteService();
        this.recompensaService = new RecompensaService();

        iniciarComponentes();

        if (clienteInicial != null) {
            asignarClienteActual(clienteInicial);
        } else {
            asignarClienteActual(null);
        }
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setBorder(BorderFactory.createEmptyBorder(14, 20, 6, 20));
        panelNorte.setOpaque(false);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBusqueda.setOpaque(false);
        JLabel lblEtiquetaDpi = new JLabel("DPI del Cliente:");
        lblEtiquetaDpi.setFont(new Font("Segoe UI", Font.BOLD, 13));

        ((AbstractDocument) txtDpiCliente.getDocument()).setDocumentFilter(FormatoTexto.filtroDpi());
        txtDpiCliente.setPreferredSize(new Dimension(200, 36));
        txtDpiCliente.addActionListener(e -> buscarClientePorDpi());

        btnBuscarCliente.setPreferredSize(new Dimension(110, 36));
        btnLimpiarCliente.setPreferredSize(new Dimension(110, 36));

        panelBusqueda.add(lblEtiquetaDpi);
        panelBusqueda.add(txtDpiCliente);
        panelBusqueda.add(btnBuscarCliente);
        panelBusqueda.add(btnLimpiarCliente);

        JPanel panelFichaCliente = new JPanel(new GridLayout(2, 3, 16, 6));
        panelFichaCliente.setOpaque(false);
        panelFichaCliente.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, TemaGestor.esModoOscuro() ? new Color(68, 71, 90) : new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 6, 10, 6)
        ));

        lblNombreCliente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDpiCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTelefonoCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblSaldoPuntos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSaldoPuntos.setForeground(new Color(16, 185, 129));

        lblEstadoCliente.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panelFichaCliente.add(lblNombreCliente);
        panelFichaCliente.add(lblSaldoPuntos);
        panelFichaCliente.add(lblEstadoCliente);
        panelFichaCliente.add(lblDpiCliente);
        panelFichaCliente.add(lblTelefonoCliente);

        JPanel panelClienteCompleto = new JPanel(new BorderLayout(8, 8));
        panelClienteCompleto.setOpaque(false);
        panelClienteCompleto.add(panelBusqueda, BorderLayout.NORTH);
        panelClienteCompleto.add(panelFichaCliente, BorderLayout.CENTER);

        JPanel tarjetaCliente = FabricaDaisyUI.crearTarjetaSeccion("Identificación del Cliente", panelClienteCompleto);
        panelNorte.add(tarjetaCliente, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        panelCentro.setOpaque(false);

        String[] columnas = {"ID", "Recompensa", "Tipo", "Puntos Requeridos", "Disponibilidad", "Estado de Canje"};
        modeloRecompensas.setColumnIdentifiers(columnas);
        tablaRecompensas.setModel(modeloRecompensas);
        FabricaDaisyUI.estilizarTabla(tablaRecompensas);

        TableColumnModel colModel = tablaRecompensas.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(50);
        colModel.getColumn(0).setMaxWidth(65);
        colModel.getColumn(1).setPreferredWidth(230);
        colModel.getColumn(2).setPreferredWidth(95);
        colModel.getColumn(3).setPreferredWidth(130);
        colModel.getColumn(4).setPreferredWidth(170);
        colModel.getColumn(5).setPreferredWidth(190);

        tablaRecompensas.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (value != null) {
                    String str = value.toString();
                    if (str.startsWith("CANJEABLE")) {
                        lbl.setForeground(new Color(16, 185, 129));
                    } else if (str.startsWith("PUNTOS")) {
                        lbl.setForeground(new Color(239, 68, 68));
                    } else {
                        lbl.setForeground(new Color(217, 119, 6));
                    }
                }
                return lbl;
            }
        });

        tablaRecompensas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoSeleccion();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaRecompensas);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> {
            if (clienteActual != null) {
                Cliente ref = clienteService.buscarClientePorId(clienteActual.getIdCli());
                if (ref != null) {
                    asignarClienteActual(ref);
                }
            }
            cargarRecompensas();
        });

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton("Catálogo de Recompensas", btnRefrescar, scrollTabla);
        panelCentro.add(tarjetaTabla, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout(8, 6));
        panelSur.setBorder(BorderFactory.createEmptyBorder(4, 20, 12, 20));
        panelSur.setOpaque(false);

        lblResumenSeleccion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResumenSeleccion.setHorizontalAlignment(SwingConstants.CENTER);
        lblResumenSeleccion.setForeground(new Color(99, 102, 241));
        lblResumenSeleccion.setVisible(false);

        graficoPuntosCanje.setPreferredSize(new Dimension(280, 95));
        graficoPuntosCanje.setVisible(false);

        JPanel panelCentroSur = new JPanel(new BorderLayout(4, 2));
        panelCentroSur.setOpaque(false);
        panelCentroSur.add(lblResumenSeleccion, BorderLayout.NORTH);
        panelCentroSur.add(graficoPuntosCanje, BorderLayout.CENTER);

        panelSur.add(panelCentroSur, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 4));
        panelBotones.setOpaque(false);

        btnConfirmarCanje.setPreferredSize(new Dimension(190, 38));
        btnHistorialCanjes.setPreferredSize(new Dimension(160, 38));
        btnRegresar.setPreferredSize(new Dimension(120, 38));

        btnConfirmarCanje.setEnabled(false);

        panelBotones.add(btnConfirmarCanje);
        panelBotones.add(btnHistorialCanjes);
        panelBotones.add(btnRegresar);

        panelSur.add(panelBotones, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);
    }

    private void buscarClientePorDpi() {
        String texto = txtDpiCliente.getText().trim();
        String soloDigitos = FormatoTexto.soloDigitos(texto);
        if (soloDigitos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese el número de DPI del cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
                    "El cliente " + c.getNombreCli() + " se encuentra INACTIVO.\nNo es posible canjear puntos para clientes inactivos.",
                    "Cliente Inactivo",
                    JOptionPane.ERROR_MESSAGE
            );
            asignarClienteActual(null);
            return;
        }
        asignarClienteActual(c);
    }

    private void limpiarCliente() {
        asignarClienteActual(null);
    }

    private void asignarClienteActual(Cliente c) {
        this.clienteActual = c;
        if (c != null) {
            txtDpiCliente.setText(c.getDpiCli() != null ? FormatoTexto.formatearDpi(c.getDpiCli()) : "");
            lblNombreCliente.setText("Cliente: " + c.getNombreCli());
            lblNombreCliente.setForeground(TemaGestor.esModoOscuro() ? new Color(248, 250, 252) : new Color(15, 23, 42));
            lblDpiCliente.setText("DPI: " + (c.getDpiCli() != null ? FormatoTexto.formatearDpi(c.getDpiCli()) : "S/D"));
            lblTelefonoCliente.setText("Tel: " + (c.getTelefonoCli() != null && !c.getTelefonoCli().trim().isEmpty() ? c.getTelefonoCli() : "S/T"));

            int saldo = c.getSaldoPuntoCli() != null ? c.getSaldoPuntoCli().intValue() : 0;
            lblSaldoPuntos.setText("Saldo: " + saldo + " pts");
            lblSaldoPuntos.setForeground(new Color(16, 185, 129));

            boolean activo = "A".equalsIgnoreCase(c.getEstadoCli());
            lblEstadoCliente.setText(activo ? "CLIENTE ACTIVO" : "CLIENTE INACTIVO");
            lblEstadoCliente.setForeground(activo ? new Color(16, 185, 129) : new Color(239, 68, 68));
        } else {
            txtDpiCliente.setText("");
            lblNombreCliente.setText("Ningún cliente seleccionado");
            lblNombreCliente.setForeground(Color.GRAY);
            lblDpiCliente.setText("DPI: -");
            lblTelefonoCliente.setText("Tel: -");
            lblSaldoPuntos.setText("Saldo: 0 pts");
            lblSaldoPuntos.setForeground(Color.GRAY);
            lblEstadoCliente.setText("ESTADO: -");
            lblEstadoCliente.setForeground(Color.GRAY);
            graficoPuntosCanje.limpiar();
        }
        cargarRecompensas();
        actualizarEstadoSeleccion();
    }

    private void cargarRecompensas() {
        modeloRecompensas.setRowCount(0);
        listaRecompensasMemoria.clear();
        try {
            List<Recompensas> todas = recompensaService.listar();
            java.util.Map<Integer, model.Productos> mapaProductos = canjesService.obtenerMapaProductos();
            int saldoCliente = (clienteActual != null && clienteActual.getSaldoPuntoCli() != null)
                    ? clienteActual.getSaldoPuntoCli().intValue() : 0;
            boolean clienteValido = clienteActual != null && "A".equalsIgnoreCase(clienteActual.getEstadoCli());

            for (Recompensas r : todas) {
                if ("A".equalsIgnoreCase(r.getActivoRec())) {
                    listaRecompensasMemoria.add(r);
                    String tipoDesc = "P".equalsIgnoreCase(r.getTipoItemRec()) ? "Producto" :
                            ("S".equalsIgnoreCase(r.getTipoItemRec()) ? "Sándwich" : "Menú");
                    CanjesService.InfoDisponibilidad info = canjesService.evaluarDisponibilidad(r, mapaProductos);
                    boolean stockOk = info.isDisponible();
                    boolean puntosOk = saldoCliente >= r.getPuntosRequeridosRec();

                    String estadoCanje;
                    if (!clienteValido) {
                        estadoCanje = " --- ";
                    } else if (!stockOk) {
                        estadoCanje = "SIN STOCK";
                    } else if (!puntosOk) {
                        estadoCanje = "PUNTOS INSUFICIENTES";
                    } else {
                        estadoCanje = "CANJEABLE";
                    }

                    modeloRecompensas.addRow(new Object[]{
                            r.getIdRec(),
                            r.getNombreRec(),
                            tipoDesc,
                            r.getPuntosRequeridosRec() + " pts",
                            info.getTexto(),
                            estadoCanje
                    });
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las recompensas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEstadoSeleccion() {
        int fila = tablaRecompensas.getSelectedRow();
        if (fila == -1) {
            btnConfirmarCanje.setEnabled(false);
            lblResumenSeleccion.setText("");
            lblResumenSeleccion.setVisible(false);
            graficoPuntosCanje.limpiar();
            graficoPuntosCanje.setVisible(false);
            return;
        }

        int filaModelo = tablaRecompensas.convertRowIndexToModel(fila);
        Recompensas recompensa = listaRecompensasMemoria.get(filaModelo);
        int puntosRequeridos = recompensa.getPuntosRequeridosRec();

        if (clienteActual == null) {
            btnConfirmarCanje.setEnabled(false);
            lblResumenSeleccion.setText("Ingrese o busque un cliente para canjear '" + recompensa.getNombreRec() + "'");
            lblResumenSeleccion.setForeground(new Color(239, 68, 68));
            lblResumenSeleccion.setVisible(true);
            graficoPuntosCanje.limpiar();
            graficoPuntosCanje.setVisible(false);
            return;
        }

        if (!"A".equalsIgnoreCase(clienteActual.getEstadoCli())) {
            btnConfirmarCanje.setEnabled(false);
            lblResumenSeleccion.setText("El cliente está inactivo. Canje bloqueado.");
            lblResumenSeleccion.setForeground(new Color(239, 68, 68));
            lblResumenSeleccion.setVisible(true);
            graficoPuntosCanje.limpiar();
            graficoPuntosCanje.setVisible(false);
            return;
        }

        int saldo = clienteActual.getSaldoPuntoCli() != null ? clienteActual.getSaldoPuntoCli().intValue() : 0;
        graficoPuntosCanje.configurarParaCanje(saldo, puntosRequeridos, recompensa.getNombreRec());
        graficoPuntosCanje.setVisible(true);

        boolean stockOk = canjesService.verificarDisponibilidad(recompensa);
        if (!stockOk) {
            btnConfirmarCanje.setEnabled(false);
            lblResumenSeleccion.setText("Sin existencias para entregar: " + recompensa.getNombreRec());
            lblResumenSeleccion.setForeground(new Color(239, 68, 68));
            lblResumenSeleccion.setVisible(true);
            return;
        }

        if (saldo < puntosRequeridos) {
            btnConfirmarCanje.setEnabled(false);
            int faltantes = puntosRequeridos - saldo;
            lblResumenSeleccion.setText("Puntos insuficientes (faltan " + faltantes + " pts)");
            lblResumenSeleccion.setForeground(new Color(239, 68, 68));
            lblResumenSeleccion.setVisible(true);
            return;
        }

        int saldoRestante = saldo - puntosRequeridos;
        btnConfirmarCanje.setEnabled(true);
        lblResumenSeleccion.setText("Saldo restante tras canje: " + saldoRestante + " pts");
        lblResumenSeleccion.setForeground(new Color(16, 185, 129));
        lblResumenSeleccion.setVisible(true);
    }

    private void confirmarCanje() {
        int fila = tablaRecompensas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una recompensa de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (clienteActual == null) {
            JOptionPane.showMessageDialog(this, "Debe ingresar y buscar el DPI del cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaRecompensas.convertRowIndexToModel(fila);
        Recompensas recompensa = listaRecompensasMemoria.get(filaModelo);
        int saldoActual = clienteActual.getSaldoPuntoCli() != null ? clienteActual.getSaldoPuntoCli().intValue() : 0;
        int costoPuntos = recompensa.getPuntosRequeridosRec();

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Desea confirmar el canje de la siguiente recompensa?\n\n" +
                        "Cliente: " + clienteActual.getNombreCli() + "\n" +
                        "Recompensa: " + recompensa.getNombreRec() + "\n" +
                        "Puntos a descontar: " + costoPuntos + " pts\n" +
                        "Saldo actual: " + saldoActual + " pts\n" +
                        "Saldo resultante: " + (saldoActual - costoPuntos) + " pts",
                "Confirmación de Canje",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Canjes canjeGenerado = canjesService.procesarCanje(clienteActual, recompensa);

            Cliente clienteActualizado = clienteService.buscarClientePorId(clienteActual.getIdCli());
            if (clienteActualizado != null) {
                asignarClienteActual(clienteActualizado);
            }

            mostrarComprobanteCanje(canjeGenerado, recompensa, saldoActual, clienteActualizado != null ? clienteActualizado.getSaldoPuntoCli().intValue() : (saldoActual - costoPuntos));

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo realizar el canje: " + ex.getMessage(), "Validación de Canje", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar el canje: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarComprobanteCanje(Canjes canje, Recompensas recompensa, int saldoAnterior, int saldoNuevo) {
        JDialog dialog = new JDialog(this, "Comprobante de Canje", true);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        String fechaTexto = canje.getFechaCan() != null ? canje.getFechaCan().format(formateadorFecha) : LocalDateTime.now().format(formateadorFecha);
        String dpiTexto = clienteActual.getDpiCli() != null ? FormatoTexto.formatearDpi(clienteActual.getDpiCli()) : "S/D";

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("              PAN, PUNTOS Y PREMIOS                  \n");
        sb.append("         COMPROBANTE DE CANJE DE PUNTOS              \n");
        sb.append("=====================================================\n\n");
        sb.append(String.format(" No. Canje:       #CAN-%05d\n", canje.getIdCan()));
        sb.append(String.format(" Fecha y Hora:    %s\n", fechaTexto));
        sb.append(String.format(" Cliente:         %s\n", clienteActual.getNombreCli()));
        sb.append(String.format(" DPI:             %s\n", dpiTexto));
        sb.append("-----------------------------------------------------\n");
        sb.append(String.format(" Recompensa:      %s\n", recompensa.getNombreRec()));
        sb.append(String.format(" Puntos Canjeados: -%d pts\n", recompensa.getPuntosRequeridosRec()));
        sb.append("-----------------------------------------------------\n");
        sb.append(String.format(" Saldo Anterior:  %d pts\n", saldoAnterior));
        sb.append(String.format(" Saldo Restante:  %d pts\n", saldoNuevo));
        sb.append("-----------------------------------------------------\n");
        sb.append("   ¡Felicidades por su recompensa!     \n");
        sb.append("=====================================================\n");

        JTextArea txtTicket = new JTextArea(sb.toString());
        txtTicket.setEditable(false);
        txtTicket.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtTicket.setBackground(TemaGestor.esModoOscuro() ? new Color(34, 37, 46) : new Color(255, 255, 255));
        txtTicket.setForeground(TemaGestor.esModoOscuro() ? new Color(248, 250, 252) : new Color(15, 23, 42));
        txtTicket.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(txtTicket);
        scroll.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 18));
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelAcciones.setOpaque(false);

        JButton btnAceptar = FabricaDaisyUI.crearBotonPrimario("Aceptar y Finalizar", Icons.check(16), e -> dialog.dispose());
        btnAceptar.setPreferredSize(new Dimension(170, 36));
        panelAcciones.add(btnAceptar);

        dialog.add(panelAcciones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void verHistorialCanjes() {
        JDialog dialog = new JDialog(this, "Historial de Canjes Realizados", true);
        dialog.setSize(820, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        DefaultTableModel modeloHistorial = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modeloHistorial.setColumnIdentifiers(new String[]{"ID Canje", "Cliente", "Recompensa", "Puntos Canjeados", "Fecha y Hora"});
        JTable tablaHistorial = new JTable(modeloHistorial);
        FabricaDaisyUI.estilizarTabla(tablaHistorial);

        TableColumnModel colModel = tablaHistorial.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(70);
        colModel.getColumn(0).setMaxWidth(90);
        colModel.getColumn(1).setPreferredWidth(190);
        colModel.getColumn(2).setPreferredWidth(210);
        colModel.getColumn(3).setPreferredWidth(110);
        colModel.getColumn(4).setPreferredWidth(160);

        try {
            List<Canjes> canjes = canjesService.listar();
            List<Recompensas> todasRec = recompensaService.listar();
            for (Canjes c : canjes) {
                Cliente cli = null;
                try {
                    cli = clienteService.buscarClientePorId(c.getIdCliCan());
                } catch (Exception ignored) {
                }
                String nomCli = cli != null ? cli.getNombreCli() : "Cliente #" + c.getIdCliCan();
                String nomRec = "Recompensa #" + c.getIdRecCan();
                for (Recompensas r : todasRec) {
                    if (r.getIdRec() == c.getIdRecCan()) {
                        nomRec = r.getNombreRec();
                        break;
                    }
                }
                String fechaTexto = c.getFechaCan() != null ? c.getFechaCan().format(formateadorFecha) : "";
                modeloHistorial.addRow(new Object[]{
                        c.getIdCan(),
                        nomCli,
                        nomRec,
                        "-" + c.getPuntosRecompensaCan() + " pts",
                        fechaTexto
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error al cargar historial: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scroll = new JScrollPane(tablaHistorial);
        scroll.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        panelSur.setOpaque(false);
        JButton btnCerrarHistorial = FabricaDaisyUI.crearBotonNeutral("Cerrar", Icons.x(16), e -> dialog.dispose());
        btnCerrarHistorial.setPreferredSize(new Dimension(100, 34));
        panelSur.add(btnCerrarHistorial);
        dialog.add(panelSur, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}
