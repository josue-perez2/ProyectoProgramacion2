package util;

import vista.util.FabricaDaisyUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportadorCSV {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static boolean exportarTabla(Component padre, JTable tabla, String nombreSugerido) {
        if (tabla == null || tabla.getModel() == null) {
            FabricaDaisyUI.mostrarAdvertencia(padre, "Exportación", "No hay datos en la tabla para exportar.");
            return false;
        }

        TableModel modelo = tabla.getModel();
        int filas = modelo.getRowCount();
        int columnas = modelo.getColumnCount();

        if (filas == 0) {
            FabricaDaisyUI.mostrarAdvertencia(padre, "Exportación", "La tabla está vacía. No hay registros para exportar.");
            return false;
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar como archivo CSV");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));
        selector.setAcceptAllFileFilterUsed(false);

        String nombreArchivoDefecto = (nombreSugerido != null && !nombreSugerido.trim().isEmpty() ? nombreSugerido.trim() : "reporte")
                + "_" + LocalDateTime.now().format(FORMATO_ARCHIVO) + ".csv";
        selector.setSelectedFile(new File(nombreArchivoDefecto));

        int seleccion = selector.showSaveDialog(padre);
        if (seleccion != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }

        if (archivo.exists()) {
            boolean sobreescribir = FabricaDaisyUI.mostrarConfirmacion(
                    padre,
                    "Confirmar Sobrescritura",
                    "El archivo ya existe:\n" + archivo.getName() + "\n¿Desea reemplazarlo?"
            );
            if (!sobreescribir) {
                return false;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            bw.write("\uFEFF");

            for (int col = 0; col < columnas; col++) {
                String nombreCol = modelo.getColumnName(col);
                bw.write(escaparValorCSV(nombreCol));
                if (col < columnas - 1) {
                    bw.write(";");
                }
            }
            bw.newLine();

            for (int row = 0; row < filas; row++) {
                for (int col = 0; col < columnas; col++) {
                    Object val = modelo.getValueAt(row, col);
                    String texto = val != null ? val.toString() : "";
                    bw.write(escaparValorCSV(texto));
                    if (col < columnas - 1) {
                        bw.write(";");
                    }
                }
                bw.newLine();
            }

            bw.flush();
            FabricaDaisyUI.mostrarToastExito(padre, "Reporte exportado exitosamente:\n" + archivo.getName());
            return true;
        } catch (Exception ex) {
            FabricaDaisyUI.mostrarError(padre, "Error al Exportar", "Ocurrió un error al guardar el archivo CSV: " + ex.getMessage());
            return false;
        }
    }

    private static String escaparValorCSV(String valor) {
        if (valor == null) {
            return "\"\"";
        }
        String v = valor.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }
}
