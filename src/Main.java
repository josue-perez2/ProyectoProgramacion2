import vista.ClienteView;

public class Main {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ClienteView view = new ClienteView();
            view.setVisible(true);
        });
    }
}