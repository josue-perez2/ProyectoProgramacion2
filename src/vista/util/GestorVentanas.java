package vista.util;

import java.awt.Frame;
import java.awt.Window;
import java.util.HashMap;
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
}
