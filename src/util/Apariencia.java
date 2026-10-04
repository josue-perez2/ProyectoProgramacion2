package util;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class Apariencia {

    public static final Color FONDO = Color.WHITE;
    private static final Color TEXTO = new Color(0x1F2933);
    private static final Color BORDE = new Color(0xE5E7EB);
    private static final String RUTA_RECURSO = "recursos/logo.png";

    private static BufferedImage logo;
    private static boolean logoCargado;

    private Apariencia() {
    }

    public static void aplicarEstiloClaro() {
        for (String laf : new String[]{
                "com.sun.java.swing.plaf.metal.MetalLookAndFeel",
                UIManager.getCrossPlatformLookAndFeelClassName()}) {
            try {
                UIManager.setLookAndFeel(laf);
                break;
            } catch (Exception ex) {
                continue;
            }
        }

        UIManager.put("control", new Color(0xF3F4F6));
        UIManager.put("text", TEXTO);
        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", FONDO);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Button.background", BORDE);
        UIManager.put("Button.foreground", TEXTO);
        UIManager.put("ToggleButton.background", BORDE);
        UIManager.put("ToggleButton.foreground", TEXTO);
        UIManager.put("Table.background", FONDO);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.gridColor", BORDE);
        UIManager.put("Table.selectionBackground", new Color(0xDBEAFE));
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("TableHeader.background", BORDE);
        UIManager.put("TableHeader.foreground", TEXTO);
        UIManager.put("TextField.background", FONDO);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.caretForeground", TEXTO);
        UIManager.put("TextArea.background", FONDO);
        UIManager.put("TextArea.foreground", TEXTO);
        UIManager.put("ComboBox.background", FONDO);
        UIManager.put("ComboBox.foreground", TEXTO);
        UIManager.put("ScrollPane.background", FONDO);
        UIManager.put("Viewport.background", FONDO);
        UIManager.put("SplitPane.background", FONDO);
        UIManager.put("TabbedPane.background", FONDO);
        UIManager.put("TabbedPane.foreground", TEXTO);
        UIManager.put("List.background", FONDO);
        UIManager.put("List.foreground", TEXTO);
        UIManager.put("MenuBar.background", FONDO);
        UIManager.put("Menu.background", FONDO);
        UIManager.put("Menu.foreground", TEXTO);
        UIManager.put("MenuItem.background", FONDO);
        UIManager.put("MenuItem.foreground", TEXTO);
        UIManager.put("TitledBorder.background", FONDO);
        UIManager.put("TitledBorder.titleColor", TEXTO);
        UIManager.put("CheckBox.background", FONDO);
        UIManager.put("CheckBox.foreground", TEXTO);
        UIManager.put("RadioButton.background", FONDO);
        UIManager.put("RadioButton.foreground", TEXTO);
        UIManager.put("Separator.background", BORDE);
        UIManager.put("ToolTip.background", FONDO);
        UIManager.put("ToolTip.foreground", TEXTO);
    }

    public static BufferedImage obtenerLogo() {
        if (!logoCargado) {
            logoCargado = true;
            logo = leerLogo();
            if (logo == null) {
                System.err.println("No se encontro el archivo " + RUTA_RECURSO + " en ninguna ubicacion conocida.");
            }
        }
        return logo;
    }

    private static BufferedImage leerLogo() {
        try (InputStream entrada = Apariencia.class.getClassLoader().getResourceAsStream(RUTA_RECURSO)) {
            if (entrada != null) {
                return ImageIO.read(entrada);
            }
        } catch (IOException ex) {
            System.err.println("No se pudo leer el logo del recurso: " + ex.getMessage());
        }

        for (File archivo : rutasAlternativas()) {
            if (archivo.isFile()) {
                try {
                    return ImageIO.read(archivo);
                } catch (IOException ex) {
                    System.err.println("No se pudo leer el logo de " + archivo + ": " + ex.getMessage());
                }
            }
        }
        return null;
    }

    private static List<File> rutasAlternativas() {
        List<File> rutas = new ArrayList<>();
        String base = System.getProperty("user.dir", ".");
        rutas.add(new File(base, RUTA_RECURSO));
        rutas.add(new File(base, "src/" + RUTA_RECURSO));
        rutas.add(new File(base, "../" + RUTA_RECURSO));
        rutas.add(new File(base, "../../" + RUTA_RECURSO));
        rutas.add(new File(base, "src/main/resources/" + RUTA_RECURSO));

        try {
            URL ubicacion = Apariencia.class.getProtectionDomain().getCodeSource().getLocation();
            File carpeta = new File(ubicacion.toURI());
            if (!carpeta.isDirectory()) {
                carpeta = carpeta.getParentFile();
            }
            if (carpeta != null) {
                rutas.add(new File(carpeta, RUTA_RECURSO));
            }
        } catch (URISyntaxException | NullPointerException | IllegalArgumentException ex) {
            System.err.println("No se pudo resolver la ubicacion de la aplicacion: " + ex.getMessage());
        }

        return rutas;
    }

    public static JPanel crearEncabezado() {
        return crearEncabezado(90);
    }

    public static JPanel crearEncabezado(int alto) {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(FONDO);
        encabezado.setOpaque(true);
        encabezado.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));
        encabezado.setPreferredSize(new Dimension(0, alto));

        JLabel etiqueta = new JLabel();
        etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
        etiqueta.setBackground(FONDO);
        etiqueta.setOpaque(true);

        BufferedImage imagen = obtenerLogo();
        if (imagen != null) {
            etiqueta.setIcon(new ImageIcon(escalar(imagen, alto - 20)));
        }

        encabezado.add(etiqueta, BorderLayout.CENTER);
        return encabezado;
    }

    public static Image escalar(Image imagen, int alto) {
        if (imagen == null || alto <= 0) {
            return null;
        }
        int anchoOriginal = imagen.getWidth(null);
        int altoOriginal = imagen.getHeight(null);
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            return null;
        }
        int ancho = Math.max(1, (int) Math.round(anchoOriginal * (alto / (double) altoOriginal)));
        return imagen.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
    }

    public static void aplicarIcono(Window ventana) {
        BufferedImage imagen = obtenerLogo();
        if (imagen == null) {
            return;
        }
        ventana.setIconImage(escalar(imagen, 64));
    }

    public static void pintarFondo(Window ventana) {
        if (ventana instanceof javax.swing.JFrame) {
            javax.swing.JFrame frame = (javax.swing.JFrame) ventana;
            frame.getContentPane().setBackground(FONDO);
            frame.getRootPane().setBackground(FONDO);
            frame.setBackground(FONDO);
        }
    }
}
