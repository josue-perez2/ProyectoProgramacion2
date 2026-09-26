package model;

public class Recompensas {
    private int idRec;
    private String nombreRec;
    private int puntosRequeridosRec;
    private String tipoItemRec;
    private String activoRec;

    public Recompensas() {
    }

    public Recompensas(int idRec, String nombreRec, int puntosRequeridosRec, String tipoItemRec, String activoRec) {
        this.idRec = idRec;
        this.nombreRec = nombreRec;
        this.puntosRequeridosRec = puntosRequeridosRec;
        this.tipoItemRec = tipoItemRec;
        this.activoRec = activoRec;
    }

    public int getIdRec() {
        return idRec;
    }

    public void setIdRec(int idRec) {
        this.idRec = idRec;
    }

    public String getNombreRec() {
        return nombreRec;
    }

    public void setNombreRec(String nombreRec) {
        this.nombreRec = nombreRec;
    }

    public int getPuntosRequeridosRec() {
        return puntosRequeridosRec;
    }

    public void setPuntosRequeridosRec(int puntosRequeridosRec) {
        this.puntosRequeridosRec = puntosRequeridosRec;
    }

    public String getTipoItemRec() {
        return tipoItemRec;
    }

    public void setTipoItemRec(String tipoItemRec) {
        this.tipoItemRec = tipoItemRec;
    }

    public String getActivoRec() {
        return activoRec;
    }

    public void setActivoRec(String activoRec) {
        this.activoRec = activoRec;
    }
}
