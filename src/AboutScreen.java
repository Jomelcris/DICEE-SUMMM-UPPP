import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class AboutScreen extends JPanel {

    private static final String BG_PATH       = "assets/backgrounds/background_about.png";
    private static final String BTN_BACK_PATH = "assets/buttons/btn_back.png";


    private static final int BTN_WIDTH  = 250;
    private static final int BTN_HEIGHT = 55;



    private final GameWindow gameWindow;

    private BufferedImage bgImage;
    private BufferedImage btnBackImg;

    private Rectangle backRect = new Rectangle();
    private boolean hoverBack  = false;

    public AboutScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        loadImages();
        addMouseListeners();
    }

    private void loadImages() {
        bgImage    = loadImage(BG_PATH);
        btnBackImg = loadImage(BTN_BACK_PATH);
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

        // Draw background only
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, w, h);
        }

        // Back button — upper left
        updateRect(backRect, 20, 20);
        drawButton(g2, btnBackImg, backRect, hoverBack, "BACK");
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
            g2.setFont(new Font("Arial", Font.BOLD, 18));
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
                hoverBack = backRect.contains(e.getPoint());
                setCursor(hoverBack
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (backRect.contains(e.getPoint())) {
                    onBackClicked();
                }
            }
        });
    }

    private void onBackClicked() {
        gameWindow.switchScreen(new HomeScreen(gameWindow));
    }

    private void updateRect(Rectangle r, int x, int y) {
        r.setBounds(x, y, BTN_WIDTH, BTN_HEIGHT);
    }

    private Rectangle expandRect(Rectangle r, int amount) {
        return new Rectangle(r.x - amount, r.y - amount,
                r.width + amount * 2, r.height + amount * 2);
    }
}