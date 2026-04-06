public class Main {
    public static void main(String[] args) {
        // Launch the game window on the Swing Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);

        });
    }
}