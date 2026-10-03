package util;

import model.Categorias;
import service.CategoriaService;

import java.util.List;

public class CategoriasItem {

    public static final int BEBIDA = 38;
    public static final int PAN = 33;
    public static final int RICITO = 39;

    private CategoriasItem() {
    }

    public static int buscarIdPorNombre(String palabraClave, int idPorDefecto) {
        try {
            List<Categorias> categorias = new CategoriaService().listar();
            for (Categorias c : categorias) {
                if (c.getNombreCat() != null && c.getNombreCat().toLowerCase().contains(palabraClave.toLowerCase())) {
                    return c.getIdCat();
                }
            }
        } catch (Exception ex) {
            return idPorDefecto;
        }
        return idPorDefecto;
    }

    public static int obtenerIdPan() {
        return buscarIdPorNombre("Pan", PAN);
    }

    public static int obtenerIdBebida() {
        return buscarIdPorNombre("Bebida", BEBIDA);
    }

    public static int obtenerIdRicito() {
        int id = buscarIdPorNombre("Acompañamiento", RICITO);
        if (id == RICITO) {
            id = buscarIdPorNombre("Snack", RICITO);
        }
        return id;
    }
}
