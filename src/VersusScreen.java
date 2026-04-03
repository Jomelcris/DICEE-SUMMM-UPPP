import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class VersusScreen extends JPanel {

    private static final String BG_PATH        = "assets/backgrounds/versus_background.png";
    private static final String PANEL_PATH     = "assets/ui/versus_panel.png";
    private static final String VS_LABEL_PATH  = "assets/ui/label_vs.png";

    private static final String[] SPRITE_FILES = {
            "assets/characters/sprites/s_echo.png",
            "assets/characters/sprites/s_zyah.png",
            "assets/characters/sprites/s_raze.png",
            "assets/characters/sprites/s_vibe.png",
            "assets/characters/sprites/s_torque.png",
            "assets/characters/sprites/s_luma.png",
            "assets/characters/sprites/s_lyric.png",
            "assets/characters/sprites/s_ayo.png"
    };

    private static final String[][] CHARACTERS = {
            { "Echo",   "Assassin" },
            { "Zyah",   "Assassin" },
            { "Raze",   "Fighter"  },
            { "Vibe",   "Fighter"  },
            { "Torque", "Tank"     },
            { "Luma",   "Tank"     },
            { "Lyric",  "Support"  },
            { "Ayo",    "Support"  },
    };

    private static final int SPRITE_WIDTH  = 150;
    private static final int SPRITE_HEIGHT = 200;
    private static final int AUTO_PROCEED_DELAY = 3000; // 3 seconds

    private final GameWindow gameWindow;
    private final int        p1Index;
    private final int        p2Index;
    private final String     gameMode;
    private final String     p1Label;
    private final String     p2Label;

    private BufferedImage bgImage;
    private BufferedImage panelImage;
    private BufferedImage vsLabelImage;
    private BufferedImage[] sprites = new BufferedImage[8];

    public VersusScreen(GameWindow gameWindow, int p1Index, int p2Index,
                        String gameMode, String p1Label, String p2Label) {
        this.gameWindow = gameWindow;
        this.p1Index    = p1Index;
        this.p2Index    = p2Index;
        this.gameMode   = gameMode;
        this.p1Label    = p1Label;
        this.p2Label    = p2Label;
        setLayout(null);
        loadImages();
        startAutoProceed();
    }

    private void loadImages() {
        bgImage       = loadImage(BG_PATH);
        panelImage    = loadImage(PANEL_PATH);
        vsLabelImage  = loadImage(VS_LABEL_PATH);
        for (int i = 0; i < 8; i++) {
            sprites[i] = loadImage(SPRITE_FILES[i]);
        }
    }

    private BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) {
            System.err.println("Could not load image: " + path);
            return null;
        }
    }

    private void startAutoProceed() {
        Timer timer = new Timer(AUTO_PROCEED_DELAY, e ->
                gameWindow.switchScreen(
                        new BattleScreen(gameWindow, p1Index, p2Index, gameMode)));
        timer.setRepeats(false);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // ── Background ────────────────────────────────────────────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g2.setColor(new Color(100, 180, 220));
            g2.fillRect(0, 0, w, h);
        }

        // ── Center panel ──────────────────────────────────────────────────────
        int panelW = (int)(w * 0.75);
        int panelH = (int)(h * 0.65);
        int panelX = (w - panelW) / 2;
        int panelY = (h - panelH) / 2;

        if (panelImage != null) {
            g2.drawImage(panelImage, panelX, panelY, panelW, panelH, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(panelX, panelY, panelW, panelH, 30, 30);
        }

        // ── Green bar at top of panel ─────────────────────────────────────────
        int barW = (int)(panelW * 0.80);
        int barH = 45;
        int barX = panelX + (panelW - barW) / 2;
        int barY = panelY + 15;

// Outer dark green border
        g2.setColor(new Color(30, 80, 30));
        g2.fillRoundRect(barX, barY, barW, barH, 20, 20);

// Inner lighter green fill
        g2.setColor(new Color(80, 140, 60));
        g2.fillRoundRect(barX + 3, barY + 3, barW - 6, barH - 6, 16, 16);

// Text inside the bar
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(Color.WHITE);
        FontMetrics fmBar = g2.getFontMetrics();
        String barText = CHARACTERS[p1Index][0] + " VS " + CHARACTERS[p2Index][0];
        g2.drawString(barText,
                barX + (barW - fmBar.stringWidth(barText)) / 2,
                barY + (barH + fmBar.getAscent() - fmBar.getDescent()) / 2);

        // ── Sprite area ───────────────────────────────────────────────────────
        int spriteY = panelY + barH + 40;

        // P1 sprite — left side
        int p1SpriteX = panelX + (panelW / 4) - (SPRITE_WIDTH / 2);
        if (sprites[p1Index] != null) {
            g2.drawImage(sprites[p1Index], p1SpriteX, spriteY,
                    SPRITE_WIDTH, SPRITE_HEIGHT, null);
        } else {
            g2.setColor(new Color(80, 140, 255, 100));
            g2.fillRect(p1SpriteX, spriteY, SPRITE_WIDTH, SPRITE_HEIGHT);
        }

        // P2 sprite — right side
        int p2SpriteX = panelX + (panelW * 3 / 4) - (SPRITE_WIDTH / 2);
        if (sprites[p2Index] != null) {
            // Flip P2 sprite horizontally so they face each other
            g2.drawImage(sprites[p2Index],
                    p2SpriteX + SPRITE_WIDTH, spriteY,
                    -SPRITE_WIDTH, SPRITE_HEIGHT, null);
        } else {
            g2.setColor(new Color(220, 80, 80, 100));
            g2.fillRect(p2SpriteX, spriteY, SPRITE_WIDTH, SPRITE_HEIGHT);
        }

        // ── VS label in the center ────────────────────────────────────────────
        int vsW = 120;
        int vsH = 60;
        int vsX = (w - vsW) / 2;
        int vsY = spriteY + (SPRITE_HEIGHT / 2) - (vsH / 2);

        if (vsLabelImage != null) {
            g2.drawImage(vsLabelImage, vsX, vsY, vsW, vsH, null);
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            g2.setColor(new Color(150, 20, 20));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("VS", (w - fm.stringWidth("VS")) / 2,
                    vsY + fm.getAscent());
        }

        // ── Player name labels below sprites ──────────────────────────────────
        int nameY = spriteY + SPRITE_HEIGHT + 25;

        // P1 name
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(new Color(80, 140, 255));
        FontMetrics fm = g2.getFontMetrics();
        String p1Name = p1Label + " - " + CHARACTERS[p1Index][0];
        g2.drawString(p1Name,
                p1SpriteX + (SPRITE_WIDTH - fm.stringWidth(p1Name)) / 2, nameY);

        // P2 name
        g2.setColor(new Color(220, 80, 80));
        String p2Name = p2Label + " - " + CHARACTERS[p2Index][0];
        g2.drawString(p2Name,
                p2SpriteX + (SPRITE_WIDTH - fm.stringWidth(p2Name)) / 2, nameY);
    }
}