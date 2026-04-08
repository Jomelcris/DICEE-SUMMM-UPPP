import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class AboutScreen extends JPanel {

    private static final String BG_PATH_1     = "assets/backgrounds/background_aboutt.png";
    private static final String BG_PATH_2     = "assets/backgrounds/background_aboutt1.png"; // your 2nd image
    private static final String BTN_BACK_PATH = "assets/buttons/btn_back.png";
    private static final String ARROW_RIGHT_PATH = "assets/ui/arrow_right.png";
    private static final String ARROW_LEFT_PATH  = "assets/ui/arrow_left.png";

    private static final int BTN_WIDTH  = 250;
    private static final int BTN_HEIGHT = 55;
    private static final int ARROW_SIZE = 80;

    private final GameWindow gameWindow;

    private BufferedImage bgImage1;
    private BufferedImage bgImage2;
    private BufferedImage btnBackImg;
    private BufferedImage arrowRightImg;
    private BufferedImage arrowLeftImg;

    private int currentPage = 1; // 1 or 2

    private Rectangle backRect       = new Rectangle();
    private Rectangle arrowRightRect = new Rectangle();
    private Rectangle arrowLeftRect  = new Rectangle();

    private boolean hoverBack  = false;
    private boolean hoverRight = false;
    private boolean hoverLeft  = false;

    public AboutScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setLayout(null);
        loadImages();
        addMouseListeners();
    }

    private void loadImages() {
        bgImage1      = loadImage(BG_PATH_1);
        bgImage2      = loadImage(BG_PATH_2);
        btnBackImg    = loadImage(BTN_BACK_PATH);
        arrowRightImg = loadImage(ARROW_RIGHT_PATH);
        arrowLeftImg  = loadImage(ARROW_LEFT_PATH);
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

        // Draw current page background
        BufferedImage currentBg = (currentPage == 1) ? bgImage1 : bgImage2;
        if (currentBg != null) {
            g2.drawImage(currentBg, 0, 0, w, h, null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, w, h);
        }

        // Back button — upper left
        backRect.setBounds(20, 20, BTN_WIDTH - 100, BTN_HEIGHT - 10);
        drawButton(g2, btnBackImg, backRect, hoverBack, "BACK");

        // Page indicator dots — bottom center
        drawPageDots(g2, w, h);

        // Arrow RIGHT — only on page 1
        if (currentPage == 1) {
            int arrowY = (h - ARROW_SIZE) / 2;
            arrowRightRect.setBounds(w - ARROW_SIZE - 20, arrowY, ARROW_SIZE, ARROW_SIZE);
            drawArrow(g2, arrowRightImg, arrowRightRect, hoverRight, ">");
        } else {
            arrowRightRect.setBounds(0, 0, 0, 0); // disable
        }

        // Arrow LEFT — only on page 2
        if (currentPage == 2) {
            int arrowY = (h - ARROW_SIZE) / 2;
            arrowLeftRect.setBounds(20, arrowY, ARROW_SIZE, ARROW_SIZE);
            drawArrow(g2, arrowLeftImg, arrowLeftRect, hoverLeft, "<");
        } else {
            arrowLeftRect.setBounds(0, 0, 0, 0); // disable
        }
    }

    // ── Page indicator dots ───────────────────────────────────────────────────
    private void drawPageDots(Graphics2D g2, int w, int h) {
        int dotSize   = 12;
        int dotSpacing = 20;
        int totalW    = (2 * dotSize) + dotSpacing;
        int startX    = (w - totalW) / 2;
        int dotY      = h - 30;

        for (int i = 1; i <= 2; i++) {
            int dotX = startX + (i - 1) * (dotSize + dotSpacing);
            if (i == currentPage) {
                g2.setColor(Color.WHITE);          // active dot
            } else {
                g2.setColor(new Color(255, 255, 255, 100)); // inactive dot
            }
            g2.fillOval(dotX, dotY, dotSize, dotSize);
        }
    }

    // ── Button drawing ────────────────────────────────────────────────────────
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

    // ── Arrow drawing ─────────────────────────────────────────────────────────
    private void drawArrow(Graphics2D g2, BufferedImage img,
                           Rectangle rect, boolean hover, String fallback) {
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    hover ? 1.0f : 0.75f));
            g2.drawImage(img, rect.x + (hover ? -2 : 0), rect.y + (hover ? -2 : 0),
                    rect.width, rect.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(255, 200, 80) : new Color(180, 140, 50));
            g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 12, 12);
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(fallback,
                    rect.x + (rect.width  - fm.stringWidth(fallback)) / 2,
                    rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    // ── Mouse listeners ───────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverBack  = backRect.contains(e.getPoint());
                hoverRight = arrowRightRect.contains(e.getPoint());
                hoverLeft  = arrowLeftRect.contains(e.getPoint());
                setCursor((hoverBack || hoverRight || hoverLeft)
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
                } else if (arrowRightRect.contains(e.getPoint()) && currentPage == 1) {
                    currentPage = 2;
                    repaint();
                } else if (arrowLeftRect.contains(e.getPoint()) && currentPage == 2) {
                    currentPage = 1;
                    repaint();
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