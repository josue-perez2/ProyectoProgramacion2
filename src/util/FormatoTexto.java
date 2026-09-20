package util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.function.Function;

public class FormatoTexto {

    public static DocumentFilter filtroDpi() {
        return filtroDigitos(13, "", FormatoTexto::formatearDpi);
    }

    public static DocumentFilter filtroTelefono() {
        return filtroDigitos(8, "+502 ", FormatoTexto::formatearTelefono);
    }

    public static DocumentFilter filtroDigitos(int maxDigitos, String prefijo, Function<String, String> formateador) {
        return new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.insert(offset, string);
                aplicar(fb, sb.toString());
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text == null ? "" : text);
                aplicar(fb, sb.toString());
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.delete(offset, offset + length);
                aplicar(fb, sb.toString());
            }

            private void aplicar(FilterBypass fb, String contenido) throws BadLocationException {
                String cuerpo = contenido;
                if (!prefijo.isEmpty() && contenido.startsWith(prefijo)) {
                    cuerpo = contenido.substring(prefijo.length());
                }
                StringBuilder digitos = new StringBuilder();
                for (char c : cuerpo.toCharArray()) {
                    if (Character.isDigit(c)) {
                        digitos.append(c);
                        if (digitos.length() == maxDigitos) {
                            break;
                        }
                    }
                }
                fb.replace(0, fb.getDocument().getLength(), prefijo + formateador.apply(digitos.toString()), null);
            }
        };
    }

    public static String formatearDpi(String digitos) {
        if (digitos.length() <= 4) {
            return digitos;
        }
        if (digitos.length() <= 9) {
            return digitos.substring(0, 4) + " " + digitos.substring(4);
        }
        return digitos.substring(0, 4) + " " + digitos.substring(4, 9) + " " + digitos.substring(9);
    }

    public static String formatearTelefono(String digitos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digitos.length(); i++) {
            if (i == 4) {
                sb.append('-');
            }
            sb.append(digitos.charAt(i));
        }
        return sb.toString();
    }

    public static String soloDigitos(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String soloDigitosTelefono(String texto) {
        String cuerpo = texto;
        if (texto.startsWith("+502 ")) {
            cuerpo = texto.substring("+502 ".length());
        }
        return soloDigitos(cuerpo);
    }
}