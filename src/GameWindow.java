import javax.swing.*;
import java.awt.Dimension;

public class GameWindow extends JFrame {

    private static final int DEFAULT_WIDTH  = 1080;
    private static final int DEFAULT_HEIGHT = 700;

    private HomeScreen homeScreen;

    public GameWindow() {
        setTitle("My Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setMaximumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setResizable(false);
        setLocationRelativeTo(null);

        homeScreen = new HomeScreen(this);
        setContentPane(homeScreen);
    }

    public void switchScreen(JPanel newScreen) {
        setContentPane(newScreen);
        revalidate();
        repaint();
    }
}