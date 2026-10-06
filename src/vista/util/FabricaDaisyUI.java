package vista.util;

import com.formdev.flatlaf.ui.FlatLineBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;

public class FabricaDaisyUI {

    public static final String PROPIEDAD_ESTILO = "FlatLaf.style";
    public static final String PROPIEDAD_PLACEHOLDER = "JTextField.placeholderText";
    public static final String PROPIEDAD_LIMPIAR = "JTextField.showClearButton";

    public static final String ESTILO_BOTON_PRIMARIO = "arc: 10; background: #10b981; foreground: #ffffff; hoverBackground: #059669; font: bold 12; margin: 6,18,6,18; border: 1,1,1,1,#059669,,10";
    public static final String ESTILO_BOTON_SECUNDARIO = "arc: 10; background: #6366f1; foreground: #ffffff; hoverBackground: #4f46e5; font: bold 12; margin: 6,18,6,18; border: 1,1,1,1,#4f46e5,,10";
    public static final String ESTILO_BOTON_ACENTO = "arc: 10; background: #f59e0b; foreground: #ffffff; hoverBackground: #d97706; font: bold 12; margin: 6,18,6,18; border: 1,1,1,1,#d97706,,10";
    public static final String ESTILO_BOTON_PELIGRO = "arc: 10; background: #ef4444; foreground: #ffffff; hoverBackground: #dc2626; font: bold 12; margin: 6,18,6,18; border: 1,1,1,1,#dc2626,,10";
    public static final String ESTILO_BOTON_NEUTRAL = "arc: 10; font: bold 12; margin: 6,18,6,18; [light]background: #ffffff; [light]foreground: #0f172a; [light]border: 1,1,1,1,#cbd5e1,,10; [light]hoverBackground: #f8fafc; [dark]background: #343746; [dark]foreground: #f8f8f2; [dark]border: 1,1,1,1,#6272a4,,10; [dark]hoverBackground: #44475a";
    public static final String ESTILO_BOTON_FANTASMA = "arc: 10; font: bold 12; margin: 6,18,6,18; background: #00000000; [light]foreground: #334155; [light]hoverBackground: #e2e8f0; [dark]foreground: #f8f8f2; [dark]hoverBackground: #44475a";
    public static final String ESTILO_BOTON_TURQUESA = "arc: 10; background: #0d9488; foreground: #ffffff; hoverBackground: #0f766e; font: bold 12; margin: 6,18,6,18; border: 1,1,1,1,#0f766e,,10";

    public enum TipoBoton { PRIMARIO, SECUNDARIO, ACENTO, PELIGRO, NEUTRAL, TURQUESA }

    public static class BotonElevado extends JButton {
        private final TipoBoton tipo;

