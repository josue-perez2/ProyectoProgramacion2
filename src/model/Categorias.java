package model;

public class Categorias {
    private int idCat;
    private String nombreCat;

    public Categorias() {
    }

    public Categorias(int idCat, String nombreCat) {
        this.idCat = idCat;
        this.nombreCat = nombreCat;
    }

    public int getIdCat() {
        return idCat;
    }

    public void setIdCat(int idCat) {
        this.idCat = idCat;
    }

    public String getNombreCat() {
        return nombreCat;
    }

    public void setNombreCat(String nombreCat) {
        this.nombreCat = nombreCat;
    }
}
