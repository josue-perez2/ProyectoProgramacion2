package vista;

import model.Categorias;
import service.CategoriaService;
import vista.util.FabricaDaisyUI;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class CategoriaView extends JFrame {

    private final CategoriaService categoriaService;

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRegresar = new JButton("← Volver");

    private final Window parent;
    private Integer idCategoriaSeleccionada = null;

    private final JTextField txtNombre = new JTextField();

    private final JTable tabla = new JTable() {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(TemaGestor.esModoOscuro() ? new Color(98, 114, 164) : Color.GRAY);
                FontMetrics fm = g2.getFontMetrics();
                String mensaje = "Sin categorías registradas";
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

    public CategoriaView() {
        this(null);
    }

    public CategoriaView(Window parent) {
        super("Gestión de Categorías");
        this.parent = parent;
        this.categoriaService = new CategoriaService();
        iniciarComponentes();
        cargarTabla();
        actualizarEstadoBotones(false);
    }

    private void iniciarComponentes() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(880, 600);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(TemaGestor.esModoOscuro() ? new Color(40, 42, 54) : new Color(248, 250, 252));

        JPanel panelContenedorPrincipal = new JPanel(new BorderLayout(12, 12));
        panelContenedorPrincipal.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panelContenedorPrincipal.setOpaque(false);

        JPanel panelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panelCampos.setOpaque(false);

        txtNombre.setPreferredSize(new Dimension(380, 36));
        FabricaDaisyUI.estilizarCampo(txtNombre);

        panelCampos.add(FabricaDaisyUI.crearCampoConEtiqueta("Nombre de la Categoría:", txtNombre));

        JPanel tarjetaFormulario = FabricaDaisyUI.crearTarjetaSeccion(
                "Datos de la Categoría",
                panelCampos
        );

        panelContenedorPrincipal.add(tarjetaFormulario, BorderLayout.NORTH);

        String[] columnas = {"ID", "Nombre de la Categoría"};
        modeloTabla.setColumnIdentifiers(columnas);
        tabla.setModel(modeloTabla);
        FabricaDaisyUI.estilizarTabla(tabla);

        TableColumnModel colModel = tabla.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(50);
        colModel.getColumn(0).setMaxWidth(70);
        colModel.getColumn(1).setPreferredWidth(500);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JButton btnRefrescar = FabricaDaisyUI.crearBotonRefrescar(e -> cargarTabla());

        JPanel tarjetaTabla = FabricaDaisyUI.crearTarjetaSeccionConBoton(
                "Categorías Registradas",
                btnRefrescar,
                scrollTabla
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

        btnAgregar.addActionListener(e -> guardarCategoria());
        btnModificar.addActionListener(e -> actualizarCategoria());
        btnEliminar.addActionListener(e -> eliminarCategoria());
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

    private void actualizarEstadoBotones(boolean seleccionActiva) {
        btnAgregar.setEnabled(!seleccionActiva);
        btnModificar.setEnabled(seleccionActiva);
        btnEliminar.setEnabled(seleccionActiva);
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Categorias> categorias = categoriaService.listar();
        for (Categorias c : categorias) {
            modeloTabla.addRow(new Object[]{
                    c.getIdCat(),
                    c.getNombreCat()
            });
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            actualizarEstadoBotones(false);
            return;
        }
        idCategoriaSeleccionada = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        actualizarEstadoBotones(true);
    }

    private void guardarCategoria() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la categoría es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (Categorias c : categoriaService.listar()) {
            if (c.getNombreCat().equalsIgnoreCase(nombre)) {
                JOptionPane.showMessageDialog(this, "Ya existe una categoría con el nombre '" + nombre + "'.", "Categoría Duplicada", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        try {
            Categorias categoria = new Categorias();
            categoria.setNombreCat(nombre);

            categoriaService.insertar(categoria);
            JOptionPane.showMessageDialog(this, "Categoría guardada correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCategoria() {
        if (idCategoriaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la categoría es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (Categorias c : categoriaService.listar()) {
            if (c.getNombreCat().equalsIgnoreCase(nombre) && c.getIdCat() != idCategoriaSeleccionada) {
                JOptionPane.showMessageDialog(this, "Ya existe otra categoría con el nombre '" + nombre + "'.", "Categoría Duplicada", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        try {
            Categorias categoria = new Categorias();
            categoria.setIdCat(idCategoriaSeleccionada);
            categoria.setNombreCat(nombre);

            categoriaService.actualizar(categoria);
            JOptionPane.showMessageDialog(this, "Categoría actualizada correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarCategoria() {
        if (idCategoriaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea eliminar la categoría seleccionada?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                categoriaService.eliminar(idCategoriaSeleccionada);
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        idCategoriaSeleccionada = null;
        txtNombre.setText("");
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