        public BotonElevado(String texto, Icon icono, TipoBoton tipo) {
            super(texto, icono);
            this.tipo = tipo;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (icono != null) {
                setIconTextGap(8);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                boolean oscuro = TemaGestor.esModoOscuro();
                boolean hover = getModel().isRollover();
                boolean pressed = getModel().isPressed();
                boolean enabled = isEnabled();

                int x = 2;
                int y = pressed ? 3 : 1;
                int w = getWidth() - 4;
                int h = getHeight() - 5;
                if (w <= 0 || h <= 0) return;
                int radio = 10;
                Object propRadio = getClientProperty("Boton.radio");
                if (propRadio instanceof Integer) {
                    int rVal = (Integer) propRadio;
                    radio = (rVal >= 999) ? h : rVal;
                }

                if (enabled) {
                    int a1 = hover ? 32 : 18;
                    int a2 = hover ? 20 : 10;
                    g2.setColor(new Color(0, 0, 0, a2));
                    g2.fillRoundRect(x, y + 3, w, h, radio, radio);
                    g2.setColor(new Color(0, 0, 0, a1));
                    g2.fillRoundRect(x, y + 1, w, h, radio, radio);
                }

                Color bg;
                Color border;
                Color fg;

                switch (tipo) {
                    case PRIMARIO:
                        bg = hover ? new Color(5, 150, 105) : new Color(16, 185, 129);
                        border = hover ? new Color(4, 120, 87) : new Color(5, 150, 105);
                        fg = Color.WHITE;
                        break;
                    case TURQUESA:
                        bg = hover ? new Color(15, 118, 110) : new Color(13, 148, 136);
                        border = hover ? new Color(17, 94, 89) : new Color(15, 118, 110);
                        fg = Color.WHITE;
                        break;
                    case SECUNDARIO:
                        bg = hover ? new Color(79, 70, 229) : new Color(99, 102, 241);
                        border = hover ? new Color(67, 56, 202) : new Color(79, 70, 229);
                        fg = Color.WHITE;
                        break;
                    case ACENTO:
                        bg = hover ? new Color(217, 119, 6) : new Color(245, 158, 11);
                        border = hover ? new Color(180, 83, 9) : new Color(217, 119, 6);
                        fg = Color.WHITE;
                        break;
                    case PELIGRO:
                        bg = hover ? new Color(220, 38, 38) : new Color(239, 68, 68);
                        border = hover ? new Color(185, 28, 28) : new Color(220, 38, 38);
                        fg = Color.WHITE;
                        break;
                    case NEUTRAL:
                    default:
                        if (oscuro) {
                            bg = hover ? new Color(68, 71, 90) : new Color(44, 47, 60);
                            border = hover ? new Color(139, 233, 253) : new Color(98, 114, 164);
                            fg = new Color(248, 248, 242);
                        } else {
                            bg = hover ? new Color(248, 250, 252) : Color.WHITE;
                            border = hover ? new Color(148, 163, 184) : new Color(203, 213, 225);
                            fg = new Color(15, 23, 42);
                        }
                        break;
                }

                if (!enabled) {
                    bg = oscuro ? new Color(50, 52, 65) : new Color(241, 245, 249);
                    border = oscuro ? new Color(68, 71, 90) : new Color(226, 232, 240);
                    fg = oscuro ? new Color(120, 125, 145) : new Color(148, 163, 184);
                }

                g2.setColor(bg);
                g2.fillRoundRect(x, y, w, h, radio, radio);

                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(x, y, w, h, radio, radio);

                setForeground(fg);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    public static void aplicarBotonPrimario(JButton boton) {
        if (boton instanceof BotonElevado) {
            return;
        }
        boton.putClientProperty(PROPIEDAD_ESTILO, ESTILO_BOTON_PRIMARIO);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarBotonSecundario(JButton boton) {
        if (boton instanceof BotonElevado) {
            return;
        }
        boton.putClientProperty(PROPIEDAD_ESTILO, ESTILO_BOTON_SECUNDARIO);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarBotonAcento(JButton boton) {
        if (boton instanceof BotonElevado) {
            return;
        }
        boton.putClientProperty(PROPIEDAD_ESTILO, ESTILO_BOTON_ACENTO);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarBotonPeligro(JButton boton) {
        if (boton instanceof BotonElevado) {
            return;
        }
        boton.putClientProperty(PROPIEDAD_ESTILO, ESTILO_BOTON_PELIGRO);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarBotonNeutral(JButton boton) {
        if (boton instanceof BotonElevado) {
            return;
        }
        boton.putClientProperty(PROPIEDAD_ESTILO, ESTILO_BOTON_NEUTRAL);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarBotonFantasma(JButton boton) {
        boton.putClientProperty(PROPIEDAD_ESTILO, ESTILO_BOTON_FANTASMA);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarBotonTurquesa(JButton boton) {
        if (boton instanceof BotonElevado) {
            return;
        }
        boton.putClientProperty(PROPIEDAD_ESTILO, ESTILO_BOTON_TURQUESA);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static JButton crearBotonTurquesa(String texto, ActionListener accion) {
        return crearBotonTurquesa(texto, null, accion);
    }

    public static JButton crearBotonTurquesa(String texto, Icon icono, ActionListener accion) {
        BotonElevado boton = new BotonElevado(texto, icono, TipoBoton.TURQUESA);
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonPrimario(String texto, ActionListener accion) {
        return crearBotonPrimario(texto, null, accion);
    }

    public static JButton crearBotonPrimario(String texto, Icon icono, ActionListener accion) {
        BotonElevado boton = new BotonElevado(texto, icono, TipoBoton.PRIMARIO);
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonSecundario(String texto, ActionListener accion) {
        return crearBotonSecundario(texto, null, accion);
    }

    public static JButton crearBotonSecundario(String texto, Icon icono, ActionListener accion) {
        BotonElevado boton = new BotonElevado(texto, icono, TipoBoton.SECUNDARIO);
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonAcento(String texto, ActionListener accion) {
        return crearBotonAcento(texto, null, accion);
    }

    public static JButton crearBotonAcento(String texto, Icon icono, ActionListener accion) {
        BotonElevado boton = new BotonElevado(texto, icono, TipoBoton.ACENTO);
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonPeligro(String texto, ActionListener accion) {
        return crearBotonPeligro(texto, null, accion);
    }

    public static JButton crearBotonPeligro(String texto, Icon icono, ActionListener accion) {
        BotonElevado boton = new BotonElevado(texto, icono, TipoBoton.PELIGRO);
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonNeutral(String texto, ActionListener accion) {
        return crearBotonNeutral(texto, null, accion);
    }

    public static JButton crearBotonNeutral(String texto, Icon icono, ActionListener accion) {
        BotonElevado boton = new BotonElevado(texto, icono, TipoBoton.NEUTRAL);
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonFantasma(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        aplicarBotonFantasma(boton);
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JTextField crearCampoTexto(String placeholder, int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.putClientProperty(PROPIEDAD_PLACEHOLDER, placeholder);
        campo.putClientProperty(PROPIEDAD_LIMPIAR, true);
        campo.putClientProperty(PROPIEDAD_ESTILO, "arc: 10; margin: 4,12,4,12; [light]background: #ffffff; [dark]background: #343746; [light]foreground: #0f172a; [dark]foreground: #f8f8f2; [light]border: 1,1,1,1,#cbd5e1,,10; [dark]border: 1,1,1,1,#6272a4,,10; [light]placeholderForeground: #94a3b8; [dark]placeholderForeground: #6272a4");
        return campo;
    }

    public static void estilizarCampo(JComponent campo) {
        if (campo instanceof JComboBox) {
            campo.putClientProperty(PROPIEDAD_ESTILO, "arc: 10; padding: 4,10,4,10; [light]background: #ffffff; [dark]background: #343746; [light]foreground: #0f172a; [dark]foreground: #f8f8f2; [light]border: 1,1,1,1,#cbd5e1,,10; [dark]border: 1,1,1,1,#6272a4,,10");
        } else if (campo instanceof JSpinner) {
            estilizarSpinner((JSpinner) campo);
        } else {
            campo.putClientProperty(PROPIEDAD_ESTILO, "arc: 10; margin: 4,12,4,12; [light]background: #ffffff; [dark]background: #343746; [light]foreground: #0f172a; [dark]foreground: #f8f8f2; [light]border: 1,1,1,1,#cbd5e1,,10; [dark]border: 1,1,1,1,#6272a4,,10; [light]placeholderForeground: #94a3b8; [dark]placeholderForeground: #6272a4");
        }
    }

    public static void estilizarSpinner(JSpinner spinner) {
        spinner.putClientProperty(PROPIEDAD_ESTILO, "arc: 10; padding: 4,10,4,10; [light]background: #ffffff; [dark]background: #343746; [light]foreground: #0f172a; [dark]foreground: #f8f8f2; [light]border: 1,1,1,1,#cbd5e1,,10; [dark]border: 1,1,1,1,#6272a4,,10");
    }

    public static void aplicarCampoEstatico(JTextField campo) {
        campo.setEditable(false);
        campo.setFocusable(false);
        campo.putClientProperty(PROPIEDAD_ESTILO, "arc: 10; margin: 4,12,4,12; [light]background: #f1f5f9; [dark]background: #343746; [light]foreground: #0f172a; [dark]foreground: #f8f8f2; font: bold 12");
    }

    public static JButton crearBotonRefrescar(ActionListener accion) {
        return crearBotonRefrescarIcono(accion);
    }

    public static JButton crearBotonRefrescarIcono(ActionListener accion) {
        BotonElevado boton = new BotonElevado(null, Icons.refreshCw(16), TipoBoton.TURQUESA);
        boton.setPreferredSize(new Dimension(38, 38));
        boton.setMinimumSize(new Dimension(38, 38));
        boton.setMaximumSize(new Dimension(38, 38));
        boton.setToolTipText("Actualizar datos");
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonLimpiarFiltros(ActionListener accion) {
        BotonElevado boton = new BotonElevado(null, Icons.filterSlash(16), TipoBoton.PELIGRO);
        boton.setPreferredSize(new Dimension(38, 38));
        boton.setMinimumSize(new Dimension(38, 38));
        boton.setMaximumSize(new Dimension(38, 38));
        boton.setToolTipText("Quitar filtros");
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonExportarCsv(ActionListener accion) {
        JButton boton = new JButton("Exportar CSV", Icons.download(16));
        aplicarBotonTurquesa(boton);
        boton.setPreferredSize(new Dimension(150, 38));
        boton.setIconTextGap(8);
        boton.setToolTipText("Exportar a archivo Excel CSV");
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static JButton crearBotonExportarCsvIcono(ActionListener accion) {
        BotonElevado boton = new BotonElevado(null, Icons.download(16), TipoBoton.TURQUESA);
        boton.setPreferredSize(new Dimension(38, 38));
        boton.setMinimumSize(new Dimension(38, 38));
        boton.setMaximumSize(new Dimension(38, 38));
        boton.setToolTipText("Exportar a archivo Excel CSV");
        if (accion != null) {
            boton.addActionListener(accion);
        }
        return boton;
    }

    public static class PanelTarjeta extends JPanel {
        public PanelTarjeta() {
            super();
            actualizarEstilo();
        }

        public PanelTarjeta(LayoutManager layout) {
            super(layout);
            actualizarEstilo();
        }

        public void actualizarEstilo() {
            boolean oscuro = TemaGestor.esModoOscuro();
            setBackground(oscuro ? new Color(52, 55, 70) : Color.WHITE);
            Color colorBorde = oscuro ? new Color(68, 71, 90) : new Color(226, 232, 240);
            setBorder(BorderFactory.createCompoundBorder(
                    new FlatLineBorder(new Insets(1, 1, 1, 1), colorBorde, 1, 16),
                    new EmptyBorder(16, 20, 16, 20)
            ));
        }

        @Override
        public void updateUI() {
            super.updateUI();
            actualizarEstilo();
        }
    }

    public static JPanel crearTarjeta() {
        return new PanelTarjeta();
    }

    public static JPanel crearTarjetaSeccion(String titulo, JComponent contenido) {
        return crearTarjetaSeccionConBoton(titulo, null, contenido);
    }

    public static JPanel crearTarjetaSeccionConBoton(String titulo, JComponent componenteAccion, JComponent contenido) {
        PanelTarjeta tarjeta = new PanelTarjeta(new BorderLayout(10, 10));

        if ((titulo != null && !titulo.trim().isEmpty()) || componenteAccion != null) {
            JPanel panelCabecera = new JPanel(new BorderLayout(8, 4));
            panelCabecera.setOpaque(false);
            panelCabecera.setBorder(new EmptyBorder(0, 0, 6, 0));

            if (titulo != null && !titulo.trim().isEmpty()) {
                JLabel lblTit = new JLabel(titulo);
                lblTit.setFont(new Font("Segoe UI", Font.BOLD, 15));
                lblTit.putClientProperty(PROPIEDAD_ESTILO, "[light]foreground: #0f172a; [dark]foreground: #f8f8f2");
                panelCabecera.add(lblTit, BorderLayout.WEST);
            }

            if (componenteAccion != null) {
                panelCabecera.add(componenteAccion, BorderLayout.EAST);
            }

            tarjeta.add(panelCabecera, BorderLayout.NORTH);
        }

        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
    }

    public static JPanel crearTarjetaSeccion(String titulo, String subtitulo, JComponent contenido) {
        return crearTarjetaSeccion(titulo, contenido);
    }

    public static JPanel crearCampoConEtiqueta(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(4, 6));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.putClientProperty(PROPIEDAD_ESTILO, "[light]foreground: #334155; [dark]foreground: #f8f8f2");
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    public static JPanel crearBarraBotones(JButton... botones) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panel.setOpaque(false);
        for (JButton b : botones) {
            panel.add(b);
        }
        return panel;
    }

    public static class TarjetaEstadistica extends JPanel {
        private final JLabel lblTit;
        private final JLabel lblVal;
        private final Color acentoLuz;
        private final Color acentoOscuro;

        public TarjetaEstadistica(String titulo, JLabel lblVal, Color acentoLuz, Color acentoOscuro) {
            super(new BorderLayout(8, 8));
            this.lblVal = lblVal;
            this.acentoLuz = acentoLuz != null ? acentoLuz : new Color(16, 185, 129);
            this.acentoOscuro = acentoOscuro != null ? acentoOscuro : new Color(80, 250, 123);
            this.lblTit = new JLabel(titulo.toUpperCase());
            this.lblTit.setFont(new Font("Segoe UI", Font.BOLD, 12));

            lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblVal.setBorder(new EmptyBorder(2, 2, 2, 2));

            add(this.lblTit, BorderLayout.NORTH);
            add(lblVal, BorderLayout.CENTER);
            actualizarEstilo();
        }

        public void actualizarEstilo() {
            boolean oscuro = TemaGestor.esModoOscuro();
            setBackground(oscuro ? new Color(44, 47, 60) : Color.WHITE);
            Color colorBorde = oscuro ? new Color(68, 71, 90) : new Color(226, 232, 240);
            setBorder(BorderFactory.createCompoundBorder(
                    new FlatLineBorder(new Insets(1, 1, 1, 1), colorBorde, 1, 14),
                    new EmptyBorder(12, 18, 12, 18)
            ));
            if (lblTit != null) {
                lblTit.setForeground(oscuro ? new Color(148, 163, 184) : new Color(100, 116, 139));
            }
            if (lblVal != null) {
                lblVal.setForeground(oscuro ? acentoOscuro : acentoLuz);
            }
        }

        @Override
        public void updateUI() {
            super.updateUI();
            actualizarEstilo();
        }
    }

    public static JPanel crearTarjetaEstadistica(String titulo, JLabel lblVal, Color colorAcento) {
        return new TarjetaEstadistica(titulo, lblVal, colorAcento, colorAcento);
    }

    public static JPanel crearTarjetaEstadistica(String titulo, JLabel lblVal, Color acentoLuz, Color acentoOscuro) {
        return new TarjetaEstadistica(titulo, lblVal, acentoLuz, acentoOscuro);
    }

    public static JPanel crearTarjetaEstadistica(String titulo, JLabel lblVal, String descripcion, Color colorAcento) {
        return crearTarjetaEstadistica(titulo, lblVal, colorAcento);
    }

    public static void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(38);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderizadorCeldaPadded());
    }

    public static class RenderizadorCeldaPadded extends DefaultTableCellRenderer {
        private final Insets padding = new Insets(0, 14, 0, 14);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
            return this;
        }
    }

    public static JLabel crearInsignia(String texto, String estado) {
        JLabel badge = new JLabel(" " + texto + " ", SwingConstants.CENTER);
        String estilo;
        if ("A".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado) || "PAGADO".equalsIgnoreCase(estado) || "C".equalsIgnoreCase(estado) || "COBRADO".equalsIgnoreCase(estado)) {
            estilo = "arc: 999; [light]background: #dcfce7; [light]foreground: #15803d; [dark]background: #1e3a2f; [dark]foreground: #50fa7b; border: 4,12,4,12; font: bold 11";
        } else if ("P".equalsIgnoreCase(estado) || "PENDIENTE".equalsIgnoreCase(estado)) {
            estilo = "arc: 999; [light]background: #fef3c7; [light]foreground: #b45309; [dark]background: #3d3522; [dark]foreground: #f1fa8c; border: 4,12,4,12; font: bold 11";
        } else if ("I".equalsIgnoreCase(estado) || "INACTIVO".equalsIgnoreCase(estado) || "ANULADO".equalsIgnoreCase(estado)) {
            estilo = "arc: 999; [light]background: #fee2e2; [light]foreground: #b91c1c; [dark]background: #3d232a; [dark]foreground: #ff5555; border: 4,12,4,12; font: bold 11";
        } else {
            estilo = "arc: 999; [light]background: #e0e7ff; [light]foreground: #4338ca; [dark]background: #2e2b44; [dark]foreground: #bd93f9; border: 4,12,4,12; font: bold 11";
        }
        badge.putClientProperty(PROPIEDAD_ESTILO, estilo);
        return badge;
    }

    public static class RenderizadorInsigniaEstado extends DefaultTableCellRenderer {
        private final InsigniaPanel insignia = new InsigniaPanel();

        public RenderizadorInsigniaEstado() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String texto = value != null ? value.toString().trim() : "";
            Color filaBg = isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? table.getBackground() : (Color) UIManager.get("Table.alternateRowColor"));
            if (filaBg == null) {
                filaBg = table.getBackground();
            }
            insignia.configurar(texto, filaBg);
            return insignia;
        }

        private static class InsigniaPanel extends JPanel {
            private String texto = "";
            private Color colorPillBg = Color.LIGHT_GRAY;
            private Color colorPillFg = Color.BLACK;

            public InsigniaPanel() {
                setLayout(null);
                setOpaque(true);
            }

            public void configurar(String texto, Color fondoCelda) {
                this.texto = texto;
                setBackground(fondoCelda);
                boolean oscuro = TemaGestor.esModoOscuro();

                if ("A".equalsIgnoreCase(texto) || "ACTIVO".equalsIgnoreCase(texto) || "PAGADO".equalsIgnoreCase(texto) || "C".equalsIgnoreCase(texto) || "COBRADO".equalsIgnoreCase(texto)) {
                    colorPillBg = oscuro ? new Color(30, 58, 47) : new Color(220, 252, 231);
                    colorPillFg = oscuro ? new Color(80, 250, 123) : new Color(21, 128, 61);
                } else if ("P".equalsIgnoreCase(texto) || "PENDIENTE".equalsIgnoreCase(texto)) {
                    colorPillBg = oscuro ? new Color(61, 53, 34) : new Color(254, 243, 199);
                    colorPillFg = oscuro ? new Color(241, 250, 140) : new Color(180, 83, 9);
                } else if ("I".equalsIgnoreCase(texto) || "INACTIVO".equalsIgnoreCase(texto) || "ANULADO".equalsIgnoreCase(texto)) {
                    colorPillBg = oscuro ? new Color(61, 35, 42) : new Color(254, 226, 226);
                    colorPillFg = oscuro ? new Color(255, 85, 85) : new Color(185, 28, 28);
                } else {
                    colorPillBg = oscuro ? new Color(46, 43, 68) : new Color(224, 231, 255);
                    colorPillFg = oscuro ? new Color(189, 147, 249) : new Color(67, 56, 202);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (texto.isEmpty()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Font font = new Font("Segoe UI", Font.BOLD, 11);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics();

                int textoAncho = fm.stringWidth(texto);
                int pillAncho = textoAncho + 24;
                int pillAlto = 24;

                int x = (getWidth() - pillAncho) / 2;
                int y = (getHeight() - pillAlto) / 2;

                g2.setColor(colorPillBg);
                g2.fillRoundRect(x, y, pillAncho, pillAlto, pillAlto, pillAlto);

                g2.setColor(colorPillFg);
                int textoX = x + 12;
                int textoY = y + ((pillAlto - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(texto, textoX, textoY);

                g2.dispose();
            }
        }
    }

    public enum TipoDialogo { EXITO, ADVERTENCIA, ERROR, INFORMACION, CONFIRMACION }

    public static void mostrarExito(Component padre, String titulo, String mensaje) {
        mostrarNotificacion(padre, titulo, mensaje, TipoDialogo.EXITO);
    }

    public static void mostrarAdvertencia(Component padre, String titulo, String mensaje) {
        mostrarNotificacion(padre, titulo, mensaje, TipoDialogo.ADVERTENCIA);
    }

    public static void mostrarError(Component padre, String titulo, String mensaje) {
        mostrarNotificacion(padre, titulo, mensaje, TipoDialogo.ERROR);
    }

    public static void mostrarInformacion(Component padre, String titulo, String mensaje) {
        mostrarNotificacion(padre, titulo, mensaje, TipoDialogo.INFORMACION);
    }

    public static boolean mostrarConfirmacion(Component padre, String titulo, String mensaje) {
        String textoSeguro = mensaje == null ? "" : mensaje;
        String html = "<html><body style='width: 320px; font-family: Segoe UI, sans-serif; font-size: 11pt; line-height: 1.45;'>"
                + textoSeguro.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>")
                + "</body></html>";
        int res = JOptionPane.showConfirmDialog(
                padre,
                html,
                titulo != null && !titulo.isEmpty() ? titulo : "Confirmar Acción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                Icons.helpCircle(32)
        );
        return res == JOptionPane.YES_OPTION;
    }

    public static void mostrarNotificacion(Component padre, String titulo, String mensaje, TipoDialogo tipo) {
        int tipoMensaje;
        Icon icono;
        switch (tipo) {
            case EXITO:
                tipoMensaje = JOptionPane.INFORMATION_MESSAGE;
                icono = Icons.check(32);
                break;
            case ADVERTENCIA:
                tipoMensaje = JOptionPane.WARNING_MESSAGE;
                icono = Icons.triangleAlert(32);
                break;
            case ERROR:
                tipoMensaje = JOptionPane.ERROR_MESSAGE;
                icono = Icons.x(32);
                break;
            default:
                tipoMensaje = JOptionPane.INFORMATION_MESSAGE;
                icono = Icons.info(32);
                break;
        }
        String textoSeguro = mensaje == null ? "" : mensaje;
        String html = "<html><body style='width: 320px; font-family: Segoe UI, sans-serif; font-size: 11pt; line-height: 1.45;'>"
                + textoSeguro.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>")
                + "</body></html>";
        JOptionPane.showMessageDialog(padre, html, titulo != null && !titulo.isEmpty() ? titulo : "Aviso", tipoMensaje, icono);
    }

    public static void mostrarToast(Component padre, String mensaje, TipoDialogo tipo) {
        ToastDaisyUI.mostrar(padre, mensaje, tipo);
    }

    public static void mostrarToastExito(Component padre, String mensaje) {
        mostrarToast(padre, mensaje, TipoDialogo.EXITO);
    }

    public static void mostrarToastAdvertencia(Component padre, String mensaje) {
        mostrarToast(padre, mensaje, TipoDialogo.ADVERTENCIA);
    }

    public static void mostrarToastError(Component padre, String mensaje) {
        mostrarToast(padre, mensaje, TipoDialogo.ERROR);
    }

    public static void mostrarToastInfo(Component padre, String mensaje) {
        mostrarToast(padre, mensaje, TipoDialogo.INFORMACION);
    }

    public static class InsigniaCircular extends JComponent {
        private final Icon icono;
        private final Color colorFondo;
        private int diametro = 46;

        public InsigniaCircular(Icon icono, Color colorFondo) {
            this(icono, colorFondo, 46);
        }

        public InsigniaCircular(Icon icono, Color colorFondo, int diametro) {
            this.icono = icono;
            this.colorFondo = colorFondo;
            this.diametro = diametro;
            setOpaque(false);
            setPreferredSize(new Dimension(diametro + 6, diametro + 6));
            setMinimumSize(new Dimension(diametro + 6, diametro + 6));
            setMaximumSize(new Dimension(diametro + 6, diametro + 6));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = diametro;
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - d) / 2;
            g2.setColor(colorFondo);
            g2.fillOval(x, y, d, d);
            if (icono != null) {
                int ix = (getWidth() - icono.getIconWidth()) / 2;
                int iy = (getHeight() - icono.getIconHeight()) / 2;
                icono.paintIcon(this, g2, ix, iy);
            }
            g2.dispose();
        }
    }

    private static class ToastDaisyUI extends JWindow {
        private final Timer temporizador;

        private ToastDaisyUI(Window owner, String mensaje, TipoDialogo tipo) {
            super(owner);
            setAlwaysOnTop(true);
            setBackground(new Color(0, 0, 0, 0));

            boolean oscuro = TemaGestor.esModoOscuro();
            Color bg = oscuro ? new Color(40, 42, 54, 245) : new Color(255, 255, 255, 245);
            Color border = oscuro ? new Color(98, 114, 164) : new Color(203, 213, 225);
            Color fg = oscuro ? new Color(248, 248, 242) : new Color(15, 23, 42);

            Icon icono;
            Color iconBg;
            switch (tipo) {
                case EXITO:
                    icono = Icons.check(16);
                    iconBg = oscuro ? new Color(30, 58, 47) : new Color(220, 252, 231);
                    break;
                case ADVERTENCIA:
                    icono = Icons.triangleAlert(16);
                    iconBg = oscuro ? new Color(61, 53, 34) : new Color(254, 243, 199);
                    break;
                case ERROR:
                    icono = Icons.x(16);
                    iconBg = oscuro ? new Color(61, 35, 42) : new Color(254, 226, 226);
                    break;
                default:
                    icono = Icons.info(14);
                    iconBg = oscuro ? new Color(46, 43, 68) : new Color(224, 231, 255);
                    break;
            }

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 25));
                    g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 16, 16);
                    g2.setColor(bg);
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                    g2.setColor(border);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                    g2.dispose();
                }
            };
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(3, 8, 3, 8));

            InsigniaCircular badge = new InsigniaCircular(icono, iconBg, 22);

            String textoSeguro = mensaje == null ? "" : mensaje.trim();
            JLabel lbl;
            if (textoSeguro.contains("\n") || textoSeguro.length() > 40) {
                String html = "<html><body style='font-family: Segoe UI, sans-serif; font-size: 9.5pt; font-weight: 500;'>"
                        + textoSeguro.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>")
                        + "</body></html>";
                lbl = new JLabel(html);
            } else {
                lbl = new JLabel(textoSeguro);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            lbl.setForeground(fg);
            lbl.setBorder(new EmptyBorder(0, 4, 0, 6));

            JButton btnCerrar = new JButton(Icons.x(10));
            btnCerrar.setPreferredSize(new Dimension(18, 18));
            btnCerrar.setOpaque(false);
            btnCerrar.setContentAreaFilled(false);
            btnCerrar.setBorderPainted(false);
            btnCerrar.setFocusPainted(false);
            btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCerrar.addActionListener(e -> cerrarToast());

            panel.add(badge);
            panel.add(lbl);
            panel.add(btnCerrar);

            add(panel);
            pack();

            Point ubicacion = calcularUbicacion(owner, getWidth(), getHeight());
            setLocation(ubicacion);

            temporizador = new Timer(3000, e -> cerrarToast());
            temporizador.setRepeats(false);
            temporizador.start();
        }

        private static Point calcularUbicacion(Window owner, int w, int h) {
            if (owner != null && owner.isShowing()) {
                Rectangle r = owner.getBounds();
                return new Point(r.x + r.width - w - 24, r.y + 44);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle scr = ge.getMaximumWindowBounds();
            return new Point(scr.x + scr.width - w - 24, scr.y + 44);
        }

        private void cerrarToast() {
            if (temporizador != null && temporizador.isRunning()) {
                temporizador.stop();
            }
            dispose();
        }

        public static void mostrar(Component padre, String mensaje, TipoDialogo tipo) {
            Window win = padre == null ? null : (padre instanceof Window ? (Window) padre : SwingUtilities.getWindowAncestor(padre));
            ToastDaisyUI toast = new ToastDaisyUI(win, mensaje, tipo);
            toast.setVisible(true);
        }
    }
}
