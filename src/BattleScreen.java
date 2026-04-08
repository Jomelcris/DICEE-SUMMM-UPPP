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
    private static final String BG_PATH           = "assets/backgrounds/background_battlescreenn.png";
    private static final String ARROW_PATH        = "assets/ui/arrow_indicator.png";
    private static final String BTN_ATTACK_PATH   = "assets/buttons/btn_attack.png";
    private static final String BTN_SPECIAL_PATH  = "assets/buttons/btn_special.png";
    private static final String BTN_DEFEND_PATH   = "assets/buttons/btn_defend.png";
    private static final String BTN_WILDCARD_PATH = "assets/buttons/btn_wildcard.png";
    private static final String HP_0_PATH         = "assets/healthbar/health_bar_0.png";
    private static final String HP_20_PATH        = "assets/healthbar/health_bar_20.png";
    private static final String HP_40_PATH        = "assets/healthbar/health_bar_40.png";
    private static final String HP_60_PATH        = "assets/healthbar/health_bar_60.png";
    private static final String HP_80_PATH        = "assets/healthbar/health_bar_80.png";
    private static final String HP_100_PATH       = "assets/healthbar/health_bar_100.png";

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
    private static final int PANEL_H   = 230;

    // ── Hit Shake Animation ───────────────────────────────────────────────────
    private int  shakingPlayer   = 0;
    private int  shakeFrames     = 0;
    private int  shakeOffsetX    = 0;
    private int  shakeOffsetY    = 0;
    private int  spriteFlashAlpha = 0;
    private int  shakeDir        = 1;
    private boolean isDefendFlash = false;

    private static final int SHAKE_TOTAL_FRAMES = 40;
    private static final int SHAKE_INTERVAL_MS  = 50;

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

    private int     currentTurn    = 1;
    private int     roundCount     = 1;
    private boolean waitingForRoll = false;
    private boolean gameOver       = false;
    private String  winner         = "";

    // ── Dice state ────────────────────────────────────────────────────────────
    private int die1Val = 0, die2Val = 0;
    private boolean showDice  = false;
    private int     lastDamage = 0;

    // ── Battle log ────────────────────────────────────────────────────────────
    private String logLine1 = "";
    private String logLine2 = "";
    private String logLine3 = "";

    // ── Wildcard ──────────────────────────────────────────────────────────────
    private String p1Wildcard = null;
    private String p2Wildcard = null;

    // ── Hover states ──────────────────────────────────────────────────────────
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
    private BufferedImage   bgImage;
    private BufferedImage[] hpBars     = new BufferedImage[6];
    private BufferedImage[] sprites    = new BufferedImage[8];
    private BufferedImage[] diceImages = new BufferedImage[6];
    private BufferedImage   arrowImg;
    private BufferedImage   btnAttackImg;
    private BufferedImage   btnSpecialImg;
    private BufferedImage   btnDefendImg;
    private BufferedImage   btnWildcardImg;
    private BufferedImage   p1LabelImg;
    private BufferedImage   p2LabelImg;

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

        arrowImg       = loadImage(ARROW_PATH);
        btnAttackImg   = loadImage(BTN_ATTACK_PATH);
        btnSpecialImg  = loadImage(BTN_SPECIAL_PATH);
        btnDefendImg   = loadImage(BTN_DEFEND_PATH);
        btnWildcardImg = loadImage(BTN_WILDCARD_PATH);
        p1LabelImg     = loadImage("assets/ui/player_1.png");
        p2LabelImg     = loadImage("assets/ui/player_2.png");

        String[] hpPaths = { HP_0_PATH, HP_20_PATH, HP_40_PATH,
                HP_60_PATH, HP_80_PATH, HP_100_PATH };
        for (int i = 0; i < 6; i++) hpBars[i]    = loadImage(hpPaths[i]);
        for (int i = 0; i < 8; i++) sprites[i]   = loadImage(SPRITE_FILES[i]);
        for (int i = 0; i < 6; i++) diceImages[i] = loadImage("assets/dice/dice_" + (i + 1) + ".png");
    }

    private BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { System.err.println("Could not load: " + path); return null; }
    }

    // ── First turn determination ──────────────────────────────────────────────
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
    private void startHitAnimation(int player) {
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();

        isDefendFlash    = false;
        shakingPlayer    = player;
        shakeFrames      = SHAKE_TOTAL_FRAMES;
        shakeDir         = 1;
        spriteFlashAlpha = 255;

        shakeTimer = new Timer(SHAKE_INTERVAL_MS, e -> {
            if (shakeFrames <= 0) {
                shakeOffsetX     = 0;
                shakeOffsetY     = 0;
                spriteFlashAlpha = 0;
                shakingPlayer    = 0;
                isDefendFlash    = false;
                shakeTimer.stop();
                repaint();
                return;
            }

            double progress = (double) shakeFrames / SHAKE_TOTAL_FRAMES;
            double eased    = progress * progress;

            shakeOffsetX = (int)(20 * eased * shakeDir);
            if (progress > 0.5) {
                shakeOffsetY = (shakeFrames % 3 == 0) ? -8 : (shakeFrames % 3 == 1 ? 4 : 0);
            } else {
                shakeOffsetY = 0;
            }
            spriteFlashAlpha = (int)(210 * eased);

            shakeDir = -shakeDir;
            shakeFrames--;
            repaint();
        });
        shakeTimer.start();
    }

    // ── Defend Animation ──────────────────────────────────────────────────────
    private void startDefendAnimation(int player) {
        if (shakeTimer != null && shakeTimer.isRunning()) shakeTimer.stop();

        isDefendFlash    = true;
        shakingPlayer    = player;
        shakeFrames      = SHAKE_TOTAL_FRAMES;
        shakeDir         = 1;
        spriteFlashAlpha = 255;

        shakeTimer = new Timer(SHAKE_INTERVAL_MS, e -> {
            if (shakeFrames <= 0) {
                shakeOffsetX     = 0;
                shakeOffsetY     = 0;
                spriteFlashAlpha = 0;
                shakingPlayer    = 0;
                isDefendFlash    = false;
                shakeTimer.stop();
                repaint();
                return;
            }

            double progress = (double) shakeFrames / SHAKE_TOTAL_FRAMES;
            double eased    = progress * progress;

            // Slide backward away from opponent
            shakeOffsetX = (int)(14 * eased * (player == 1 ? -1 : 1));
            shakeOffsetY = 0;
            spriteFlashAlpha = (int)(190 * eased);

            shakeDir = -shakeDir;
            shakeFrames--;
            repaint();
        });
        shakeTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PAINT
    // ─────────────────────────────────────────────────────────────────────────
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

        // 2. Round badge & health bars
        drawRoundBadge(g2, w);
        drawHealthBar(g2, p1Hp, p1MaxHp, 30, 20, false,
                CHARACTERS[p1Index][0], p1LabelImg);
        drawHealthBar(g2, p2Hp, p2MaxHp, w - 30 - HP_BAR_W, 20, true,
                CHARACTERS[p2Index][0], p2LabelImg);

        // 3. Sprites
        int panelY  = h - PANEL_H;
        int groundY = panelY;

        int p1X = (int)(w * 0.08) + ((shakingPlayer == 1) ? shakeOffsetX : 0);
        int p1Y = groundY - SPRITE_H + ((shakingPlayer == 1) ? shakeOffsetY : 0);
        int p2X = (int)(w * 0.78) + ((shakingPlayer == 2) ? shakeOffsetX : 0);
        int p2Y = groundY - SPRITE_H + ((shakingPlayer == 2) ? shakeOffsetY : 0);

        drawSprite(g2, sprites[p1Index], p1X, p1Y, SPRITE_W, SPRITE_H, false, shakingPlayer == 1);
        drawSprite(g2, sprites[p2Index], p2X, p2Y, SPRITE_W, SPRITE_H, true,  shakingPlayer == 2);

        // 4. Turn arrow
        if (!gameOver) {
            int arrowX = (currentTurn == 1) ? p1X + SPRITE_W / 2 : p2X + SPRITE_W / 2;
            drawTurnArrow(g2, arrowX, groundY - SPRITE_H - 12);
        }

        // 5. Action panel (contains log + buttons + dice + right info)
        drawActionPanel(g2, 0, panelY, w, h);

        // 6. Game over overlay
        if (gameOver) drawGameOverOverlay(g2, w, h);
    }

    // ── Draw sprite with optional flip and flash ──────────────────────────────
    private void drawSprite(Graphics2D g2, BufferedImage img,
                            int x, int y, int w, int h,
                            boolean flipX, boolean isHit) {
        if (img != null) {
            if (flipX) g2.drawImage(img, x + w, y, -w, h, null);
            else       g2.drawImage(img, x, y, w, h, null);

            if (isHit && spriteFlashAlpha > 0) {
                BufferedImage flash = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D fg = flash.createGraphics();
                if (flipX) fg.drawImage(img, w, 0, -w, h, null);
                else       fg.drawImage(img, 0, 0, w, h, null);
                fg.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_ATOP, spriteFlashAlpha / 255f));
                // Blue for defend, white for hit
                fg.setColor(isDefendFlash ? new Color(80, 160, 255) : Color.WHITE);
                fg.fillRect(0, 0, w, h);
                fg.dispose();
                g2.drawImage(flash, x, y, null);
            }
        } else {
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
                               int x, int y, boolean flip,
                               String charName, BufferedImage labelImg) {
        double pct = (double) hp / maxHp;
        BufferedImage bar = getHpBarImage(pct);

        // ── P1/P2 label image below character name ────────────────────────────
        int labelW = 60;
        int labelH = 25;

        if (labelImg != null) {
            if (!flip) {
                // P1 label — left side, below char name
                g2.drawImage(labelImg,
                        x + (HP_BAR_W - labelW) / 2,
                        y + HP_BAR_H + 38,
                        labelW, labelH, null);
            } else {
                // P2 label — right side, below char name
                g2.drawImage(labelImg,
                        x + (HP_BAR_W - labelW) / 2,
                        y + HP_BAR_H + 38,
                        labelW, labelH, null);
            }
        }

        // ── Health bar image ──────────────────────────────────────────────────
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
            g2.fillRoundRect(x + 2, y + 2,
                    (int)((HP_BAR_W - 4) * pct), HP_BAR_H - 4, 8, 8);
        }

        // ── HP numbers ────────────────────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        String hpText = hp + " / " + maxHp;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hpText,
                x + (HP_BAR_W - fm.stringWidth(hpText)) / 2,
                y + HP_BAR_H + 16);

        // ── Character name below HP numbers ───────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(255, 220, 100));
        FontMetrics fm2 = g2.getFontMetrics();
        String name = charName.toUpperCase();
        g2.drawString(name,
                x + (HP_BAR_W - fm2.stringWidth(name)) / 2,
                y + HP_BAR_H + 34);

        // ── P1/P2 label drawn BELOW character name ────────────────────────────
        if (labelImg != null) {
            g2.drawImage(labelImg,
                    x + (HP_BAR_W - labelW) / 2,
                    y + HP_BAR_H + 40,
                    labelW, labelH, null);
        } else {
            // Fallback text if image not found
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(flip ? new Color(220, 80, 80) : new Color(80, 140, 255));
            String fallback = flip ? "P2" : "P1";
            FontMetrics fm3 = g2.getFontMetrics();
            g2.drawString(fallback,
                    x + (HP_BAR_W - fm3.stringWidth(fallback)) / 2,
                    y + HP_BAR_H + 55);
        }
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

    // ── Dice ──────────────────────────────────────────────────────────────────
    private void drawDice(Graphics2D g2, int x, int y) {
        int dieSize = 55;
        drawSingleDie(g2, x, y, die1Val, dieSize);
        drawSingleDie(g2, x + dieSize + 8, y, die2Val, dieSize);

        int total = die1Val + die2Val;
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(60, 40, 10));
        g2.drawString("= " + total, x + dieSize * 2 + 16, y + 22);
        g2.setColor(new Color(160, 60, 0));
        g2.drawString("DMG: " + lastDamage, x + dieSize * 2 + 16, y + 42);
    }

    private void drawSingleDie(Graphics2D g2, int x, int y, int val, int s) {
        if (val < 1 || val > 6) return;
        BufferedImage dieImg = diceImages[val - 1];

        if (dieImg != null) {
            g2.drawImage(dieImg, x, y, s, s, null);
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, s, s, 8, 8);
            g2.setColor(new Color(80, 80, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, s, s, 8, 8);
            g2.setColor(new Color(30, 30, 30));
            int[][] dots = getDotPositions(val, x, y, s);
            int dotSize  = Math.max(4, s / 8);
            for (int[] dot : dots) g2.fillOval(dot[0], dot[1], dotSize, dotSize);
        }
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
                    {x+q-4, y+m-4},  {x+3*q-4, y+m-4},
                    {x+q-4, y+3*q-4},{x+3*q-4, y+3*q-4} };
            default: return new int[][]{};
        }
    }

    // ── Action panel ──────────────────────────────────────────────────────────
    private void drawActionPanel(Graphics2D g2, int px, int py, int w, int h) {
        // Full-width panel background

        boolean isPlayerTurn = (currentTurn == 1) ||
                (currentTurn == 2 && gameMode.equals("PVP"));

        // ── CENTER: 2x2 buttons ───────────────────────────────────────────────
        int btnW    = 180;
        int btnH    = 55;
        int centerX = w / 2 - btnW - 10;
        int row1    = py + 20;
        int row2    = py + 85;

        attackRect.setBounds(centerX,              row1, btnW, btnH);
        specialRect.setBounds(centerX + btnW + 20, row1, btnW, btnH);
        defendRect.setBounds(centerX,              row2, btnW, btnH);
        wildRect.setBounds(centerX + btnW + 20,    row2, btnW, btnH);

        boolean canSpecial = (currentTurn == 1) ? p1SkillCd == 0  : p2SkillCd == 0;
        boolean canDefend  = (currentTurn == 1) ? p1DefendCd == 0 : p2DefendCd == 0;
        boolean hasWild    = (currentTurn == 1) ? p1Wildcard != null : p2Wildcard != null;

        if (!gameOver && isPlayerTurn && !waitingForRoll) {
            drawActionBtn(g2, attackRect,  hoverAttack,  "▶  ATTACK", true);
            drawActionBtn(g2, specialRect, hoverSpecial, "SPECIAL",   canSpecial);
            drawActionBtn(g2, defendRect,  hoverDefend,  "DEFEND",    canDefend);
            drawActionBtn(g2, wildRect,    hoverWild,    "WILDCARD",  hasWild);
        } else {
            drawActionBtn(g2, attackRect,  false, "▶  ATTACK", false);
            drawActionBtn(g2, specialRect, false, "SPECIAL",   false);
            drawActionBtn(g2, defendRect,  false, "DEFEND",    false);
            drawActionBtn(g2, wildRect,    false, "WILDCARD",  false);
        }

        // ── RIGHT: log + sub-label + cooldowns ────────────────────────────────
        if (!gameOver) {
            int rightX       = w * 2 / 3 + 20;
            int rightW       = w / 3 - 40;
            int rightCenterX = rightX + rightW / 2;

            // ── Battle log lines at the top of the right section ─────────────
            if (!logLine2.isEmpty()) {
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.setColor(new Color(100, 60, 20));
                FontMetrics fmLog2 = g2.getFontMetrics();
                g2.drawString(logLine2,
                        rightCenterX - fmLog2.stringWidth(logLine2) / 2,
                        py + 22);
            }
            if (!logLine3.isEmpty()) {
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(new Color(80, 40, 0));
                FontMetrics fmLog3 = g2.getFontMetrics();
                g2.drawString(logLine3,
                        rightCenterX - fmLog3.stringWidth(logLine3) / 2,
                        py + 40);
            }

            // ── Divider line ──────────────────────────────────────────────────
            g2.setColor(new Color(160, 120, 70, 120));
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(rightX, py + 50, rightX + rightW, py + 50);

            // ── Sub-label (CLICK TO ROLL / IS THINKING) ───────────────────────
            String subLabel = waitingForRoll ? "CLICK TO ROLL!" :
                    !isPlayerTurn ? "IS THINKING..." : "";
            if (!subLabel.isEmpty()) {
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.setColor(new Color(120, 70, 10));
                FontMetrics fmSub = g2.getFontMetrics();
                g2.drawString(subLabel,
                        rightCenterX - fmSub.stringWidth(subLabel) / 2,
                        py + 68);
            }

            // ── Cooldowns ─────────────────────────────────────────────────────
            int cdSpecial = (currentTurn == 1) ? p1SkillCd : p2SkillCd;
            int cdDefend  = (currentTurn == 1) ? p1DefendCd : p2DefendCd;
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(new Color(100, 60, 20));
            String cdSText = "Special CD: " + (cdSpecial > 0 ? cdSpecial : "Ready");
            String cdDText = "Defend CD:  " + (cdDefend  > 0 ? cdDefend  : "Ready");
            FontMetrics fm3 = g2.getFontMetrics();
            g2.drawString(cdSText,
                    rightCenterX - fm3.stringWidth(cdSText) / 2,
                    py + 85);
            g2.drawString(cdDText,
                    rightCenterX - fm3.stringWidth(cdDText) / 2,
                    py + 102);

            // ── Wildcard if available ─────────────────────────────────────────
            String wc = (currentTurn == 1) ? p1Wildcard : p2Wildcard;
            if (wc != null) {
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(new Color(140, 0, 180));
                String wcText = "WILDCARD: " + wc;
                FontMetrics fm4 = g2.getFontMetrics();
                g2.drawString(wcText,
                        rightCenterX - fm4.stringWidth(wcText) / 2,
                        py + 122);
            }

            // ── Dice in right panel ───────────────────────────────────────────
            if (showDice) drawDice(g2, rightX, py + 130);
        }
    }

    // ── Action button ─────────────────────────────────────────────────────────
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
            g2.drawString(label,
                    r.x + (r.width  - fm.stringWidth(label)) / 2,
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
                else      { p2Defending = true; p2DefendCd = 3; }
                addLog(attackerName + " takes a defensive stance!");
                startDefendAnimation(isP1 ? 1 : 2);
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
        boolean isP1       = (currentTurn == 1);
        int attackerIdx    = isP1 ? p1Index : p2Index;
        int defenderIdx    = isP1 ? p2Index : p1Index;
        String attackerName = CHARACTERS[attackerIdx][0];
        String defenderName = CHARACTERS[defenderIdx][0];

        die1Val = rand.nextInt(6) + 1;
        die2Val = rand.nextInt(6) + 1;
        int    total  = die1Val + die2Val;
        double mult   = Double.parseDouble(CHARACTERS[attackerIdx][3]);
        int    damage = (int)(total * mult);

        boolean defending = isP1 ? p2Defending : p1Defending;
        if (defending) {
            damage = Math.max(1, damage / 2);
            if (isP1) p2Defending = false; else p1Defending = false;
            addLog(defenderName + " blocked! Damage halved.");
        }

        final int finalDamage = damage;
        lastDamage = finalDamage;

        if (isP1) { p2Hp = Math.max(0, p2Hp - finalDamage); startHitAnimation(2); }
        else      { p1Hp = Math.max(0, p1Hp - finalDamage); startHitAnimation(1); }

        int animDuration = SHAKE_TOTAL_FRAMES * SHAKE_INTERVAL_MS;
        Timer showTimer = new Timer(animDuration, e -> {
            showDice = true;
            addLog(attackerName + " rolled " + die1Val + "+" + die2Val +
                    " = " + total + " × " + mult + "×");
            addLog(attackerName + " deals " + finalDamage + " damage to " + defenderName + "!");

            if (roundCount % 3 == 0 && rand.nextInt(100) < 30) {
                String[] wildcards = {"FREEZE", "DOUBLE ROLL", "HEAL", "SHIELD"};
                String wc = wildcards[rand.nextInt(wildcards.length)];
                if (isP1) p1Wildcard = wc; else p2Wildcard = wc;
                addLog(attackerName + " drew wildcard: " + wc + "!");
            }
            repaint();

            Timer hideTimer = new Timer(2000, e2 -> {
                showDice = false;
                checkGameOver();
                if (!gameOver) endTurn();
                repaint();
            });
            hideTimer.setRepeats(false);
            hideTimer.start();
        });
        showTimer.setRepeats(false);
        showTimer.start();
    }

    private void doSpecialSkill(boolean isP1, String attacker, String defender) {
        int    idx      = isP1 ? p1Index : p2Index;
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
                lastDamage = dmg;
                showDice   = true;
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

    private void doWildcard(boolean isP1, String wc,
                            String attacker, String defender) {
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

        if (p1Stunned) {
            p1StunTurns--;
            if (p1StunTurns <= 0) {
                p1Stunned = false;
                addLog(CHARACTERS[p1Index][0] + " recovered from stun!");
            }
        }
        if (p2Stunned) {
            p2StunTurns--;
            if (p2StunTurns <= 0) {
                p2Stunned = false;
                addLog(CHARACTERS[p2Index][0] + " recovered from stun!");
            }
        }

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
            p2Defending = true;
            p2DefendCd  = 3;
            addLog(compName + " takes a defensive stance!");
            startDefendAnimation(2);
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
        int    total  = die1Val + die2Val;
        double mult   = Double.parseDouble(CHARACTERS[p2Index][3]);
        int    damage = (int)(total * mult);

        if (p1Defending) {
            damage = Math.max(1, damage / 2);
            p1Defending = false;
            addLog(CHARACTERS[p1Index][0] + " blocked! Damage halved.");
        }

        final int finalDamage = damage;
        lastDamage = finalDamage;

        p1Hp = Math.max(0, p1Hp - finalDamage);
        startHitAnimation(1);

        int animDuration = SHAKE_TOTAL_FRAMES * SHAKE_INTERVAL_MS;
        Timer showTimer = new Timer(animDuration, e -> {
            showDice = true;
            addLog(CHARACTERS[p2Index][0] + " rolled " + die1Val + "+" + die2Val +
                    " = " + total + " × " + mult + "×");
            addLog("Deals " + finalDamage + " damage to " + CHARACTERS[p1Index][0] + "!");
            repaint();

            Timer hideTimer = new Timer(2000, e2 -> {
                showDice = false;
                checkGameOver();
                if (!gameOver) endTurn();
                repaint();
            });
            hideTimer.setRepeats(false);
            hideTimer.start();
        });
        showTimer.setRepeats(false);
        showTimer.start();
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
                setCursor((hoverAttack || hoverSpecial || hoverDefend || hoverWild)
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

                if      (attackRect.contains(e.getPoint()))  onActionChosen("ATTACK");
                else if (specialRect.contains(e.getPoint())) onActionChosen("SPECIAL");
                else if (defendRect.contains(e.getPoint()))  onActionChosen("DEFEND");
                else if (wildRect.contains(e.getPoint()))    onActionChosen("WILDCARD");
            }
        });
    }
}