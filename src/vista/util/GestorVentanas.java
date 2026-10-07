package vista.util;

import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class GestorVentanas {

    private static final Map<Class<?>, Window> ventanasActivas = new HashMap<>();

    private GestorVentanas() {
    }

    public static synchronized <T extends Window> T abrirOEnfocar(Class<T> clase, Supplier<T> creador) {
        Window existente = ventanasActivas.get(clase);
        if (existente != null && existente.isDisplayable()) {
            enfocar(existente);
            return clase.cast(existente);
        }
        T nueva = creador.get();
        ventanasActivas.put(clase, nueva);
        enfocar(nueva);
        return nueva;
    }

    public static synchronized void registrarVentana(Window ventana) {
        if (ventana != null) {
            ventanasActivas.put(ventana.getClass(), ventana);
        }
    }

    public static synchronized void desregistrarVentana(Class<?> clase) {
        ventanasActivas.remove(clase);
    }

    public static synchronized boolean estaAbierta(Class<?> clase) {
        Window existente = ventanasActivas.get(clase);
        return existente != null && existente.isDisplayable();
    }

    public static synchronized void enfocar(Window ventana) {
        if (ventana == null) {
            return;
        }
        if (ventana instanceof Frame frame) {
            if (frame.getState() == Frame.ICONIFIED) {
                frame.setState(Frame.NORMAL);
            }
        }
        if (!ventana.isVisible()) {
            ventana.setVisible(true);
        }
        ventana.toFront();
        ventana.requestFocus();
    }

    public static synchronized void cerrar(Class<?> clase) {
        Window existente = ventanasActivas.remove(clase);
        if (existente != null && existente.isDisplayable()) {
            existente.dispose();
        }
    }

    public static synchronized void cerrarTodas() {
        for (Window w : ventanasActivas.values()) {
            if (w != null && w.isDisplayable()) {
                w.dispose();
            }
        }
        ventanasActivas.clear();
    }

    public static void notificarCambio(String tipo) {
        SwingUtilities.invokeLater(() -> {
            List<Window> copia;
            synchronized (GestorVentanas.class) {
                copia = new ArrayList<>(ventanasActivas.values());
            }
            for (Window w : copia) {
                if (w != null && w.isDisplayable() && w instanceof Actualizable act) {
                    try {
                        act.actualizarDatos();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }
}
