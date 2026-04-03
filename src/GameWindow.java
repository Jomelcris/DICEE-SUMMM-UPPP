import javax.swing.*;

public class GameWindow extends JFrame {

    // ─── Change these to resize the default window ───────────────────────────
    private static final int DEFAULT_WIDTH  = 1280;
    private static final int DEFAULT_HEIGHT = 720;
    // ─────────────────────────────────────────────────────────────────────────

    private HomeScreen homeScreen;

    public GameWindow() {
        setTitle("My Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setResizable(true);          // window can be freely resized
        setLocationRelativeTo(null); // center on screen

        // Start on the HomeScreen
        homeScreen = new HomeScreen(this);
        setContentPane(homeScreen);
    }

    /**
     * Call this to swap the current screen for a different JPanel.
     * Example: switchScreen(new GameScreen(this));
     */
    public void switchScreen(JPanel newScreen) {
        setContentPane(newScreen);
        revalidate();
        repaint();
    }
}