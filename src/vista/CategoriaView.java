package vista;

import model.Categorias;
import service.CategoriaService;
import vista.util.FabricaDaisyUI;
import vista.util.Icons;
import vista.util.TemaGestor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class CategoriaView extends JFrame {

    private final CategoriaService categoriaService;

    private final JButton btnAgregar = FabricaDaisyUI.crearBotonPrimario("Agregar", Icons.plus(16), null);
    private final JButton btnModificar = FabricaDaisyUI.crearBotonSecundario("Modificar", Icons.edit(16), null);
    private final JButton btnEliminar = FabricaDaisyUI.crearBotonPeligro("Eliminar", Icons.trash(16), null);
    private final JButton btnLimpiar = FabricaDaisyUI.crearBotonNeutral("Limpiar", Icons.broom(16), null);
    private final JButton btnRegresar = FabricaDaisyUI.crearBotonNeutral("Volver", Icons.arrowLeft(16), null);

    private final Window parent;
    private Integer idCategoriaSeleccionada = null;

    private final JTextField txtNombre = new JTextField();

    private final DefaultTableModel modeloTabla = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

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
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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

        btnAgregar.setIconTextGap(8);
        btnModificar.setIconTextGap(8);
        btnEliminar.setIconTextGap(8);
        btnLimpiar.setIconTextGap(8);
        btnRegresar.setIconTextGap(8);

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
            idCategoriaSeleccionada = null;
            txtNombre.setText("");
            actualizarEstadoBotones(false);
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        idCategoriaSeleccionada = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(filaModelo, 0)));
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(filaModelo, 1)));
        actualizarEstadoBotones(true);
    }

    private void guardarCategoria() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "El nombre de la categoría es obligatorio.");
            return;
        }
        for (Categorias c : categoriaService.listar()) {
            if (c.getNombreCat().equalsIgnoreCase(nombre)) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Categoría Duplicada", "Ya existe una categoría con el nombre '" + nombre + "'.");
                return;
            }
        }
        try {
            Categorias categoria = new Categorias();
            categoria.setNombreCat(nombre);

            categoriaService.insertar(categoria);
            FabricaDaisyUI.mostrarToastExito(this, "Categoría guardada correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al guardar: " + ex.getMessage());
        }
    }

    private void actualizarCategoria() {
        if (idCategoriaSeleccionada == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione una categoría de la tabla.");
            return;
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Validación", "El nombre de la categoría es obligatorio.");
            return;
        }
        for (Categorias c : categoriaService.listar()) {
            if (c.getNombreCat().equalsIgnoreCase(nombre) && c.getIdCat() != idCategoriaSeleccionada) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Categoría Duplicada", "Ya existe otra categoría con el nombre '" + nombre + "'.");
                return;
            }
        }
        try {
            Categorias categoria = new Categorias();
            categoria.setIdCat(idCategoriaSeleccionada);
            categoria.setNombreCat(nombre);

            categoriaService.actualizar(categoria);
            FabricaDaisyUI.mostrarToastExito(this, "Categoría actualizada correctamente.");
            cargarTabla();
            limpiarFormulario();
        } catch (RuntimeException ex) {
            FabricaDaisyUI.mostrarError(this, "Error", "Error al actualizar: " + ex.getMessage());
        }
    }

    private void eliminarCategoria() {
        if (idCategoriaSeleccionada == null) {
            FabricaDaisyUI.mostrarAdvertencia(this, "Aviso", "Seleccione una categoría de la tabla.");
            return;
        }
        boolean confirmar = FabricaDaisyUI.mostrarConfirmacion(this, "Confirmar Eliminación", "¿Desea eliminar la categoría seleccionada?");
        if (confirmar) {
            try {
                categoriaService.eliminar(idCategoriaSeleccionada);
                FabricaDaisyUI.mostrarToastExito(this, "Categoría eliminada correctamente.");
                cargarTabla();
                limpiarFormulario();
            } catch (RuntimeException ex) {
                FabricaDaisyUI.mostrarAdvertencia(this, "Operación Bloqueada", "No se puede eliminar la categoría porque contiene productos registrados. Reasigne o elimine primero los productos asociados.");
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
