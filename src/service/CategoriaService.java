package service;

import dao.CategoriaDao;
import dao.Impl.CategoriasDaoImpl;
import model.Categorias;
import model.Cliente;

import java.util.List;

public class CategoriaService {
    private final CategoriaDao categoriaDao;

    public CategoriaService() { this.categoriaDao = new CategoriasDaoImpl();}

    public List<Categorias> listar() {
        return categoriaDao.listar();
    }

    public void insertar(Categorias categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        String nombre = categoria.getNombreCat() != null ? categoria.getNombreCat().trim() : "";
        for (Categorias c : listar()) {
            if (c.getNombreCat() != null && c.getNombreCat().equalsIgnoreCase(nombre)) {
                throw new IllegalStateException("Ya existe una categoría con el nombre '" + nombre + "'.");
            }
        }
        categoriaDao.insertar(categoria);
    }

    public void actualizar(Categorias categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        boolean existe = false;
        String nombre = categoria.getNombreCat() != null ? categoria.getNombreCat().trim() : "";
        for (Categorias c : listar()) {
            if (c.getIdCat() == categoria.getIdCat()) {
                existe = true;
            } else if (c.getNombreCat() != null && c.getNombreCat().equalsIgnoreCase(nombre)) {
                throw new IllegalStateException("El nombre '" + nombre + "' ya está en uso por otra categoría.");
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede modificar: la categoría con ID " + categoria.getIdCat() + " no existe.");
        }
        categoriaDao.actualizar(categoria);
    }

    public void eliminar(int id) {
        boolean existe = false;
        for (Categorias c : listar()) {
            if (c.getIdCat() == id) {
                existe = true;
                break;
            }
        }
        if (!existe) {
            throw new IllegalStateException("No se puede eliminar: la categoría con ID " + id + " no existe.");
        }
        categoriaDao.eliminar(id);
    }
}
