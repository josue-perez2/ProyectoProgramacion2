import vista.DashboardView;
import vista.util.TemaGestor;

public class Main {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TemaGestor.iniciarTema();
            DashboardView view = new DashboardView();
            view.setVisible(true);
        });
    }
}
 
