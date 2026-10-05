package vista;

import model.Cliente;
import service.ClienteService;
import util.FormatoTexto;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

public class ClienteView extends JFrame {

    private final ClienteService clienteService;

    private final JButton btnAgregar = FabricaDaisyUI.crearBotonPrimario("Agregar", Icons.plus(16), null);
    private final JButton btnModificar = FabricaDaisyUI.crearBotonSecundario("Modificar", Icons.edit(16), null);
    private final JButton btnEliminar = FabricaDaisyUI.crearBotonPeligro("Eliminar", Icons.trash(16), null);
    private final JButton btnLimpiar = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), null);
    private final JButton btnRegresar = FabricaDaisyUI.crearBotonNeutral("Volver", Icons.arrowLeft(16), null);

    private final Window parent;
    private Integer idClienteSeleccionado = null;

    private final JTextField txtDpi = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtDireccion = new JTextField();
    private final JTextField txtSaldo = new JTextField();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});

    private final JTextField txtBuscar = FabricaDaisyUI.crearCampoTexto("Buscar por DPI o Nombre...", 25);
    private TableRowSorter<DefaultTableModel> clasificador;

    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public ClienteView() {
        this(null);
    }

    public ClienteView(Window parent) {
        this(parent, null);
    }

    public ClienteView(Window parent, String dpiInicial) {
        super("Gestión de Clientes");
        this.parent = parent;
        this.clienteService = new ClienteService();
        iniciarComponentes();
        cargarTabla();
        if (dpiInicial != null && !dpiInicial.trim().isEmpty()) {
            txtDpi.setText(dpiInicial.trim());
            txtBuscar.setText(dpiInicial.trim());
            filtrarTabla();
        }
        actualizarEstadoBotones(false);
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1140, 740);
        setMinimumSize(new Dimension(1060, 660));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedorPrincipal = new JPanel(new BorderLayout(12, 12));
        panelContenedorPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        panelContenedorPrincipal.setOpaque(false);

        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 14, 10));
        panelCampos.setOpaque(false);

        txtDpi.setPreferredSize(new Dimension(150, 36));
        ((AbstractDocument) txtDpi.getDocument()).setDocumentFilter(FormatoTexto.filtroDpi());
        txtTelefono.setPreferredSize(new Dimension(150, 36));
        ((AbstractDocument) txtTelefono.getDocument()).setDocumentFilter(FormatoTexto.filtroTelefono());

        txtSaldo.setText("0");
        txtSaldo.setPreferredSize(new Dimension(140, 36));
        FabricaDaisyUI.aplicarCampoEstatico(txtSaldo);

        txtNombre.setPreferredSize(new Dimension(200, 36));
        txtCorreo.setPreferredSize(new Dimension(200, 36));
        txtDireccion.setPreferredSize(new Dimension(200, 36));
        cmbEstado.setPreferredSize(new Dimension(150, 36));

        FabricaDaisyUI.estilizarCampo(txtDpi);
        FabricaDaisyUI.estilizarCampo(txtTelefono);
        FabricaDaisyUI.estilizarCampo(txtNombre);
        FabricaDaisyUI.estilizarCampo(txtCorreo);
        FabricaDaisyUI.estilizarCampo(txtDireccion);
        FabricaDaisyUI.estilizarCampo(cmbEstado);

        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("DPI:", txtDpi));
        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Nombre Completo:", txtNombre));
        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Teléfono:", txtTelefono));
        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Estado:", cmbEstado));
        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Correo Electrónico:", txtCorreo));
        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Dirección de Entrega:", txtDireccion));
        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Puntos:", txtSaldo));

        JPanel tarjetaFormulario = FabricaDaisyUI.crearTarjetaSeccion(
                "Datos del Cliente",
                panelCampos
        );
        panelContenedorPrincipal.add(tarjetaFormulario, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(8, 8));
        panelCentro.setOpaque(false);

        JPanel panelBarraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBarraBusqueda.setOpaque(false);
        JLabel lblBuscar = new JLabel("Buscar Cliente:");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtBuscar.setPreferredSize(new Dimension(320, 34));
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

        JButton btnLimpiarBusqueda = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(14), e -> {
            txtBuscar.setText("");
            filtrarTabla();
        });
        btnLimpiarBusqueda.setPreferredSize(new Dimension(120, 34));

        panelBarraBusqueda.add(lblBuscar);
        panelBarraBusqueda.add(txtBuscar);
        panelBarraBusqueda.add(btnLimpiarBusqueda);

        String[] columnas = {"ID", "DPI", "Nombre", "Teléfono", "Correo", "Dirección", "Puntos", "Estado"};
        modeloTabla.setColumnIdentifiers(columnas);
        clasificador = new TableRowSorter<>(modeloTabla);
        tabla.setModel(modeloTabla);
        tabla.setRowSorter(clasificador);
        FabricaDaisyUI.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(7).setCellRenderer(new FabricaDaisyUI.RenderizadorInsigniaEstado());

        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(45);
        colModel.getColumn(0).setMaxWidth(60);
        colModel.getColumn(1).setPreferredWidth(130);
        colModel.getColumn(2).setPreferredWidth(180);
        colModel.getColumn(3).setPreferredWidth(110);
        colModel.getColumn(4).setPreferredWidth(160);
        colModel.getColumn(5).setPreferredWidth(180);
        colModel.getColumn(6).setPreferredWidth(95);
        colModel.getColumn(7).setPreferredWidth(95);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> cargarTabla());

        JPanel panelContenidoTabla = new JPanel(new BorderLayout(8, 8));
        panelContenidoTabla.setOpaque(false);
        panelContenidoTabla.add(panelBarraBusqueda, BorderLayout.NORTH);
        panelContenidoTabla.add(scrollTabla, BorderLayout.CENTER);

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Clientes Registrados",
                btnRefrescar,
                panelContenidoTabla
        );
        panelContenedorPrincipal.add(tarjetaTabla, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(4, 16, 14, 16));
        panelBotones.setOpaque(false);

        btnAgregar.setPreferredSize(new Dimension(135, 38));
        btnModificar.setPreferredSize(new Dimension(135, 38));
        btnEliminar.setPreferredSize(new Dimension(135, 38));
        btnLimpiar.setPreferredSize(new Dimension(130, 38));
        btnRegresar.setPreferredSize(new Dimension(130, 38));

        FabricaDaisyUI.aplicarBotonPrimario(btnAgregar);
        FabricaDaisyUI.aplicarBotonSecundario(btnModificar);
        FabricaDaisyUI.aplicarBotonPeligro(btnEliminar);
        FabricaDaisyUI.aplicarBotonNeutral(btnLimpiar);
        FabricaDaisyUI.aplicarBotonNeutral(btnRegresar);

        btnAgregar.setIconTextGap(8);
        btnModificar.setIconTextGap(8);
        btnEliminar.setIconTextGap(8);
        btnLimpiar.setIconTextGap(8);
        btnRegresar.setIconTextGap(8);

        btnAgregar.addActionListener(e -> guardarCliente());
        btnModificar.addActionListener(e -> actualizarCliente());
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRegresar.addActionListener(e -> regresar());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRegresar);

        add(panelContenedorPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void filtrarTabla() {
        if (clasificador == null) {
            return;
        }
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            clasificador.setRowFilter(null);
            return;
        }
        String digitos = FormatoTexto.soloDigitos(texto);
        clasificador.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String dpi = entry.getStringValue(1);
                String nombre = entry.getStringValue(2);
                String telefono = entry.getStringValue(3);
                String correo = entry.getStringValue(4);

                if (!digitos.isEmpty()) {
                    String dpiDigitos = FormatoTexto.soloDigitos(dpi);
                    if (dpiDigitos != null && dpiDigitos.contains(digitos)) {
                        return true;
                    }
                }
                String q = texto.toLowerCase();
                return dpi.toLowerCase().contains(q) ||
                        nombre.toLowerCase().contains(q) ||
                        telefono.toLowerCase().contains(q) ||
                        correo.toLowerCase().contains(q);
            }
        });
    }

    private void actualizarEstadoBotones(boolean seleccionActiva) {
        btnAgregar.setEnabled(!seleccionActiva);
        btnModificar.setEnabled(seleccionActiva);
        btnEliminar.setEnabled(seleccionActiva);
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Cliente> clientes = clienteService.listar();
        for (Cliente c : clientes) {
            modeloTabla.addRow(new Object[]{
                    c.getIdCli(),
                    c.getDpiCli(),
                    c.getNombreCli(),
                    c.getTelefonoCli(),
                    c.getCorreoCli(),
                    c.getDireccionCli(),
                    c.getSaldoPuntoCli(),
                    "A".equalsIgnoreCase(c.getEstadoCli()) ? "ACTIVO" : "INACTIVO"
            });
        }
        filtrarTabla();
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            idClienteSeleccionado = null;
            limpiarCamposSinDeseleccionar();
            actualizarEstadoBotones(false);
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        idClienteSeleccionado = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(filaModelo, 0)));
        txtDpi.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 1)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 2)));
        txtTelefono.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 3)));
        txtCorreo.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 4)));
        txtDireccion.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 5)));
        txtSaldo.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 6)));
        String estadoFila = String.valueOf(modeloTabla.getValueAt(filaModelo, 7));
        cmbEstado.setSelectedItem("ACTIVO".equalsIgnoreCase(estadoFila) ? "Activo" : "Inactivo");
        actualizarEstadoBotones(true);
    }

    private void guardarCliente() {
        if (!validarFormulario()) {
            return;
        }
        String dpi = txtDpi.getText().trim();
        for (Cliente c : clienteService.listar()) {
            if (c.getDpiCli() != null && c.getDpiCli().equalsIgnoreCase(dpi)) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe un cliente registrado con el DPI '" + dpi + "'. Use el botón 'Modificar' para actualizar el registro existente.",
                        "DPI Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        try {
            Cliente cliente = new Cliente();
            cliente.setDpiCli(dpi);
            cliente.setNombreCli(txtNombre.getText().trim());
            cliente.setTelefonoCli(txtTelefono.getText().trim());
            cliente.setCorreoCli(txtCorreo.getText().trim());
            cliente.setDireccionCli(txtDireccion.getText().trim());
            cliente.setSaldoPuntoCli(leerSaldo());
            cliente.setEstadoCli("Activo".equals(cmbEstado.getSelectedItem()) ? "A" : "I");

            clienteService.insertar(cliente);
            JOptionPane.showMessageDialog(this, "Cliente guardado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El saldo debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCliente() {
        if (idClienteSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        String dpi = txtDpi.getText().trim();
        for (Cliente c : clienteService.listar()) {
            if (c.getDpiCli() != null && c.getDpiCli().equalsIgnoreCase(dpi) && c.getIdCli() != idClienteSeleccionado) {
                JOptionPane.showMessageDialog(this,
                        "Ya existe otro cliente registrado con el DPI '" + dpi + "'. Ingrese un DPI único.",
                        "DPI Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        try {
            Cliente cliente = new Cliente();
            cliente.setIdCli(idClienteSeleccionado);
            cliente.setDpiCli(dpi);
            cliente.setNombreCli(txtNombre.getText().trim());
            cliente.setTelefonoCli(txtTelefono.getText().trim());
            cliente.setCorreoCli(txtCorreo.getText().trim());
            cliente.setDireccionCli(txtDireccion.getText().trim());
            cliente.setSaldoPuntoCli(leerSaldo());
            cliente.setEstadoCli("Activo".equals(cmbEstado.getSelectedItem()) ? "A" : "I");

            clienteService.actualizar(cliente);
            JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El saldo debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCliente() {
        if (idClienteSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar o inactivar este cliente?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                clienteService.eliminar(idClienteSeleccionado);
                JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente.");
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                try {
                    clienteService.integridad(idClienteSeleccionado);
                    JOptionPane.showMessageDialog(this, "El cliente tiene pedidos o movimientos registrados, por lo que fue marcado como INACTIVO para preservar el historial.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    cargarTabla();
                    limpiarFormulario();
                } catch (RuntimeException exInactivar) {
                    JOptionPane.showMessageDialog(this, "No se pudo procesar la solicitud: " + exInactivar.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private boolean validarFormulario() {
        String dpi = txtDpi.getText().trim();
        if (FormatoTexto.soloDigitos(dpi).length() != 13) {
            JOptionPane.showMessageDialog(this, "El DPI debe tener 13 dígitos (ej. 1234 56789 0101).", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del cliente es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String telefono = txtTelefono.getText().trim();
        if (FormatoTexto.soloDigitosTelefono(telefono).length() != 8) {
            JOptionPane.showMessageDialog(this, "El teléfono debe tener 8 dígitos (ej. +502 9999-9999).", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String correo = txtCorreo.getText().trim();
        if (!PATRON_CORREO.matcher(correo).matches()) {
            JOptionPane.showMessageDialog(this, "El correo no es válido (ej. cliente@correo.com).", "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private BigDecimal leerSaldo() {
        String saldo = txtSaldo.getText().trim();
        if (saldo.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(saldo);
    }

    private void limpiarCamposSinDeseleccionar() {
        txtDpi.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        txtSaldo.setText("0");
        cmbEstado.setSelectedItem("Activo");
    }

    private void limpiarFormulario() {
        idClienteSeleccionado = null;
        limpiarCamposSinDeseleccionar();
        tabla.clearSelection();
        actualizarEstadoBotones(false);
    }

    private void regresar() {
        dispose();
        if (parent != null && parent.isDisplayable()) {
            parent.toFront();
        }
    }
}