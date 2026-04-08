import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CharacterSelectionScreen extends JPanel {

    private static final String BG_PATH          = "assets/backgrounds/background_charselectt.png";
    private static final String BTN_CONFIRM_PATH = "assets/buttons/btn_confirm.png";
    private static final String BTN_BACK_PATH    = "assets/buttons/btn_back.png";
    private static final String ARROW_LEFT_PATH  = "assets/ui/arrow_left.png";
    private static final String ARROW_RIGHT_PATH = "assets/ui/arrow_right.png";
    private static final String CARD_BG_PATH     = "assets/ui/card_bg.png";
    private static final String PLAYER1_IMG_PATH = "assets/ui/player1.png";
    private static final String PLAYER2_IMG_PATH = "assets/ui/player2.png";
    private static final String PLAYER_IMG_PATH  = "assets/ui/player.png";
    private static final String COMPUTER_IMG_PATH= "assets/ui/computer.png";

    private static final int BTN_WIDTH   = 250;
    private static final int BTN_HEIGHT  = 55;
    private static final int ARROW_SIZE  = 80;
    private static final int CARD_WIDTH  = 320;
    private static final int CARD_HEIGHT = 420;
    private static final int PORTRAIT_W  = 280;
    private static final int PORTRAIT_H  = 200;
    private static final int BADGE_W     = 80;
    private static final int BADGE_H     = 40;

    private static final String[][] CHARACTERS = {
            { "Echo",   "Assassin", "Osaka, Japan",       "80",  "4.0×", "Phantom Dance – Dodge next 2 attacks (CD: 5 turns)"         },
            { "Zyah",   "Assassin", "Kingston, Jamaica",  "80",  "4.0×", "Dancehall Fever – Guaranteed extra turn (CD: 4 turns)"      },
            { "Raze",   "Fighter",  "Seoul, South Korea", "115", "3.0×", "Blazing Combo – +8 damage for 3 attacks (CD: 5 turns)"      },
            { "Vibe",   "Fighter",  "Milan, Italy",       "110", "3.0×", "House Foundation – 50% dmg reduction 2 turns (CD: 4 turns)" },
            { "Torque", "Tank",     "Los Angeles, USA",   "150", "2.0×", "Earthquake Stomp – Stun enemy 2 turns (CD: 4 turns)"        },
            { "Luma",   "Tank",     "São Paulo, Brazil",  "140", "2.0×", "Radiant Burst – Heal 30 HP (CD: 4 turns)"                   },
            { "Lyric",  "Support",  "Paris, France",      "105", "2.0×", "Healing Freestyle – Heal 35 HP (CD: 3 turns)"               },
            { "Ayo",    "Support",  "Lagos, Nigeria",     "100", "2.0×", "Ancestral Call – Revive 50% HP (CD: 5 turns, once/battle)"  },
    };

    private static final String[] PORTRAIT_FILES = {
            "assets/characters/portraits/echo.gif",
            "assets/characters/portraits/zyah.gif",
            "assets/characters/portraits/raze.gif",
            "assets/characters/portraits/vibee.gif",
            "assets/characters/portraits/torque.gif",
            "assets/characters/portraits/luma.gif",
            "assets/characters/portraits/lyric.gif",
            "assets/characters/portraits/ayo.gif"
    };

    private static final Color COLOR_ASSASSIN = new Color(180, 50,  50);
    private static final Color COLOR_FIGHTER  = new Color(200, 120, 30);
    private static final Color COLOR_TANK     = new Color(50,  100, 200);
    private static final Color COLOR_SUPPORT  = new Color(50,  180, 100);

    private final GameWindow gameWindow;
    private final String     gameMode;

    private int  currentPlayer = 1;
    private int  p1Choice      = -1;
    private int  currentIndex  = 0;

    // ── Assets ────────────────────────────────────────────────────────────────
    private BufferedImage bgImage;
    private BufferedImage btnConfirmImg;
    private BufferedImage btnBackImg;
    private BufferedImage arrowLeftImg;
    private BufferedImage arrowRightImg;
    private BufferedImage cardBgImg;
    private BufferedImage player1Img;
    private BufferedImage player2Img;
    private BufferedImage playerImg;
    private BufferedImage computerImg;
    private BufferedImage[] portraits = new BufferedImage[8];
    private Image[]         portraitGifs = new Image[8];

    // ── Rectangles ────────────────────────────────────────────────────────────
    private Rectangle arrowLeftRect  = new Rectangle();
    private Rectangle arrowRightRect = new Rectangle();
    private Rectangle confirmRect    = new Rectangle();
    private Rectangle backRect       = new Rectangle();

    // ── Hover states ──────────────────────────────────────────────────────────
    private boolean hoverLeft    = false;
    private boolean hoverRight   = false;
    private boolean hoverConfirm = false;
    private boolean hoverBack    = false;

    public CharacterSelectionScreen(GameWindow gameWindow, String gameMode) {
        this.gameWindow = gameWindow;
        this.gameMode   = gameMode;
        setLayout(null);
        loadImages();
        addMouseListeners();
    }

    // ── Image loading ─────────────────────────────────────────────────────────
    private void loadImages() {
        bgImage       = loadImage(BG_PATH);
        btnConfirmImg = loadImage(BTN_CONFIRM_PATH);
        btnBackImg    = loadImage(BTN_BACK_PATH);
        arrowLeftImg  = loadImage(ARROW_LEFT_PATH);
        arrowRightImg = loadImage(ARROW_RIGHT_PATH);
        cardBgImg     = loadImage(CARD_BG_PATH);
        player1Img    = loadImage(PLAYER1_IMG_PATH);
        player2Img    = loadImage(PLAYER2_IMG_PATH);
        playerImg     = loadImage(PLAYER_IMG_PATH);
        computerImg   = loadImage(COMPUTER_IMG_PATH);
        for (int i = 0; i < 8; i++) {
            File f = new File(PORTRAIT_FILES[i]);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(PORTRAIT_FILES[i]);
                portraitGifs[i] = icon.getImage();
            } else {
                portraits[i] = loadImage(PORTRAIT_FILES[i]);
            }
        }
    }

    private BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { System.err.println("Could not load: " + path); return null; }
    }

    // ── Painting ──────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();

        // ── Background ────────────────────────────────────────────────────────
        if (bgImage != null) g2.drawImage(bgImage, 0, 0, w, h, null);
        else { g2.setColor(new Color(20, 15, 10)); g2.fillRect(0, 0, w, h); }

        // ── Card position (centered) ──────────────────────────────────────────
        int cardX = (w - CARD_WIDTH)  / 2;
        int cardY = (h - CARD_HEIGHT) / 2;

        // ── P1 / P2 badge above card ──────────────────────────────────────────
        int badgeX = cardX + (CARD_WIDTH - BADGE_W) / 2;
        int badgeY = cardY - BADGE_H - 8;
        drawPlayerBadge(g2, badgeX, badgeY);

        // ── Card ──────────────────────────────────────────────────────────────
        drawCard(g2, currentIndex, cardX, cardY);

        // ── Left arrow ────────────────────────────────────────────────────────
        int arrowY = cardY + (CARD_HEIGHT - ARROW_SIZE) / 2;
        arrowLeftRect.setBounds(cardX - ARROW_SIZE - 20, arrowY, ARROW_SIZE, ARROW_SIZE);
        drawArrow(g2, arrowLeftImg, arrowLeftRect, hoverLeft, "<");

        // ── Right arrow ───────────────────────────────────────────────────────
        arrowRightRect.setBounds(cardX + CARD_WIDTH + 20, arrowY, ARROW_SIZE, ARROW_SIZE);
        drawArrow(g2, arrowRightImg, arrowRightRect, hoverRight, ">");

        // ── Page counter ──────────────────────────────────────────────────────
        String counter = (currentIndex + 1) + " / " + CHARACTERS.length;
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(220, 220, 220));
        FontMetrics fmC = g2.getFontMetrics();
        g2.drawString(counter, (w - fmC.stringWidth(counter)) / 2,
                cardY + CARD_HEIGHT + 18);

        // ── Confirm button ────────────────────────────────────────────────────
        confirmRect.setBounds((w - BTN_WIDTH) / 2,
                cardY + CARD_HEIGHT + 30, BTN_WIDTH, BTN_HEIGHT);
        drawButton(g2, btnConfirmImg, confirmRect, hoverConfirm, "CONFIRM");

        // ── Back button ───────────────────────────────────────────────────────
        backRect.setBounds(20, 20, 150, 45);
        drawButton(g2, btnBackImg, backRect, hoverBack, "BACK");

        // ── P1 already chose reminder ─────────────────────────────────────────
        if (currentPlayer == 2) {
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.setColor(new Color(80, 140, 255));
            g2.drawString("P1 chose: " + CHARACTERS[p1Choice][0], 20, h - 20);
        }
    }

    // ── Player badge (P1 / P2 pixel label above card) ─────────────────────────
    private void drawPlayerBadge(Graphics2D g2, int x, int y) {
        BufferedImage badge = null;

        if (gameMode.equals("PVP")) {
            badge = (currentPlayer == 1) ? player1Img : player2Img;
        } else if (gameMode.equals("PVC")) {
            badge = (currentPlayer == 1) ? playerImg : computerImg;
        } else {
            badge = playerImg; // ARCADE
        }

        if (badge != null) {
            g2.drawImage(badge, x, y, BADGE_W, BADGE_H, null);
        } else {
            // Fallback — draw pixel-style P1/P2 text badge
            String label = (currentPlayer == 1) ? "P1" : "P2";
            Color bg     = (currentPlayer == 1)
                    ? new Color(180, 30, 30)
                    : new Color(30,  80, 180);

            g2.setColor(bg);
            g2.fillRoundRect(x, y, BADGE_W, BADGE_H, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label,
                    x + (BADGE_W - fm.stringWidth(label)) / 2,
                    y + (BADGE_H + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    // ── Card drawing ──────────────────────────────────────────────────────────
    private void drawCard(Graphics2D g2, int index, int x, int y) {
        String[] data      = CHARACTERS[index];
        Color    classColor = getClassColor(data[1]);

        // Card background
        if (cardBgImg != null) {
            g2.drawImage(cardBgImg, x, y, CARD_WIDTH, CARD_HEIGHT, null);
        } else {
            g2.setColor(new Color(220, 200, 170));
            g2.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 16, 16);
            g2.setColor(new Color(140, 100, 60));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 16, 16);
        }

        // ── Portrait area (top portion of card) ───────────────────────────────
        int portraitPad = 10;
        int portraitX   = x + (CARD_WIDTH - PORTRAIT_W) / 2;
        int portraitY   = y + portraitPad;

        // Portrait background (light parchment box)
        g2.setColor(new Color(240, 230, 210));
        g2.fillRect(portraitX, portraitY, PORTRAIT_W, PORTRAIT_H);

        if (portraitGifs[index] != null) {
            // GIF — animated, needs 'this' to keep looping
            g2.drawImage(portraitGifs[index], portraitX, portraitY,
                    PORTRAIT_W, PORTRAIT_H, this);
        } else if (portraits[index] != null) {
            // Static PNG fallback
            g2.drawImage(portraits[index], portraitX, portraitY,
                    PORTRAIT_W, PORTRAIT_H, null);
        } else {
            g2.setColor(new Color(180, 160, 130));
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            String ph = "PORTRAIT";
            g2.drawString(ph,
                    portraitX + (PORTRAIT_W - fm.stringWidth(ph)) / 2,
                    portraitY + PORTRAIT_H / 2 + 5);
        }

        // ── Name header strip (drawn with code) ───────────────────────────────
        int stripY = portraitY + PORTRAIT_H;
        int stripH = 30;

        // Gray gradient strip
        GradientPaint gp = new GradientPaint(
                x, stripY, new Color(160, 160, 160),
                x, stripY + stripH, new Color(110, 110, 110));
        g2.setPaint(gp);
        g2.fillRect(x, stripY, CARD_WIDTH, stripH);

        // Thin highlight line on top of strip
        g2.setColor(new Color(200, 200, 200, 180));
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(x, stripY, x + CARD_WIDTH, stripY);

        // Character name centered in strip
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.WHITE);
        FontMetrics fmStrip = g2.getFontMetrics();
        g2.drawString(data[0],
                x + (CARD_WIDTH - fmStrip.stringWidth(data[0])) / 2,
                stripY + (stripH + fmStrip.getAscent() - fmStrip.getDescent()) / 2);

        // ── Stats area ────────────────────────────────────────────────────────
        int statsY   = stripY + stripH + 14;
        int statX    = x + 20;
        int statLineH = 22;

        // Class badge
        int badgeW = 90, badgeH = 18;
        int badgeX = x + (CARD_WIDTH - badgeW) / 2;
        g2.setColor(classColor);
        g2.fillRoundRect(badgeX, statsY - 14, badgeW, badgeH, 8, 8);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        FontMetrics fmBadge = g2.getFontMetrics();
        String cls = data[1].toUpperCase();
        g2.drawString(cls,
                badgeX + (badgeW - fmBadge.stringWidth(cls)) / 2,
                statsY);
        statsY += 10;

        // Divider line
        g2.setColor(new Color(150, 120, 80, 150));
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(x + 15, statsY, x + CARD_WIDTH - 15, statsY);
        statsY += 14;

        // Stat rows
        drawStatRow(g2, statX, statsY, "Origin",      data[2]); statsY += statLineH;
        drawStatRow(g2, statX, statsY, "Health",      data[3] + " HP"); statsY += statLineH;
        drawStatRow(g2, statX, statsY, "Base Damage", data[4]); statsY += statLineH + 4;

        // Special skill label
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(classColor);
        g2.drawString("Special Skill", statX, statsY);
        statsY += 14;

        // Special skill text (wrapped)
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(60, 40, 20));
        drawWrappedText(g2, data[5], statX, statsY, CARD_WIDTH - 40, 14);
    }

    // ── Stat row ──────────────────────────────────────────────────────────────
    private void drawStatRow(Graphics2D g2, int x, int y, String label, String value) {
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(new Color(80, 60, 30));
        g2.drawString(label + ":", x, y);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(30, 20, 10));
        g2.drawString(value, x + 95, y);
    }

    // ── Wrapped text ──────────────────────────────────────────────────────────
    private void drawWrappedText(Graphics2D g2, String text,
                                 int x, int y, int maxW, int lineH) {
        FontMetrics fm    = g2.getFontMetrics();
        String[]    words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line + (line.length() > 0 ? " " : "") + word;
            if (fm.stringWidth(test) > maxW) {
                g2.drawString(line.toString(), x, y);
                y += lineH;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) g2.drawString(line.toString(), x, y);
    }

    // ── Class color ───────────────────────────────────────────────────────────
    private Color getClassColor(String cls) {
        switch (cls) {
            case "Assassin": return COLOR_ASSASSIN;
            case "Fighter":  return COLOR_FIGHTER;
            case "Tank":     return COLOR_TANK;
            case "Support":  return COLOR_SUPPORT;
            default:         return Color.GRAY;
        }
    }

    // ── Arrow drawing ─────────────────────────────────────────────────────────
    private void drawArrow(Graphics2D g2, BufferedImage img,
                           Rectangle rect, boolean hover, String fallback) {
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, hover ? 1.0f : 0.75f));
            g2.drawImage(img,
                    rect.x + (hover ? -3 : 0),
                    rect.y + (hover ? -3 : 0),
                    rect.width, rect.height, null);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(255, 80, 80) : new Color(180, 40, 40));
            int[] px, py;
            if (fallback.equals("<")) {
                px = new int[]{ rect.x + rect.width, rect.x,
                        rect.x + rect.width };
                py = new int[]{ rect.y, rect.y + rect.height / 2,
                        rect.y + rect.height };
            } else {
                px = new int[]{ rect.x, rect.x + rect.width,
                        rect.x };
                py = new int[]{ rect.y, rect.y + rect.height / 2,
                        rect.y + rect.height };
            }
            g2.fillPolygon(px, py, 3);
        }
    }

    // ── Button drawing ────────────────────────────────────────────────────────
    private void drawButton(Graphics2D g2, BufferedImage img,
                            Rectangle rect, boolean hover, String label) {
        Rectangle r = hover ? expandRect(rect, 3) : rect;
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, hover ? 1.0f : 0.85f));
            g2.drawImage(img, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g2.setColor(hover ? new Color(200, 200, 255) : new Color(150, 150, 220));
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label,
                    r.x + (r.width  - fm.stringWidth(label)) / 2,
                    r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    // ── Mouse listeners ───────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hoverLeft    = arrowLeftRect.contains(e.getPoint());
                hoverRight   = arrowRightRect.contains(e.getPoint());
                hoverConfirm = confirmRect.contains(e.getPoint());
                hoverBack    = backRect.contains(e.getPoint());
                setCursor((hoverLeft || hoverRight || hoverConfirm || hoverBack)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (arrowLeftRect.contains(e.getPoint())) {
                    currentIndex = (currentIndex - 1 + CHARACTERS.length) % CHARACTERS.length;
                    repaint();
                } else if (arrowRightRect.contains(e.getPoint())) {
                    currentIndex = (currentIndex + 1) % CHARACTERS.length;
                    repaint();
                } else if (confirmRect.contains(e.getPoint())) {
                    onConfirmClicked();
                } else if (backRect.contains(e.getPoint())) {
                    onBackClicked();
                }
            }
        });
    }

    // ── Button actions ────────────────────────────────────────────────────────
    private void onConfirmClicked() {
        if (gameMode.equals("ARCADE")) {
            // TODO: switch to arcade screen
            return;
        }

        if (currentPlayer == 1) {
            p1Choice      = currentIndex;
            currentPlayer = 2;
            currentIndex  = (p1Choice + 1) % CHARACTERS.length;
            repaint();
        } else {
            if (currentIndex == p1Choice) {
                // Character already taken — flash warning (optional)
                return;
            }
            int p2Choice = currentIndex;
            String p1Label = gameMode.equals("PVP") ? "Player 1" : "Player";
            String p2Label = gameMode.equals("PVP") ? "Player 2" : "Computer";
            gameWindow.switchScreen(new VersusScreen(
                    gameWindow, p1Choice, p2Choice, gameMode, p1Label, p2Label));
        }
    }

    private void onBackClicked() {
        if (currentPlayer == 2) {
            currentPlayer = 1;
            currentIndex  = p1Choice;
            p1Choice      = -1;
            repaint();
        } else {
            gameWindow.switchScreen(new GameModeScreen(gameWindow));
        }
    }

    private Rectangle expandRect(Rectangle r, int amount) {
        return new Rectangle(r.x - amount, r.y - amount,
                r.width + amount * 2, r.height + amount * 2);
    }
}