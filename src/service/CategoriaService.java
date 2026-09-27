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
        categoriaDao.insertar(categoria);
    }

    public void actualizar(Categorias categoria) {
        categoriaDao.actualizar(categoria);
    }

    public void eliminar(int id) {
        categoriaDao.eliminar(id);
    }


}
