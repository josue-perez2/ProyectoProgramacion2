package vista.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class TemaGestor {

    public static final String TEMA_EMERALD = "Emerald (Claro)";
    public static final String TEMA_DRACULA = "Dracula (Oscuro)";

    private static String temaActual = TEMA_EMERALD;

    public static String[] obtenerNombresTemas() {
        return new String[]{
                TEMA_EMERALD,
                TEMA_DRACULA
        };
    }

    public static String getTemaActual() {
        return temaActual;
    }

    public static void iniciarTema() {
        System.setProperty("flatlaf.useWindowDecorations", "true");
        System.setProperty("flatlaf.menuBarEmbedded", "true");
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        aplicarTema(TEMA_EMERALD);
    }

    public static void aplicarConfiguracionGlobal() {
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("TextField.margin", new Insets(6, 12, 6, 12));
        UIManager.put("PasswordField.margin", new Insets(6, 12, 6, 12));
        UIManager.put("ComboBox.padding", new Insets(4, 10, 4, 10));
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.selectionArc", 8);
        UIManager.put("Table.selectionInsets", new Insets(2, 4, 2, 4));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("TableHeader.height", 38);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Popup.dropShadowPainted", true);
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("MenuItem.selectionArc", 8);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("OptionPane.showIcon", true);
        UIManager.put("OptionPane.border", new EmptyBorder(16, 20, 16, 20));
        UIManager.put("OptionPane.messageAreaBorder", new EmptyBorder(4, 4, 12, 4));
        UIManager.put("OptionPane.buttonAreaBorder", new EmptyBorder(12, 4, 4, 4));
        UIManager.put("OptionPane.buttonPadding", 12);
    }

    public static void aplicarTema(String nombreTema) {
        temaActual = nombreTema;
        try {
            Color colorFondo;
            Color colorTarjeta;
            Color colorFilaAlt;
            Color colorBorde;
            Color colorTexto;
            Color colorAcento;
            Color colorPlaceholder;

            if (TEMA_DRACULA.equals(nombreTema)) {
                FlatDraculaIJTheme.setup();
                colorFondo = new Color(40, 42, 54);
                colorTarjeta = new Color(52, 55, 70);
                colorFilaAlt = new Color(46, 48, 62);
                colorBorde = new Color(68, 71, 90);
                colorTexto = new Color(248, 248, 242);
                colorAcento = new Color(189, 147, 249);
                colorPlaceholder = new Color(98, 114, 164);

                UIManager.put("@accentColor", "#bd93f9");
                UIManager.put("Component.focusColor", new ColorUIResource(189, 147, 249));
                UIManager.put("Component.borderColor", new ColorUIResource(colorBorde));
                UIManager.put("Component.focusedBorderColor", new ColorUIResource(colorAcento));
                UIManager.put("Panel.background", new ColorUIResource(colorFondo));
                UIManager.put("RootPane.background", new ColorUIResource(colorFondo));
                UIManager.put("Label.foreground", new ColorUIResource(colorTexto));
                UIManager.put("Table.background", new ColorUIResource(colorTarjeta));
                UIManager.put("Table.foreground", new ColorUIResource(colorTexto));
                UIManager.put("Table.alternateRowColor", new ColorUIResource(colorFilaAlt));
                UIManager.put("Table.selectionBackground", new ColorUIResource(68, 71, 90));
                UIManager.put("Table.selectionForeground", new ColorUIResource(colorTexto));
                UIManager.put("Table.gridColor", new ColorUIResource(colorBorde));
                UIManager.put("TableHeader.background", new ColorUIResource(colorFondo));
                UIManager.put("TableHeader.foreground", new ColorUIResource(colorAcento));
                UIManager.put("TableHeader.separatorColor", new ColorUIResource(colorBorde));
                UIManager.put("ScrollPane.background", new ColorUIResource(colorTarjeta));
                UIManager.put("Viewport.background", new ColorUIResource(colorTarjeta));
                UIManager.put("ComboBox.background", new ColorUIResource(colorTarjeta));
                UIManager.put("ComboBox.foreground", new ColorUIResource(colorTexto));
                UIManager.put("TextField.background", new ColorUIResource(colorTarjeta));
                UIManager.put("TextField.foreground", new ColorUIResource(colorTexto));
                UIManager.put("TextField.caretForeground", new ColorUIResource(colorTexto));
                UIManager.put("TextField.placeholderForeground", new ColorUIResource(colorPlaceholder));
                UIManager.put("PopupMenu.background", new ColorUIResource(colorTarjeta));
                UIManager.put("MenuItem.background", new ColorUIResource(colorTarjeta));
                UIManager.put("MenuItem.foreground", new ColorUIResource(colorTexto));
                UIManager.put("MenuItem.selectionBackground", new ColorUIResource(68, 71, 90));
                UIManager.put("ScrollBar.track", new ColorUIResource(colorFondo));
                UIManager.put("ScrollBar.thumb", new ColorUIResource(68, 71, 90));
                UIManager.put("TitlePane.background", new ColorUIResource(colorFondo));
                UIManager.put("TitlePane.foreground", new ColorUIResource(colorTexto));
            } else {
                FlatLightLaf.setup();
                colorFondo = new Color(248, 250, 252);
                colorTarjeta = Color.WHITE;
                colorFilaAlt = new Color(248, 250, 252);
                colorBorde = new Color(226, 232, 240);
                colorTexto = new Color(15, 23, 42);
                colorAcento = new Color(16, 185, 129);
                colorPlaceholder = new Color(148, 163, 184);

                UIManager.put("@accentColor", "#10b981");
                UIManager.put("Component.focusColor", new ColorUIResource(16, 185, 129));
                UIManager.put("Component.borderColor", new ColorUIResource(203, 213, 225));
                UIManager.put("Component.focusedBorderColor", new ColorUIResource(colorAcento));
                UIManager.put("Panel.background", new ColorUIResource(colorFondo));
                UIManager.put("RootPane.background", new ColorUIResource(colorFondo));
                UIManager.put("Label.foreground", new ColorUIResource(colorTexto));
                UIManager.put("Table.background", new ColorUIResource(colorTarjeta));
                UIManager.put("Table.foreground", new ColorUIResource(colorTexto));
                UIManager.put("Table.alternateRowColor", new ColorUIResource(colorFilaAlt));
                UIManager.put("Table.selectionBackground", new ColorUIResource(209, 250, 229));
                UIManager.put("Table.selectionForeground", new ColorUIResource(6, 95, 70));
                UIManager.put("Table.gridColor", new ColorUIResource(241, 245, 249));
                UIManager.put("TableHeader.background", new ColorUIResource(241, 245, 249));
                UIManager.put("TableHeader.foreground", new ColorUIResource(colorTexto));
                UIManager.put("TableHeader.separatorColor", new ColorUIResource(colorBorde));
                UIManager.put("ScrollPane.background", new ColorUIResource(colorTarjeta));
                UIManager.put("Viewport.background", new ColorUIResource(colorTarjeta));
                UIManager.put("ComboBox.background", new ColorUIResource(colorTarjeta));
                UIManager.put("ComboBox.foreground", new ColorUIResource(colorTexto));
                UIManager.put("TextField.background", new ColorUIResource(colorTarjeta));
                UIManager.put("TextField.foreground", new ColorUIResource(colorTexto));
                UIManager.put("TextField.caretForeground", new ColorUIResource(colorTexto));
                UIManager.put("TextField.placeholderForeground", new ColorUIResource(colorPlaceholder));
                UIManager.put("PopupMenu.background", new ColorUIResource(colorTarjeta));
                UIManager.put("MenuItem.background", new ColorUIResource(colorTarjeta));
                UIManager.put("MenuItem.foreground", new ColorUIResource(colorTexto));
                UIManager.put("MenuItem.selectionBackground", new ColorUIResource(209, 250, 229));
                UIManager.put("ScrollBar.track", new ColorUIResource(colorFondo));
                UIManager.put("ScrollBar.thumb", new ColorUIResource(203, 213, 225));
                UIManager.put("TitlePane.background", new ColorUIResource(colorFondo));
                UIManager.put("TitlePane.foreground", new ColorUIResource(colorTexto));
            }

            aplicarConfiguracionGlobal();
            FlatLaf.updateUI();

            for (Frame f : Frame.getFrames()) {
                actualizarArbolComponentes(f, colorFondo, colorTarjeta, colorFilaAlt, colorBorde, colorTexto, colorAcento);
                for (Window w : f.getOwnedWindows()) {
                    actualizarArbolComponentes(w, colorFondo, colorTarjeta, colorFilaAlt, colorBorde, colorTexto, colorAcento);
                }
            }
        } catch (Exception ex) {
            FlatLightLaf.setup();
        }
    }

    private static void actualizarArbolComponentes(Window w, Color fondo, Color tarjeta, Color filaAlt, Color borde, Color texto, Color acento) {
        if (w instanceof RootPaneContainer) {
            ((RootPaneContainer) w).getContentPane().setBackground(fondo);
        }
        w.setBackground(fondo);
        actualizarHijosRecursivo(w, fondo, tarjeta, filaAlt, borde, texto, acento);
        SwingUtilities.updateComponentTreeUI(w);
        w.revalidate();
        w.repaint();
    }

    private static void actualizarHijosRecursivo(Container cont, Color fondo, Color tarjeta, Color filaAlt, Color borde, Color texto, Color acento) {
        for (Component c : cont.getComponents()) {
            if (c instanceof JTable) {
                JTable t = (JTable) c;
                t.setBackground(tarjeta);
                t.setForeground(texto);
                t.setGridColor(borde);
                t.setShowVerticalLines(false);
                t.setShowHorizontalLines(true);
                if (t.getTableHeader() != null) {
                    t.getTableHeader().setBackground(fondo);
                    t.getTableHeader().setForeground(acento);
                }
            } else if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                sp.getViewport().setBackground(tarjeta);
                sp.setBackground(tarjeta);
            } else if (c instanceof JComboBox) {
                c.setBackground(tarjeta);
                c.setForeground(texto);
            } else if (c instanceof JTextField) {
                if (((JTextField) c).isEditable()) {
                    c.setBackground(tarjeta);
                    c.setForeground(texto);
                }
            }

            if (c instanceof Container) {
                actualizarHijosRecursivo((Container) c, fondo, tarjeta, filaAlt, borde, texto, acento);
            }
        }
    }

    public static boolean esModoOscuro() {
        return TEMA_DRACULA.equals(temaActual);
    }
}
