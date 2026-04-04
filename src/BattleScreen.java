import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class BattleScreen extends JPanel {

    // ── Asset paths ───────────────────────────────────────────────────────────
    private static final String BG_PATH      = "assets/backgrounds/fixed_platform1.png";
    private static final String PANEL_PATH   = "assets/ui/panell.png";
    private static final String ARROW_PATH    = "assets/ui/arrow_indicator.png";
    private static final String BTN_ATTACK_PATH   = "assets/buttons/btn_attack.png";
    private static final String BTN_SPECIAL_PATH  = "assets/buttons/btn_special.png";
    private static final String BTN_DEFEND_PATH   = "assets/buttons/btn_defend.png";
    private static final String BTN_WILDCARD_PATH = "assets/buttons/btn_wildcard.png";
    private static final String HP_0_PATH    = "assets/healthbar/health_bar_0.png";
    private static final String HP_20_PATH   = "assets/healthbar/health_bar_20.png";
    private static final String HP_40_PATH   = "assets/healthbar/health_bar_40.png";
    private static final String HP_60_PATH   = "assets/healthbar/health_bar_60.png";
    private static final String HP_80_PATH   = "assets/healthbar/health_bar_80.png";
    private static final String HP_100_PATH  = "assets/healthbar/health_bar_100.png";

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
            { "Echo",   "Assassin", "80",  "4.0", "Phantom Dance – Dodge next 2 attacks (CD: 5 turns)"         },
            { "Zyah",   "Assassin", "80",  "4.0", "Dancehall Fever – Guaranteed extra turn (CD: 4 turns)"      },
            { "Raze",   "Fighter",  "115", "3.0", "Blazing Combo – +8 damage for 3 attacks (CD: 5 turns)"      },
            { "Vibe",   "Fighter",  "110", "3.0", "House Foundation – 50% dmg reduction 2 turns (CD: 4 turns)" },
            { "Torque", "Tank",     "150", "2.0", "Earthquake Stomp – Stun enemy 2 turns (CD: 4 turns)"        },
            { "Luma",   "Tank",     "140", "2.0", "Radiant Burst – Heal 30 HP (CD: 4 turns)"                   },
            { "Lyric",  "Support",  "105", "2.0", "Healing Freestyle – Heal 35 HP (CD: 3 turns)"               },
            { "Ayo",    "Support",  "100", "2.0", "Ancestral Call – Revive 50% HP (CD: 5 turns, once/battle)"  },
    };

    // ── Sprite & panel sizes ──────────────────────────────────────────────────
    private static final int SPRITE_W  = 160;
    private static final int SPRITE_H  = 200;
    private static final int HP_BAR_W  = 220;
    private static final int HP_BAR_H  = 40;
    private static final int PANEL_W   = 420;
    private static final int PANEL_H   = 200;

    // ── Hit shake animation ───────────────────────────────────────────────────
    // Which player is currently shaking: 0 = none, 1 = P1, 2 = P2
    // ── Hit Shake Animation ───────────────────────────────────────────────────────
    private int  shakingPlayer   = 0;   // 0 = none, 1 = P1, 2 = P2
    private int  shakeFrames     = 0;
    private int  shakeOffsetX    = 0;
    private int  shakeOffsetY    = 0;
    private int  spriteFlashAlpha = 0;  // White overlay ON the sprite only (0–255)
    private int  shakeDir        = 1;

    // NO full-screen flash — only the sprite itself flashes white
    private static final int SHAKE_TOTAL_FRAMES = 40;   // 40 × 50ms = 2 seconds
    private static final int SHAKE_INTERVAL_MS  = 50;   // 20 fps

    private Timer shakeTimer;

    // ── Game state ────────────────────────────────────────────────────────────
    private final GameWindow gameWindow;
    private final String     gameMode;
    private final int        p1Index;
    private final int        p2Index;

    private int p1MaxHp, p2MaxHp;
    private int p1Hp,    p2Hp;
    private int p1SkillCd = 0, p2SkillCd = 0;
    private int p1DefendCd = 0, p2DefendCd = 0;
    private boolean p1Defending = false, p2Defending = false;
    private boolean p1Stunned   = false, p2Stunned   = false;
    private int p1StunTurns = 0, p2StunTurns = 0;

    private int  currentTurn  = 1;
    private int  roundCount   = 1;
    private boolean waitingForRoll = false;
    private boolean gameOver       = false;
    private String  winner         = "";

    // ── Dice state ────────────────────────────────────────────────────────────
    private int die1Val = 0, die2Val = 0;
    private boolean showDice = false;
    private int lastDamage   = 0;

    // ── Battle log ────────────────────────────────────────────────────────────
    private String logLine1 = "";
    private String logLine2 = "";
    private String logLine3 = "";

    // ── Wildcard ──────────────────────────────────────────────────────────────
    private String p1Wildcard = null;
    private String p2Wildcard = null;

    // ── Hover states ─────────────────────────────────────────────────────────
    private boolean hoverAttack  = false;
    private boolean hoverSpecial = false;
    private boolean hoverDefend  = false;
    private boolean hoverWild    = false;

    // ── Button rectangles ─────────────────────────────────────────────────────
    private Rectangle attackRect  = new Rectangle();
    private Rectangle specialRect = new Rectangle();
    private Rectangle defendRect  = new Rectangle();
    private Rectangle wildRect    = new Rectangle();

    // ── Assets ────────────────────────────────────────────────────────────────
    private BufferedImage bgImage;
    private BufferedImage panelImage;
    private BufferedImage[] hpBars = new BufferedImage[6];
    private BufferedImage[] sprites = new BufferedImage[8];
    private BufferedImage arrowImg;
    private BufferedImage btnAttackImg;
    private BufferedImage btnSpecialImg;
    private BufferedImage btnDefendImg;
    private BufferedImage btnWildcardImg;

    private final Random rand = new Random();

    // ─────────────────────────────────────────────────────────────────────────
    public BattleScreen(GameWindow gameWindow, int p1Index, int p2Index, String gameMode) {
        this.gameWindow = gameWindow;
        this.p1Index    = p1Index;
        this.p2Index    = p2Index;
        this.gameMode   = gameMode;

        p1MaxHp = Integer.parseInt(CHARACTERS[p1Index][2]);
        p2MaxHp = Integer.parseInt(CHARACTERS[p2Index][2]);
        p1Hp    = p1MaxHp;
        p2Hp    = p2MaxHp;

        setLayout(null);
        loadImages();
        addMouseListeners();
        determineFirstTurn();
    }

    // ── Image loading ─────────────────────────────────────────────────────────
    private void loadImages() {
        bgImage    = loadImage(BG_PATH);
        panelImage = loadImage(PANEL_PATH);

        arrowImg       = loadImage(ARROW_PATH);
        btnAttackImg   = loadImage(BTN_ATTACK_PATH);
        btnSpecialImg  = loadImage(BTN_SPECIAL_PATH);
        btnDefendImg   = loadImage(BTN_DEFEND_PATH);
        btnWildcardImg = loadImage(BTN_WILDCARD_PATH);

        String[] hpPaths = { HP_0_PATH, HP_20_PATH, HP_40_PATH,
                HP_60_PATH, HP_80_PATH, HP_100_PATH };
        for (int i = 0; i < 6; i++) hpBars[i] = loadImage(hpPaths[i]);
        for (int i = 0; i < 8; i++) sprites[i] = loadImage(SPRITE_FILES[i]);
    }

    private BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { System.err.println("Could not load: " + path); return null; }
    }

    // ── First turn determination ───────────────────────────────────────────────
    private void determineFirstTurn() {
        int roll1 = rand.nextInt(6) + rand.nextInt(6) + 2;
        int roll2 = rand.nextInt(6) + rand.nextInt(6) + 2;
        currentTurn = (roll1 >= roll2) ? 1 : 2;
        addLog(CHARACTERS[p1Index][0] + " rolled " + roll1 +
                ", " + CHARACTERS[p2Index][0] + " rolled " + roll2 + ".");
        addLog((currentTurn == 1 ? CHARACTERS[p1Index][0]
                : CHARACTERS[p2Index][0]) + " goes first!");

        if (gameMode.equals("PVC") && currentTurn == 2) {
            Timer t = new Timer(1200, e -> { doComputerTurn(); repaint(); });
            t.setRepeats(false);
            t.start();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HIT SHAKE ANIMATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Starts the shake+flash animation for the player who just took damage.
     * @param player 1 = P1 was hit, 2 = P2 was hit
     */
    // Constants for the 2-second duration
    // ── Animation Constants ───────────────────────────────────────────────────

    // These constants define the 2-second "Ouch" feel

    // ── Hit Shake Animation ───────────────────────────────────────────────────
    private void startHitAnimation(int player) {
        if (shakeTimer != null && shakeTimer.isRunning()) {
            shakeTimer.stop();
        }

        shakingPlayer    = player;
        shakeFrames      = SHAKE_TOTAL_FRAMES;
        shakeDir         = 1;
        spriteFlashAlpha = 255; // Sprite starts fully white, fades out

        shakeTimer = new Timer(SHAKE_INTERVAL_MS, e -> {
            if (shakeFrames <= 0) {
                // Reset everything cleanly
                shakeOffsetX     = 0;
                shakeOffsetY     = 0;
                spriteFlashAlpha = 0;
                shakingPlayer    = 0;
                shakeTimer.stop();
                repaint();
                return;
            }

            // progress: 1.0 (start) → 0.0 (end) — eased so movement slows down
            double progress = (double) shakeFrames / SHAKE_TOTAL_FRAMES;
            double eased    = progress * progress; // quadratic ease-out = slow motion feel

            // Horizontal slide: large at start, shrinks as progress → 0
            shakeOffsetX = (int)(20 * eased * shakeDir);

            // Vertical jolt: sharp spike at start, dies off quickly
            // Only active for first half of animation
            if (progress > 0.5) {
                shakeOffsetY = (shakeFrames % 3 == 0) ? -8 : (shakeFrames % 3 == 1 ? 4 : 0);
            } else {
                shakeOffsetY = 0; // Settles vertically in second half
            }

            // Sprite white flash: fades out over the full 2 seconds
            spriteFlashAlpha = (int)(210 * eased); // fades with ease, max 210 (not blinding)

            shakeDir = -shakeDir; // Alternate left/right
            shakeFrames--;
            repaint();
        });

        shakeTimer.start();
    }

    // ── Integrated paintComponent ─────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();

        // 1. Background
        if (bgImage != null) g2.drawImage(bgImage, 0, 0, w, h, null);
        else { g2.setColor(new Color(100, 100, 100)); g2.fillRect(0, 0, w, h); }

        // 2. Health & UI
        drawRoundBadge(g2, w);
        drawHealthBar(g2, p1Hp, p1MaxHp, 30, 20, false);
        drawHealthBar(g2, p2Hp, p2MaxHp, w - 30 - HP_BAR_W, 20, true);

        // 3. Sprites with Shake Offsets
        int groundY = (int)(h * 0.64);
        int p1X = (int)(w * 0.08) + ((shakingPlayer == 1) ? shakeOffsetX : 0);
        int p1Y = groundY - SPRITE_H + ((shakingPlayer == 1) ? shakeOffsetY : 0);
        int p2X = (int)(w * 0.80) + ((shakingPlayer == 2) ? shakeOffsetX : 0);
        int p2Y = groundY - SPRITE_H + ((shakingPlayer == 2) ? shakeOffsetY : 0);

        drawSprite(g2, sprites[p1Index], p1X, p1Y, SPRITE_W, SPRITE_H, false, shakingPlayer == 1);
        drawSprite(g2, sprites[p2Index], p2X, p2Y, SPRITE_W, SPRITE_H, true, shakingPlayer == 2);

        // 4. Action Panel & Overlays
        if (!gameOver) {
            int arrowX = (currentTurn == 1) ? p1X + SPRITE_W/2 : p2X + SPRITE_W/2;
            drawTurnArrow(g2, arrowX, groundY - SPRITE_H - 12);
        }
        if (showDice) drawDice(g2, w / 2 - 160, groundY - 140);

        int panelX = (w - PANEL_W) / 2;
        int panelY = h - PANEL_H - 20;
        drawActionPanel(g2, panelX, panelY, w, h);
        drawBattleLog(g2, panelX, panelY - 85, PANEL_W);

        if (gameOver) drawGameOverOverlay(g2, w, h);
    }

    /**
     * Draws a sprite with optional horizontal flip and red hit-flash overlay.
     */
    /**
     * Draws a sprite. When isHit=true, overlays a white tint directly on the
     * sprite only — no background rectangle, just the character flashing white.
     */
    private void drawSprite(Graphics2D g2, BufferedImage img,
                            int x, int y, int w, int h,
                            boolean flipX, boolean isHit) {

        if (img != null) {
            // --- Draw the sprite normally ---
            if (flipX) {
                g2.drawImage(img, x + w, y, -w, h, null);
            } else {
                g2.drawImage(img, x, y, w, h, null);
            }

            // --- White flash overlay clipped to sprite bounds only ---
            if (isHit && spriteFlashAlpha > 0) {
                // Create a white-tinted copy of just the sprite area
                // Uses SRC_ATOP so the flash only paints where the sprite has pixels
                BufferedImage flash = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D fg = flash.createGraphics();

                // Draw the sprite into the temp buffer
                if (flipX) {
                    fg.drawImage(img, w, 0, -w, h, null);
                } else {
                    fg.drawImage(img, 0, 0, w, h, null);
                }

                // Paint white only over non-transparent pixels
                fg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                        spriteFlashAlpha / 255f));
                fg.setColor(Color.WHITE);
                fg.fillRect(0, 0, w, h);
                fg.dispose();

                // Draw the flash buffer onto the screen
                g2.drawImage(flash, x, y, null);
            }

        } else {
            // Fallback placeholder box
            Color c = flipX ? new Color(220, 80, 80) : new Color(80, 140, 255);
            g2.setColor(c);
            g2.fillRoundRect(x + 40, y, 80, 120, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("?", x + 74, y + 65);
        }
    }

    // ── Round badge ───────────────────────────────────────────────────────────
    private void drawRoundBadge(Graphics2D g2, int w) {
        String roundText = "ROUND " + roundCount;
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(roundText) + 30;
        int bh = 28;
        int bx = (w - bw) / 2;
        int by = 10;

        g2.setColor(new Color(240, 235, 210));
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(new Color(80, 60, 30));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(new Color(60, 40, 20));
        g2.drawString(roundText, bx + 15, by + bh - 8);
    }

    // ── Health bar ────────────────────────────────────────────────────────────
    private void drawHealthBar(Graphics2D g2, int hp, int maxHp,
                               int x, int y, boolean flip) {
        double pct = (double) hp / maxHp;
        BufferedImage bar = getHpBarImage(pct);

        if (bar != null) {
            if (flip) g2.drawImage(bar, x + HP_BAR_W, y, -HP_BAR_W, HP_BAR_H, null);
            else      g2.drawImage(bar, x, y, HP_BAR_W, HP_BAR_H, null);
        } else {
            g2.setColor(new Color(60, 60, 60, 180));
            g2.fillRoundRect(x, y, HP_BAR_W, HP_BAR_H, 10, 10);
            Color barColor = pct > 0.6 ? new Color(80, 200, 80)
                    : pct > 0.3 ? new Color(220, 180, 30)
                    : new Color(200, 60, 60);
            g2.setColor(barColor);
            g2.fillRoundRect(x + 2, y + 2, (int)((HP_BAR_W - 4) * pct), HP_BAR_H - 4, 8, 8);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        String hpText = hp + " / " + maxHp;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hpText, x + (HP_BAR_W - fm.stringWidth(hpText)) / 2, y + HP_BAR_H + 16);
    }

    private BufferedImage getHpBarImage(double pct) {
        if (pct <= 0.0)  return hpBars[0];
        if (pct <= 0.20) return hpBars[1];
        if (pct <= 0.40) return hpBars[2];
        if (pct <= 0.60) return hpBars[3];
        if (pct <= 0.80) return hpBars[4];
        return hpBars[5];
    }

    // ── Turn arrow ────────────────────────────────────────────────────────────
    private void drawTurnArrow(Graphics2D g2, int cx, int y) {
        int[] px = { cx - 10, cx + 10, cx };
        int[] py = { y, y, y + 14 };
        g2.setColor(Color.BLACK);
        g2.fillPolygon(px, py, 3);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(px, py, 3);
    }

    // ── Dice ─────────────────────────────────────────────────────────────────
    private void drawDice(Graphics2D g2, int x, int y) {
        drawSingleDie(g2, x, y, die1Val);
        drawSingleDie(g2, x + 140, y, die2Val);  // wider gap for bigger dice

        g2.setFont(new Font("Arial", Font.BOLD, 26));  // bigger font
        g2.setColor(Color.WHITE);
        int total = die1Val + die2Val;
        g2.drawString("= " + total, x + 290, y + 60);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(255, 180, 50));
        g2.drawString("DMG: " + lastDamage, x + 290, y + 90);
    }

    private void drawSingleDie(Graphics2D g2, int x, int y, int val) {
        int s = 130;  // was 52, now 90
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, s, s, 14, 14);
        g2.setColor(new Color(80, 80, 80));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(x, y, s, s, 14, 14);
        g2.setColor(new Color(30, 30, 30));
        int[][] dots = getDotPositions(val, x, y, s);
        for (int[] dot : dots) g2.fillOval(dot[0], dot[1], 18, 18);  // bigger dots too
    }

    private int[][] getDotPositions(int val, int x, int y, int s) {
        int m = s / 2, q = s / 4;
        switch (val) {
            case 1: return new int[][]{ {x+m-4, y+m-4} };
            case 2: return new int[][]{ {x+q-4, y+q-4}, {x+3*q-4, y+3*q-4} };
            case 3: return new int[][]{ {x+q-4, y+q-4}, {x+m-4, y+m-4}, {x+3*q-4, y+3*q-4} };
            case 4: return new int[][]{ {x+q-4, y+q-4}, {x+3*q-4, y+q-4},
                    {x+q-4, y+3*q-4}, {x+3*q-4, y+3*q-4} };
            case 5: return new int[][]{ {x+q-4, y+q-4}, {x+3*q-4, y+q-4}, {x+m-4, y+m-4},
                    {x+q-4, y+3*q-4}, {x+3*q-4, y+3*q-4} };
            case 6: return new int[][]{ {x+q-4, y+q-4}, {x+3*q-4, y+q-4},
                    {x+q-4, y+m-4},   {x+3*q-4, y+m-4},
                    {x+q-4, y+3*q-4}, {x+3*q-4, y+3*q-4} };
            default: return new int[][]{};
        }
    }

    // ── Action panel ──────────────────────────────────────────────────────────
    private void drawActionPanel(Graphics2D g2, int px, int py, int w, int h) {
        if (panelImage != null) g2.drawImage(panelImage, px, py, PANEL_W, PANEL_H, null);
        else {
            g2.setColor(new Color(20, 60, 30, 230));
            g2.fillRoundRect(px, py, PANEL_W, PANEL_H, 20, 20);
            g2.setColor(new Color(80, 140, 60));
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(px, py, PANEL_W, PANEL_H, 20, 20);
        }

        boolean isPlayerTurn = (currentTurn == 1) ||
                (currentTurn == 2 && gameMode.equals("PVP"));

        int btnW = PANEL_W / 2 - 30;
        int btnH = 55;
        int col1 = px + 20,           col2 = px + PANEL_W / 2 + 10;
        int row1 = py + 25,           row2 = py + PANEL_H / 2 + 10;

        attackRect.setBounds(col1, row1, btnW, btnH);
        specialRect.setBounds(col2, row1, btnW, btnH);
        defendRect.setBounds(col1, row2, btnW, btnH);
        wildRect.setBounds(col2, row2, btnW, btnH);

        boolean canSpecial = (currentTurn == 1) ? p1SkillCd == 0  : p2SkillCd == 0;
        boolean canDefend  = (currentTurn == 1) ? p1DefendCd == 0 : p2DefendCd == 0;
        boolean hasWild    = (currentTurn == 1) ? p1Wildcard != null : p2Wildcard != null;

        if (!gameOver && isPlayerTurn && !waitingForRoll) {
            drawActionBtn(g2, attackRect,  hoverAttack,  "▶  ATTACK", true);
            drawActionBtn(g2, specialRect, hoverSpecial, "SPECIAL",   canSpecial);
            drawActionBtn(g2, defendRect,  hoverDefend,  "DEFEND",    canDefend);
            drawActionBtn(g2, wildRect,    hoverWild,    "WILDCARD",  hasWild);
        } else if (waitingForRoll) {
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            g2.setColor(new Color(255, 220, 80));
            String rollMsg = "Click anywhere to roll dice!";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(rollMsg, px + (PANEL_W - fm.stringWidth(rollMsg)) / 2, py + PANEL_H / 2 + 10);
        } else if (!gameOver && !isPlayerTurn) {
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.setColor(new Color(200, 200, 200));
            String wait = CHARACTERS[p2Index][0] + " is thinking...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(wait, px + (PANEL_W - fm.stringWidth(wait)) / 2, py + PANEL_H / 2 + 10);
        }

        if (!gameOver) {
            String turnLabel = (currentTurn == 1)
                    ? (gameMode.equals("PVP") ? "PLAYER 1'S TURN" : "YOUR TURN")
                    : (gameMode.equals("PVP") ? "PLAYER 2'S TURN" : "COMPUTER'S TURN");
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(new Color(200, 200, 200, 180));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(turnLabel, px + (PANEL_W - fm.stringWidth(turnLabel)) / 2, py + PANEL_H - 8);
        }
    }

    private void drawActionBtn(Graphics2D g2, Rectangle r,
                               boolean hover, String label, boolean enabled) {
        BufferedImage btnImg = null;
        switch (label) {
            case "▶  ATTACK": btnImg = btnAttackImg;   break;
            case "SPECIAL":   btnImg = btnSpecialImg;  break;
            case "DEFEND":    btnImg = btnDefendImg;   break;
            case "WILDCARD":  btnImg = btnWildcardImg; break;
        }

        if (btnImg != null) {
            float alpha = enabled ? 1.0f : 0.4f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.drawImage(btnImg, r.x, r.y, r.width, r.height, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            Color bg = !enabled ? new Color(60, 60, 60, 160)
                    : hover    ? new Color(100, 180, 80)
                    : new Color(30, 100, 50, 200);
            g2.setColor(bg);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2,
                    r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
        }

        if (hover && enabled) {
            int arrowW = 25, arrowH = 25;
            int arrowX = r.x - 2;
            int arrowY = r.y + (r.height - arrowH) / 2;
            if (arrowImg != null) {
                g2.drawImage(arrowImg, arrowX, arrowY, arrowW, arrowH, null);
            } else {
                g2.setColor(new Color(255, 220, 50));
                int cx = arrowX + arrowW / 2, cy = arrowY + arrowH / 2;
                int[] px = { cx - 8, cx + 8, cx - 8 };
                int[] py = { cy - 8, cy,     cy + 8  };
                g2.fillPolygon(px, py, 3);
            }
        }
    }

    // ── Battle log ────────────────────────────────────────────────────────────
    private void drawBattleLog(Graphics2D g2, int x, int y, int w) {
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(x, y, w, 78, 10, 10);
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(180, 180, 180));
        if (!logLine1.isEmpty()) g2.drawString(logLine1, x + 10, y + 20);
        g2.setColor(new Color(210, 210, 210));
        if (!logLine2.isEmpty()) g2.drawString(logLine2, x + 10, y + 40);
        g2.setColor(Color.WHITE);
        if (!logLine3.isEmpty()) g2.drawString(logLine3, x + 10, y + 60);
    }

    // ── Game over overlay ─────────────────────────────────────────────────────
    private void drawGameOverOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, w, h);
        g2.setFont(new Font("Arial", Font.BOLD, 52));
        g2.setColor(new Color(255, 220, 50));
        FontMetrics fm = g2.getFontMetrics();
        String title = winner + " WINS!";
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, h / 2 - 20);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(new Color(200, 200, 200));
        String sub = "Returning to menu...";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(sub, (w - fm2.stringWidth(sub)) / 2, h / 2 + 30);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GAME LOGIC
    // ─────────────────────────────────────────────────────────────────────────

    private void addLog(String msg) {
        logLine1 = logLine2;
        logLine2 = logLine3;
        logLine3 = msg;
    }

    private void onActionChosen(String action) {
        boolean isP1 = (currentTurn == 1);
        String attackerName = CHARACTERS[isP1 ? p1Index : p2Index][0];
        String defenderName = CHARACTERS[isP1 ? p2Index : p1Index][0];

        switch (action) {
            case "ATTACK":
                addLog(attackerName + " winds up for an attack!");
                waitingForRoll = true;
                repaint();
                break;
            case "SPECIAL":
                if ((isP1 && p1SkillCd > 0) || (!isP1 && p2SkillCd > 0)) {
                    addLog("Special is on cooldown!"); return;
                }
                doSpecialSkill(isP1, attackerName, defenderName);
                endTurn();
                break;
            case "DEFEND":
                if ((isP1 && p1DefendCd > 0) || (!isP1 && p2DefendCd > 0)) {
                    addLog("Defend is on cooldown!"); return;
                }
                if (isP1) { p1Defending = true; p1DefendCd = 3; }
                else       { p2Defending = true; p2DefendCd = 3; }
                addLog(attackerName + " takes a defensive stance!");
                endTurn();
                break;
            case "WILDCARD":
                String wc = isP1 ? p1Wildcard : p2Wildcard;
                if (wc == null) { addLog("No wildcard available!"); return; }
                doWildcard(isP1, wc, attackerName, defenderName);
                if (isP1) p1Wildcard = null; else p2Wildcard = null;
                endTurn();
                break;
        }
    }

    private void doRollAndAttack() {
        waitingForRoll = false;
        boolean isP1   = (currentTurn == 1);
        int attackerIdx = isP1 ? p1Index : p2Index;
        int defenderIdx = isP1 ? p2Index : p1Index;
        String attackerName = CHARACTERS[attackerIdx][0];
        String defenderName = CHARACTERS[defenderIdx][0];

        die1Val = rand.nextInt(6) + 1;
        die2Val = rand.nextInt(6) + 1;
        int total = die1Val + die2Val;
        double mult = Double.parseDouble(CHARACTERS[attackerIdx][3]);
        int damage = (int)(total * mult);

        boolean defending = isP1 ? p2Defending : p1Defending;
        if (defending) {
            damage = Math.max(1, damage / 2);
            if (isP1) p2Defending = false; else p1Defending = false;
            addLog(defenderName + " blocked! Damage halved.");
        }

        lastDamage = damage;
        showDice   = true;

        // Apply damage and trigger hit animation on the DEFENDER
        if (isP1) {
            p2Hp = Math.max(0, p2Hp - damage);
            startHitAnimation(2);   // P2 was hit
        } else {
            p1Hp = Math.max(0, p1Hp - damage);
            startHitAnimation(1);   // P1 was hit
        }

        addLog(attackerName + " rolled " + die1Val + "+" + die2Val +
                " = " + total + " × " + mult + "×");
        addLog(attackerName + " deals " + damage + " damage to " + defenderName + "!");

        if (roundCount % 3 == 0 && rand.nextInt(100) < 30) {
            String[] wildcards = {"FREEZE", "DOUBLE ROLL", "HEAL", "SHIELD"};
            String w = wildcards[rand.nextInt(wildcards.length)];
            if (isP1) p1Wildcard = w; else p2Wildcard = w;
            addLog(attackerName + " drew wildcard: " + w + "!");
        }

        repaint();

        Timer t = new Timer(2000, e -> {
            showDice = false;
            checkGameOver();
            if (!gameOver) endTurn();
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private void doSpecialSkill(boolean isP1, String attacker, String defender) {
        int idx = isP1 ? p1Index : p2Index;
        String charName = CHARACTERS[idx][0];

        if (isP1) p1SkillCd = 5; else p2SkillCd = 5;

        switch (charName) {
            case "Echo":
                addLog(attacker + " uses Phantom Dance! Dodges next 2 attacks!");
                break;
            case "Zyah":
                addLog(attacker + " uses Dancehall Fever! Extra turn granted!");
                doRollAndAttack();
                return;
            case "Raze":
                die1Val = rand.nextInt(6) + 1;
                die2Val = rand.nextInt(6) + 1;
                int total = die1Val + die2Val;
                double mult = Double.parseDouble(CHARACTERS[idx][3]);
                int dmg = (int)(total * mult) + 8;
                lastDamage = dmg; showDice = true;
                if (isP1) { p2Hp = Math.max(0, p2Hp - dmg); startHitAnimation(2); }
                else      { p1Hp = Math.max(0, p1Hp - dmg); startHitAnimation(1); }
                addLog(attacker + " uses Blazing Combo! +" + dmg + " dmg!");
                break;
            case "Vibe":
                addLog(attacker + " uses House Foundation! 50% dmg reduction x2!");
                if (isP1) p1Defending = true; else p2Defending = true;
                break;
            case "Torque":
                addLog(attacker + " uses Earthquake Stomp! " + defender + " stunned!");
                // Shake the stunned defender
                startHitAnimation(isP1 ? 2 : 1);
                if (isP1) { p2Stunned = true; p2StunTurns = 2; }
                else      { p1Stunned = true; p1StunTurns = 2; }
                break;
            case "Luma":
                int healL = 30;
                if (isP1) p1Hp = Math.min(p1MaxHp, p1Hp + healL);
                else      p2Hp = Math.min(p2MaxHp, p2Hp + healL);
                addLog(attacker + " uses Radiant Burst! Healed " + healL + " HP!");
                break;
            case "Lyric":
                int healLy = 35;
                if (isP1) p1Hp = Math.min(p1MaxHp, p1Hp + healLy);
                else      p2Hp = Math.min(p2MaxHp, p2Hp + healLy);
                addLog(attacker + " uses Healing Freestyle! Healed " + healLy + " HP!");
                break;
            case "Ayo":
                addLog(attacker + " uses Ancestral Call! Will revive at 50% HP!");
                break;
        }
    }

    private void doWildcard(boolean isP1, String wc, String attacker, String defender) {
        switch (wc) {
            case "FREEZE":
                if (isP1) { p2Stunned = true; p2StunTurns = 1; }
                else      { p1Stunned = true; p1StunTurns = 1; }
                startHitAnimation(isP1 ? 2 : 1);
                addLog(attacker + " used FREEZE! " + defender + " loses next turn!");
                break;
            case "DOUBLE ROLL":
                addLog(attacker + " used DOUBLE ROLL! Rolling twice!");
                waitingForRoll = true;
                break;
            case "HEAL":
                int heal = 20;
                if (isP1) p1Hp = Math.min(p1MaxHp, p1Hp + heal);
                else      p2Hp = Math.min(p2MaxHp, p2Hp + heal);
                addLog(attacker + " used HEAL! Restored " + heal + " HP!");
                break;
            case "SHIELD":
                if (isP1) p1Defending = true; else p2Defending = true;
                addLog(attacker + " used SHIELD! Next hit reduced by 50%!");
                break;
        }
    }

    private void endTurn() {
        if (p1SkillCd  > 0) p1SkillCd--;
        if (p2SkillCd  > 0) p2SkillCd--;
        if (p1DefendCd > 0) p1DefendCd--;
        if (p2DefendCd > 0) p2DefendCd--;

        if (p1Stunned) { p1StunTurns--; if (p1StunTurns <= 0) { p1Stunned = false; addLog(CHARACTERS[p1Index][0] + " recovered from stun!"); } }
        if (p2Stunned) { p2StunTurns--; if (p2StunTurns <= 0) { p2Stunned = false; addLog(CHARACTERS[p2Index][0] + " recovered from stun!"); } }

        currentTurn = (currentTurn == 1) ? 2 : 1;
        if (currentTurn == 1) roundCount++;

        if (currentTurn == 1 && p1Stunned) {
            addLog(CHARACTERS[p1Index][0] + " is stunned and skips their turn!");
            currentTurn = 2;
        } else if (currentTurn == 2 && p2Stunned) {
            addLog(CHARACTERS[p2Index][0] + " is stunned and skips their turn!");
            currentTurn = 1;
        }

        repaint();

        if (!gameOver && gameMode.equals("PVC") && currentTurn == 2) {
            Timer t = new Timer(1200, e -> { doComputerTurn(); repaint(); });
            t.setRepeats(false);
            t.start();
        }
    }

    private void doComputerTurn() {
        if (gameOver) return;
        String compName   = CHARACTERS[p2Index][0];
        String playerName = CHARACTERS[p1Index][0];

        int decision = rand.nextInt(100);
        if (p2SkillCd == 0 && decision < 30) {
            addLog(compName + " uses their special skill!");
            doSpecialSkill(false, compName, playerName);
        } else if (p2DefendCd == 0 && p2Hp < p2MaxHp * 0.4 && decision < 50) {
            p2Defending = true; p2DefendCd = 3;
            addLog(compName + " takes a defensive stance!");
        } else {
            addLog(compName + " attacks " + playerName + "!");
            doComputerRoll();
            return;
        }
        checkGameOver();
        if (!gameOver) endTurn();
    }

    private void doComputerRoll() {
        die1Val = rand.nextInt(6) + 1;
        die2Val = rand.nextInt(6) + 1;
        int total = die1Val + die2Val;
        double mult = Double.parseDouble(CHARACTERS[p2Index][3]);
        int damage = (int)(total * mult);

        if (p1Defending) {
            damage = Math.max(1, damage / 2);
            p1Defending = false;
            addLog(CHARACTERS[p1Index][0] + " blocked! Damage halved.");
        }

        lastDamage = damage;
        showDice   = true;

        // P1 takes the hit
        p1Hp = Math.max(0, p1Hp - damage);
        startHitAnimation(1);   // P1 was hit

        addLog(CHARACTERS[p2Index][0] + " rolled " + die1Val + "+" + die2Val +
                " = " + total + " × " + mult + "×");
        addLog("Deals " + damage + " damage to " + CHARACTERS[p1Index][0] + "!");

        repaint();

        Timer t = new Timer(2000, e -> {
            showDice = false;
            checkGameOver();
            if (!gameOver) endTurn();
            repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private void checkGameOver() {
        if (p1Hp <= 0 || p2Hp <= 0) {
            gameOver = true;
            winner   = (p1Hp > 0) ? CHARACTERS[p1Index][0] : CHARACTERS[p2Index][0];
            addLog(winner + " wins the battle!");
            repaint();

            Timer t = new Timer(4000, e ->
                    gameWindow.switchScreen(new HomeScreen(gameWindow)));
            t.setRepeats(false);
            t.start();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MOUSE LISTENERS
    // ─────────────────────────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hoverAttack  = attackRect.contains(e.getPoint());
                hoverSpecial = specialRect.contains(e.getPoint());
                hoverDefend  = defendRect.contains(e.getPoint());
                hoverWild    = wildRect.contains(e.getPoint());
                setCursor((hoverAttack||hoverSpecial||hoverDefend||hoverWild)
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (gameOver) return;
                if (waitingForRoll) { doRollAndAttack(); return; }

                boolean isPlayerTurn = (currentTurn == 1) ||
                        (currentTurn == 2 && gameMode.equals("PVP"));
                if (!isPlayerTurn) return;

                if (attackRect.contains(e.getPoint()))       onActionChosen("ATTACK");
                else if (specialRect.contains(e.getPoint())) onActionChosen("SPECIAL");
                else if (defendRect.contains(e.getPoint()))  onActionChosen("DEFEND");
                else if (wildRect.contains(e.getPoint()))    onActionChosen("WILDCARD");
            }
        });
    }
}