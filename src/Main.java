import vista.DashboardView;

public class Main {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            DashboardView view = new DashboardView();
            view.setVisible(true);
        });
    }
}
