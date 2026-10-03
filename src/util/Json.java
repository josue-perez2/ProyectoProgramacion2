package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Json {

    private final String texto;
    private int posicion;

    private Json(String texto) {
        this.texto = texto;
    }

    public static String escribir(Object valor) {
        StringBuilder sb = new StringBuilder();
        agregar(sb, valor);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void agregar(StringBuilder sb, Object valor) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof Map) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<String, Object> entrada : ((Map<String, Object>) valor).entrySet()) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                agregar(sb, entrada.getKey());
                sb.append(':');
                agregar(sb, entrada.getValue());
            }
            sb.append('}');
        } else if (valor instanceof List) {
            sb.append('[');
            boolean primero = true;
            for (Object elemento : (List<Object>) valor) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                agregar(sb, elemento);
            }
            sb.append(']');
        } else if (valor instanceof Number || valor instanceof Boolean) {
            sb.append(valor);
        } else {
            agregarTexto(sb, valor.toString());
        }
    }

    private static void agregarTexto(StringBuilder sb, String texto) {
        sb.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);
            switch (caracter) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (caracter < 0x20) {
                        sb.append(String.format("\\u%04x", (int) caracter));
                    } else {
                        sb.append(caracter);
                    }
                }
            }
        }
        sb.append('"');
    }

    public static Map<String, Object> leerObjeto(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return new LinkedHashMap<>();
        }
        Json lector = new Json(cuerpo);
        try {
            lector.saltarEspacios();
            if (lector.consumir('{')) {
                return lector.leerCampos();
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("El cuerpo no es un JSON valido", e);
        }
        return new LinkedHashMap<>();
    }

    public static String texto(Map<String, Object> objeto, String clave) {
        Object valor = objeto.get(clave);
        if (valor == null) {
            return null;
        }
        String texto = valor instanceof String ? (String) valor : String.valueOf(valor);
        return texto.isBlank() ? null : texto;
    }

    private Map<String, Object> leerCampos() {
        Map<String, Object> campos = new LinkedHashMap<>();
        saltarEspacios();
        if (consumir('}')) {
            return campos;
        }
        while (true) {
            saltarEspacios();
            String clave = leerTexto();
            saltarEspacios();
            if (!consumir(':')) {
                throw new IllegalStateException("Se esperaba ':' en la posicion " + posicion);
            }
            saltarEspacios();
            campos.put(clave, leerValor());
            saltarEspacios();
            if (consumir(',')) {
                continue;
            }
            if (consumir('}')) {
                return campos;
            }
            throw new IllegalStateException("Se esperaba ',' o '}' en la posicion " + posicion);
        }
    }

    private Object leerValor() {
        if (posicion >= texto.length()) {
            throw new IllegalStateException("Cuerpo JSON incompleto");
        }
        char caracter = texto.charAt(posicion);
        if (caracter == '{') {
            posicion++;
            return leerCampos();
        }
        if (caracter == '[') {
            posicion++;
            return leerArreglo();
        }
        if (caracter == '"') {
            return leerTexto();
        }
        if (texto.startsWith("true", posicion)) {
            posicion += 4;
            return Boolean.TRUE;
        }
        if (texto.startsWith("false", posicion)) {
            posicion += 5;
            return Boolean.FALSE;
        }
        if (texto.startsWith("null", posicion)) {
            posicion += 4;
            return null;
        }
        return leerNumero();
    }

    private List<Object> leerArreglo() {
        List<Object> elementos = new ArrayList<>();
        saltarEspacios();
        if (consumir(']')) {
            return elementos;
        }
        while (true) {
            saltarEspacios();
            elementos.add(leerValor());
            saltarEspacios();
            if (consumir(',')) {
                continue;
            }
            if (consumir(']')) {
                return elementos;
            }
            throw new IllegalStateException("Se esperaba ',' o ']' en la posicion " + posicion);
        }
    }

    private String leerTexto() {
        if (!consumir('"')) {
            throw new IllegalStateException("Se esperaba un texto en la posicion " + posicion);
        }
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (posicion >= texto.length()) {
                throw new IllegalStateException("Texto JSON sin cerrar");
            }
            char caracter = texto.charAt(posicion++);
            if (caracter == '"') {
                return sb.toString();
            }
            if (caracter != '\\') {
                sb.append(caracter);
                continue;
            }
            char escape = texto.charAt(posicion++);
            switch (escape) {
                case '"' -> sb.append('"');
                case '\\' -> sb.append('\\');
                case '/' -> sb.append('/');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(texto.substring(posicion, posicion + 4), 16));
                    posicion += 4;
                }
                default -> throw new IllegalStateException("Escape JSON invalido: \\" + escape);
            }
        }
    }

    private Object leerNumero() {
        int inicio = posicion;
        while (posicion < texto.length() && "+-0123456789.eE".indexOf(texto.charAt(posicion)) >= 0) {
            posicion++;
        }
        String numero = texto.substring(inicio, posicion);
        if (numero.isEmpty()) {
            throw new IllegalStateException("Valor JSON invalido en la posicion " + inicio);
        }
        return new java.math.BigDecimal(numero);
    }

    private void saltarEspacios() {
        while (posicion < texto.length() && Character.isWhitespace(texto.charAt(posicion))) {
            posicion++;
        }
    }

    private boolean consumir(char esperado) {
        if (posicion < texto.length() && texto.charAt(posicion) == esperado) {
            posicion++;
            return true;
        }
        return false;
    }
}