package vista;

import model.Cliente;
import service.ClienteService;
import util.FormatoTexto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

public class ClienteView extends JFrame {

    private final ClienteService clienteService;

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    private final JTextField txtId = new JTextField();
    private final JTextField txtDpi = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtDireccion = new JTextField();
    private final JTextField txtSaldo = new JTextField();
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"A", "I"});

    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

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

    public ClienteView() {
        super("Gestión de Clientes");
        this.clienteService = new ClienteService();
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(0, 3, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        txtId.setEditable(false);
        txtId.setPreferredSize(new Dimension(200, 25));

        txtDpi.setPreferredSize(new Dimension(200, 25));
        ((AbstractDocument) txtDpi.getDocument()).setDocumentFilter(FormatoTexto.filtroDpi());
        txtTelefono.setPreferredSize(new Dimension(200, 25));
        ((AbstractDocument) txtTelefono.getDocument()).setDocumentFilter(FormatoTexto.filtroTelefono());
        txtSaldo.setText("0");
        txtSaldo.setEditable(false);

        panelFormulario.add(crearCampo("ID:", txtId));
        panelFormulario.add(crearCampo("DPI:", txtDpi));
        panelFormulario.add(crearCampo("Nombre:", txtNombre));
        panelFormulario.add(crearCampo("Teléfono:", txtTelefono));
        panelFormulario.add(crearCampo("Correo:", txtCorreo));
        panelFormulario.add(crearCampo("Dirección:", txtDireccion));
        panelFormulario.add(crearCampo("Saldo Puntos:", txtSaldo));
        panelFormulario.add(crearCampo("Estado:", cmbEstado));

        add(panelFormulario, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAgregar.setPreferredSize(new Dimension(110, 30));
        btnModificar.setPreferredSize(new Dimension(110, 30));
        btnEliminar.setPreferredSize(new Dimension(110, 30));
        btnLimpiar.setPreferredSize(new Dimension(110, 30));

        btnAgregar.addActionListener(e -> guardarCliente());
        btnModificar.addActionListener(e -> actualizarCliente());
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Listado de Clientes"));

        String[] columnas = {"ID", "DPI", "Nombre", "Teléfono", "Correo", "Dirección", "Saldo Puntos", "Estado"};
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

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        panelCentral.add(panelBotones, BorderLayout.NORTH);
        panelCentral.add(panelTabla, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);
    }

    private JPanel crearCampo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
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
                    c.getEstadoCli()
            });
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
        txtTelefono.setText(String.valueOf(modeloTabla.getValueAt(fila, 3)));
        txtCorreo.setText(String.valueOf(modeloTabla.getValueAt(fila, 4)));
        txtDireccion.setText(String.valueOf(modeloTabla.getValueAt(fila, 5)));
        txtSaldo.setText(String.valueOf(modeloTabla.getValueAt(fila, 6)));
        cmbEstado.setSelectedItem(String.valueOf(modeloTabla.getValueAt(fila, 7)));
    }

    private void guardarCliente() {
        if (!validarFormulario()) {
            return;
        }
        try {
            Cliente cliente = new Cliente();
            cliente.setDpiCli(txtDpi.getText().trim());
            cliente.setNombreCli(txtNombre.getText().trim());
            cliente.setTelefonoCli(txtTelefono.getText().trim());
            cliente.setCorreoCli(txtCorreo.getText().trim());
            cliente.setDireccionCli(txtDireccion.getText().trim());
            cliente.setSaldoPuntoCli(leerSaldo());
            cliente.setEstadoCli((String) cmbEstado.getSelectedItem());

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
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormulario()) {
            return;
        }
        try {
            Cliente cliente = new Cliente();
            cliente.setIdCli(Integer.parseInt(txtId.getText()));
            cliente.setDpiCli(txtDpi.getText().trim());
            cliente.setNombreCli(txtNombre.getText().trim());
            cliente.setTelefonoCli(txtTelefono.getText().trim());
            cliente.setCorreoCli(txtCorreo.getText().trim());
            cliente.setDireccionCli(txtDireccion.getText().trim());
            cliente.setSaldoPuntoCli(leerSaldo());
            cliente.setEstadoCli((String) cmbEstado.getSelectedItem());

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
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar físicamente el cliente seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                clienteService.eliminar(Integer.parseInt(txtId.getText()));
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private void limpiarFormulario() {
        txtId.setText("");
        txtDpi.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        txtSaldo.setText("0");
        cmbEstado.setSelectedItem("A");
        tabla.clearSelection();
    }
}