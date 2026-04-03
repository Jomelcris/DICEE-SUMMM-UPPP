import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
public class HomeScreen extends JPanel {

    private static final String BG_PATH        = "assets/backgrounds/background.png";
    private static final String BTN_PLAY_PATH  = "assets/buttons/btn_play.png";
    private static final String BTN_SET_PATH   = "assets/buttons/btn_settings.png";
    private static final String BTN_QUIT_PATH  = "assets/buttons/btn_quit.png";
    private static final String BTN_ABOUT_PATH = "assets/buttons/btn_about.png";

    private static final double BTN_CENTER_X = 0.49;
    private static final double PLAY_Y       = 0.45;
    private static final double SETTINGS_Y   = 0.55;
    private static final double ABOUT_Y      = 0.65;
    private static final double QUIT_Y       = 0.75;

    private static final int BTN_WIDTH  = 250;
    private static final int BTN_HEIGHT = 60;

    private final GameWindow gameWindow;

    private BufferedImage bgImage;
    private BufferedImage btnPlayImg;
    private BufferedImage btnSettingsImg;
    private BufferedImage btnAboutImg;
    private BufferedImage btnQuitImg;

    private Rectangle playRect     = new Rectangle();
    private Rectangle settingsRect = new Rectangle();
    private Rectangle quitRect     = new Rectangle();
    private Rectangle aboutRect    = new Rectangle();

    private boolean hoverPlay     = false;
    private boolean hoverSettings = false;
    private boolean hoverAbout    = false;
    private boolean hoverQuit     = false;

    public HomeScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        loadImages();
        addMouseListeners();
    }

    private void loadImages() {
        bgImage        = loadImage(BG_PATH);
        btnPlayImg     = loadImage(BTN_PLAY_PATH);
        btnSettingsImg = loadImage(BTN_SET_PATH);
        btnQuitImg     = loadImage(BTN_QUIT_PATH);
        btnAboutImg = loadImage(BTN_ABOUT_PATH);

    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load image: " + path);
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, w, h);
        }

        int bx = (int)(w * BTN_CENTER_X) - BTN_WIDTH / 2;
        updateRect(playRect,     bx, (int)(h * PLAY_Y));
        updateRect(settingsRect, bx, (int)(h * SETTINGS_Y));
        updateRect(aboutRect,    bx, (int)(h * ABOUT_Y));
        updateRect(quitRect,     bx, (int)(h * QUIT_Y));

        drawButton(g2, btnPlayImg,     playRect,     hoverPlay,     "LET'S PLAY");
        drawButton(g2, btnSettingsImg, settingsRect, hoverSettings, "SETTINGS");
        drawButton(g2, btnAboutImg,    aboutRect,    hoverAbout,    "ABOUT");
        drawButton(g2, btnQuitImg,     quitRect,     hoverQuit,     "EXIT");
    }

    private void drawButton(Graphics2D g2, BufferedImage img,
                            Rectangle rect, boolean hover, String label) {
        Rectangle r = hover ? expandRect(rect, 4) : rect;

        if (img != null) {
            if (!hover) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            }
            g2.drawImage(img, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(200, 200, 255) : new Color(150, 150, 220));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            int tx = r.x + (r.width  - fm.stringWidth(label)) / 2;
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(label, tx, ty);
        }
    }

    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverPlay     = playRect.contains(e.getPoint());
                hoverSettings = settingsRect.contains(e.getPoint());
                hoverAbout    = aboutRect.contains(e.getPoint());
                hoverQuit     = quitRect.contains(e.getPoint());

                if (hoverPlay || hoverSettings || hoverAbout || hoverQuit){
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (playRect.contains(e.getPoint())) {
                    onPlayClicked();
                } else if (settingsRect.contains(e.getPoint())) {
                    onSettingsClicked();
                } else if (aboutRect.contains(e.getPoint())) {
                    onAboutClicked();
                } else if (quitRect.contains(e.getPoint())) {
                    onQuitClicked();
                }
            }
        });
    }

    private void onPlayClicked() {
        gameWindow.switchScreen(new GameModeScreen(gameWindow));
    }

    private void onSettingsClicked() {
        gameWindow.switchScreen(new SettingsScreen(gameWindow));
    }

    private void onAboutClicked() {
        gameWindow.switchScreen(new AboutScreen(gameWindow));
    }

    private void onQuitClicked() {
        System.exit(0);
    }

    private void updateRect(Rectangle r, int x, int y) {
        r.setBounds(x, y, BTN_WIDTH, BTN_HEIGHT);
    }

    private Rectangle expandRect(Rectangle r, int amount) {
        return new Rectangle(r.x - amount, r.y - amount,
                r.width + amount * 2, r.height + amount * 2);
    }
}