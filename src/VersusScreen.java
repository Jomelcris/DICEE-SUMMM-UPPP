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
            "assets/characters/portraits/echo.gif",
            "assets/characters/portraits/zyah.gif",
            "assets/characters/portraits/raze.gif",
            "assets/characters/portraits/vibe.gif",
            "assets/characters/portraits/torque.gif",
            "assets/characters/portraits/luma.gif",
            "assets/characters/portraits/lyric.gif",
            "assets/characters/portraits/ayo.gif"
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // ── Background (full screen, no white panel) ──────────────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g2.setColor(new Color(30, 20, 10));
            g2.fillRect(0, 0, w, h);
        }

        // ── Dark overlay to make text/sprites pop ─────────────────────────────
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(0, 0, w, h);

        // ── Green name bar at top center ──────────────────────────────────────
        int barW = (int)(w * 0.55);
        int barH = 50;
        int barX = (w - barW) / 2;
        int barY = (int)(h * 0.08);

        g2.setColor(new Color(30, 80, 30));
        g2.fillRoundRect(barX, barY, barW, barH, 20, 20);
        g2.setColor(new Color(80, 140, 60));
        g2.fillRoundRect(barX + 3, barY + 3, barW - 6, barH - 6, 16, 16);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        FontMetrics fmBar = g2.getFontMetrics();
        String barText = CHARACTERS[p1Index][0] + " VS " + CHARACTERS[p2Index][0];
        g2.drawString(barText,
                barX + (barW - fmBar.stringWidth(barText)) / 2,
                barY + (barH + fmBar.getAscent() - fmBar.getDescent()) / 2);

        // ── Sprite positions — large, facing each other ───────────────────────
        int spriteW = 220;
        int spriteH = 280;
        int groundY = (int)(h * 0.82); // bottom of sprite

        int p1SpriteX = (int)(w * 0.15);  // left side
        int p2SpriteX = (int)(w * 0.65);  // right side
        int spriteY   = groundY - spriteH;

        // P1 — faces right (normal)
        if (sprites[p1Index] != null) {
            g2.drawImage(sprites[p1Index], p1SpriteX, spriteY, spriteW, spriteH, null);
        } else {
            g2.setColor(new Color(80, 140, 255, 180));
            g2.fillRoundRect(p1SpriteX, spriteY, spriteW, spriteH, 16, 16);
        }

        // P2 — flipped to face left (toward P1)
        if (sprites[p2Index] != null) {
            g2.drawImage(sprites[p2Index], p2SpriteX + spriteW, spriteY, -spriteW, spriteH, null);
        } else {
            g2.setColor(new Color(220, 80, 80, 180));
            g2.fillRoundRect(p2SpriteX, spriteY, spriteW, spriteH, 16, 16);
        }

        // ── VS label centered between sprites ────────────────────────────────
        int vsW = 130, vsH = 70;
        int vsX = (w - vsW) / 2;
        int vsY = spriteY + (spriteH / 2) - (vsH / 2);

        if (vsLabelImage != null) {
            g2.drawImage(vsLabelImage, vsX, vsY, vsW, vsH, null);
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 56));
            g2.setColor(new Color(200, 30, 30));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("VS", (w - fm.stringWidth("VS")) / 2, vsY + fm.getAscent());
        }

        // ── Player name labels below each sprite ──────────────────────────────
        int nameY = groundY + 20;
        g2.setFont(new Font("Arial", Font.BOLD, 18));

        // P1 label
        g2.setColor(new Color(120, 180, 255));
        FontMetrics fm = g2.getFontMetrics();
        String p1Name = p1Label + " — " + CHARACTERS[p1Index][0];
        g2.drawString(p1Name, p1SpriteX + (spriteW - fm.stringWidth(p1Name)) / 2, nameY);

        // P2 label
        g2.setColor(new Color(255, 120, 120));
        String p2Name = p2Label + " — " + CHARACTERS[p2Index][0];
        g2.drawString(p2Name, p2SpriteX + (spriteW - fm.stringWidth(p2Name)) / 2, nameY);
    }
}