package model.reportes;

public class PuntosClienteDTO {
    private int idCli;
    private String nombreCli;
    private String dpiCli;
    private int saldoPuntoCli;

    public PuntosClienteDTO() {
    }

    public PuntosClienteDTO(int idCli, String nombreCli, String dpiCli, int saldoPuntoCli) {
        this.idCli = idCli;
        this.nombreCli = nombreCli;
        this.dpiCli = dpiCli;
        this.saldoPuntoCli = saldoPuntoCli;
    }

    public int getIdCli() {
        return idCli;
    }

    public void setIdCli(int idCli) {
        this.idCli = idCli;
    }

    public String getNombreCli() {
        return nombreCli;
    }

    public void setNombreCli(String nombreCli) {
        this.nombreCli = nombreCli;
    }

    public String getDpiCli() {
        return dpiCli;
    }

    public void setDpiCli(String dpiCli) {
        this.dpiCli = dpiCli;
    }

    public int getSaldoPuntoCli() {
        return saldoPuntoCli;
    }

    public void setSaldoPuntoCli(int saldoPuntoCli) {
        this.saldoPuntoCli = saldoPuntoCli;
    }
}