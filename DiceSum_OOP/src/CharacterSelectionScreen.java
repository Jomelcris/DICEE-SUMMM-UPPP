import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CharacterSelectionScreen extends JPanel {

    private static final String BG_PATH          = "assets/background_charselect.png";
    private static final String BTN_CONFIRM_PATH = "assets/btn_confirm.png";
    private static final String BTN_BACK_PATH    = "assets/btn_back.png";
    private static final String ARROW_LEFT_PATH  = "assets/arrow_left.png";
    private static final String ARROW_RIGHT_PATH = "assets/arrow_right.png";
    private static final String CARD_BG_PATH     = "assets/card_bg.png";
    private static final String FRAME_PATH       = "assets/portrait_frame.png";

    private static final int BTN_WIDTH   = 250;
    private static final int BTN_HEIGHT  = 55;
    private static final int ARROW_SIZE  = 80;
    private static final int CARD_WIDTH  = 320;
    private static final int CARD_HEIGHT = 400;

    // ── Character data ────────────────────────────────────────────────────────
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
            "assets/echo.png",  "assets/zyah.png",  "assets/raze.png",  "assets/vibe.png",
            "assets/torque.png","assets/luma.png",  "assets/lyric.png", "assets/ayo.png"
    };

    // ── Class colors ──────────────────────────────────────────────────────────
    private static final Color COLOR_ASSASSIN = new Color(180, 50,  50);
    private static final Color COLOR_FIGHTER  = new Color(200, 120, 30);
    private static final Color COLOR_TANK     = new Color(50,  100, 200);
    private static final Color COLOR_SUPPORT  = new Color(50,  180, 100);

    private final GameWindow gameWindow;
    private final String     gameMode;

    private BufferedImage bgImage;
    private BufferedImage btnConfirmImg;
    private BufferedImage btnBackImg;
    private BufferedImage arrowLeftImg;
    private BufferedImage arrowRightImg;
    private BufferedImage cardBgImg;
    private BufferedImage portraitFrameImg;
    private BufferedImage[] portraits = new BufferedImage[8];

    private int     currentIndex = 0;
    private int     p1Choice     = -1;
    private boolean p1Done       = false;

    private Rectangle arrowLeftRect  = new Rectangle();
    private Rectangle arrowRightRect = new Rectangle();
    private Rectangle confirmRect    = new Rectangle();
    private Rectangle backRect       = new Rectangle();

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

    private void loadImages() {
        bgImage          = loadImage(BG_PATH);
        btnConfirmImg    = loadImage(BTN_CONFIRM_PATH);
        btnBackImg       = loadImage(BTN_BACK_PATH);
        arrowLeftImg     = loadImage(ARROW_LEFT_PATH);
        arrowRightImg    = loadImage(ARROW_RIGHT_PATH);
        cardBgImg        = loadImage(CARD_BG_PATH);
        portraitFrameImg = loadImage(FRAME_PATH);
        for (int i = 0; i < 8; i++) {
            portraits[i] = loadImage(PORTRAIT_FILES[i]);
        }
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // Background
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            g2.setColor(new Color(20, 15, 10));
            g2.fillRect(0, 0, w, h);
        }

        // Card center position
        int cardX = (w - CARD_WIDTH) / 2;
        int cardY = (h - CARD_HEIGHT) / 2 - 30;

        // Draw the big card
        drawCard(g2, currentIndex, cardX, cardY);

        // Left arrow
        int arrowY = cardY + (CARD_HEIGHT - ARROW_SIZE) / 2;
        int leftX  = cardX - ARROW_SIZE - 20;
        arrowLeftRect.setBounds(leftX, arrowY, ARROW_SIZE, ARROW_SIZE);
        drawArrow(g2, arrowLeftImg, arrowLeftRect, hoverLeft, "<");

        // Right arrow
        int rightX = cardX + CARD_WIDTH + 20;
        arrowRightRect.setBounds(rightX, arrowY, ARROW_SIZE, ARROW_SIZE);
        drawArrow(g2, arrowRightImg, arrowRightRect, hoverRight, ">");

        // Character counter below card
        String counter = (currentIndex + 1) + " / " + CHARACTERS.length;
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(200, 200, 200));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(counter, (w - fm.stringWidth(counter)) / 2,
                cardY + CARD_HEIGHT + 15);

        // Confirm button
        int bx = (w - BTN_WIDTH) / 2;
        int by = cardY + CARD_HEIGHT + 30;
        confirmRect.setBounds(bx, by, BTN_WIDTH, BTN_HEIGHT);
        drawButton(g2, btnConfirmImg, confirmRect, hoverConfirm, "CONFIRM");

        // Back button — top left
        backRect.setBounds(20, 20, BTN_WIDTH - 100, BTN_HEIGHT - 10);
        drawButton(g2, btnBackImg, backRect, hoverBack, "BACK");

        // P1 chosen indicator — bottom left
        if (p1Done) {
            String p1Text = "P1 chose: " + CHARACTERS[p1Choice][0];
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.setColor(new Color(50, 120, 220));
            g2.drawString(p1Text, 20, h - 20);
        }
    }

    private void drawCard(Graphics2D g2, int index, int x, int y) {
        String[] data      = CHARACTERS[index];
        String   name      = data[0];
        String   cls       = data[1];
        String   origin    = data[2];
        String   hp        = data[3];
        String   dmg       = data[4];
        String   skill     = data[5];
        Color    classColor = getClassColor(cls);

        // Card background
        if (cardBgImg != null) {
            g2.drawImage(cardBgImg, x, y, CARD_WIDTH, CARD_HEIGHT, null);
        } else {
            g2.setColor(new Color(25, 20, 40, 220));
            g2.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 20, 20);
        }

        // Portrait
        int portraitSize = 120;
        int px = x + (CARD_WIDTH - portraitSize) / 2;
        int py = y + 15;

        if (portraits[index] != null) {
            g2.drawImage(portraits[index], px, py, portraitSize, portraitSize, null);
        } else {
            g2.setColor(new Color(50, 40, 30));
            g2.fillRoundRect(px, py, portraitSize, portraitSize, 12, 12);
            g2.setColor(new Color(150, 150, 150));
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            String ph = "PORTRAIT";
            g2.drawString(ph, px + (portraitSize - fm.stringWidth(ph)) / 2,
                    py + portraitSize / 2 + 5);
        }

        // Portrait frame drawn on top — delete next 3 lines if portrait is hidden


        int textY = py + portraitSize + 20;
        FontMetrics fm;

        // Name
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(255, 220, 100));
        fm = g2.getFontMetrics();
        g2.drawString(name, x + (CARD_WIDTH - fm.stringWidth(name)) / 2, textY);
        textY += 24;

        // Class badge
        int badgeW = 100;
        int badgeH = 20;
        int badgeX = x + (CARD_WIDTH - badgeW) / 2;
        g2.setColor(classColor);
        g2.fillRoundRect(badgeX, textY - 14, badgeW, badgeH, 10, 10);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        fm = g2.getFontMetrics();
        String clsUpper = cls.toUpperCase();
        g2.drawString(clsUpper,
                badgeX + (badgeW - fm.stringWidth(clsUpper)) / 2,
                textY);
        textY += 22;

        // Divider
        g2.setColor(new Color(255, 255, 255, 40));
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(x + 20, textY, x + CARD_WIDTH - 20, textY);
        textY += 14;

        // Stats
        int statX = x + 20;
        int lineH = 22;
        drawStatRow(g2, statX, textY, "Origin",  origin);
        textY += lineH;
        drawStatRow(g2, statX, textY, "Health",  hp + " HP");
        textY += lineH;
        drawStatRow(g2, statX, textY, "Damage",  dmg);
        textY += lineH + 4;

        // Special skill label
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(classColor);
        g2.drawString("SPECIAL SKILL", statX, textY);
        textY += 16;

        // Special skill text — word wrapped
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(255, 200, 80));
        drawWrappedText(g2, skill, statX, textY, CARD_WIDTH - 40, 14);
    }

    private void drawStatRow(Graphics2D g2, int x, int y, String label, String value) {
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(new Color(160, 160, 160));
        g2.drawString(label + ":", x, y);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString(value, x + 80, y);
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y,
                                 int maxWidth, int lineHeight) {
        FontMetrics   fm    = g2.getFontMetrics();
        String[]      words = text.split(" ");
        StringBuilder line  = new StringBuilder();
        for (String word : words) {
            String test = line + (line.length() > 0 ? " " : "") + word;
            if (fm.stringWidth(test) > maxWidth) {
                g2.drawString(line.toString(), x, y);
                y   += lineHeight;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) g2.drawString(line.toString(), x, y);
    }

    private Color getClassColor(String cls) {
        switch (cls) {
            case "Assassin": return COLOR_ASSASSIN;
            case "Fighter":  return COLOR_FIGHTER;
            case "Tank":     return COLOR_TANK;
            case "Support":  return COLOR_SUPPORT;
            default:         return Color.WHITE;
        }
    }

    private void drawArrow(Graphics2D g2, BufferedImage img,
                           Rectangle rect, boolean hover, String fallback) {
        if (img != null) {
            float alpha = hover ? 1.0f : 0.75f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int offset = hover ? -2 : 0;
            g2.drawImage(img, rect.x + offset, rect.y + offset,
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

    private void drawButton(Graphics2D g2, BufferedImage img,
                            Rectangle rect, boolean hover, String label) {
        Rectangle r = hover ? expandRect(rect, 3) : rect;
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    hover ? 1.0f : 0.85f));
            g2.drawImage(img, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
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

    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverLeft    = arrowLeftRect.contains(e.getPoint());
                hoverRight   = arrowRightRect.contains(e.getPoint());
                hoverConfirm = confirmRect.contains(e.getPoint());
                hoverBack    = backRect.contains(e.getPoint());

                if (hoverLeft || hoverRight || hoverConfirm || hoverBack) {
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

    private void onConfirmClicked() {
        if (!p1Done) {
            p1Choice     = currentIndex;
            p1Done       = true;
            currentIndex = (p1Choice + 1) % CHARACTERS.length;
            System.out.println("P1 chose: " + CHARACTERS[p1Choice][0]);
            repaint();
        } else {
            if (currentIndex == p1Choice) {
                System.out.println("Already taken by P1! Pick another.");
                return;
            }
            int p2Choice = currentIndex;
            System.out.println("P2 chose: " + CHARACTERS[p2Choice][0]);
            System.out.println("Both ready! Starting game...");
            // TODO: gameWindow.switchScreen(new GameScreen(gameWindow, p1Choice, p2Choice));
        }
    }

    private void onBackClicked() {
        if (p1Done) {
            p1Done       = false;
            p1Choice     = -1;
            currentIndex = 0;
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