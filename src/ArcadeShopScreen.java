import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ArcadeShopScreen extends JPanel {

    private static final String BG_PATH         = "assets/backgrounds/background_shop.png";
    private static final String KEEPER_PATH      = "assets/shop/keep.gif";
    private static final String ARROW_LEFT_PATH  = "assets/ui/arrow_left.png";
    private static final String ARROW_RIGHT_PATH = "assets/ui/arrow_right.png";
    private static final String BTN_BACK_PATH    = "assets/buttons/btn_back.png";

    // ── Shop items [name, description, cost, imagePath] ───────────────────────
    private static final String[][] ITEMS = {
            { "Health Potion",    "+20 HP in battle",              "25",  "assets/shop/potion.png"        },
            { "Attack Boost",     "+2 damage for 3 attacks",       "30",  "assets/shop/attack.png"        },
            { "Defense Boost",    "-1 damage taken for 2 turns",   "30",  "assets/shop/defense.png"       },
            { "Full Heal",        "Restore all HP now",            "50",  "assets/shop/fullheal.png"      },
            { "Precision Dice",   "Next 3 rolls +2",               "40",  "assets/shop/precisiondice.png" },
            { "CD Reduction",     "Reset all cooldowns now",       "35",  "assets/shop/cd.png"            },
            { "Temp Shield",      "Block 15 damage next hit",      "30",  "assets/shop/shield.png"        },
            { "Extend Life",      "+30 Max HP permanently",        "250", "assets/shop/life.png"          },
    };

    private static final int ITEM_W   = 120;
    private static final int ITEM_H   = 140;
    private static final int KEEPER_W = 200;
    private static final int KEEPER_H = 300;

    private final GameWindow    gameWindow;
    private final ArcadeManager arcadeManager;

    // ── Scroll / page ─────────────────────────────────────────────────────────
    private int currentItemIndex = 0; // which item is selected/shown in detail

    // ── Hover ─────────────────────────────────────────────────────────────────
    private boolean hoverLeft    = false;
    private boolean hoverRight   = false;
    private boolean hoverBuy     = false;
    private boolean hoverContinue= false;
    private int     hoverItem    = -1;

    // ── Rectangles ────────────────────────────────────────────────────────────
    private Rectangle arrowLeftRect   = new Rectangle();
    private Rectangle arrowRightRect  = new Rectangle();
    private Rectangle buyRect         = new Rectangle();
    private Rectangle continueRect    = new Rectangle();
    private Rectangle[] itemRects     = new Rectangle[ITEMS.length];

    // ── Assets ────────────────────────────────────────────────────────────────
    private BufferedImage   bgImage;
    private BufferedImage   arrowLeftImg, arrowRightImg, btnBackImg;
    private ImageIcon       keeperIcon;
    private BufferedImage[] itemImages = new BufferedImage[ITEMS.length];

    // ── Feedback message ──────────────────────────────────────────────────────
    private String feedbackMsg  = "";
    private Color  feedbackColor = Color.GREEN;

    public ArcadeShopScreen(GameWindow gameWindow, ArcadeManager arcadeManager) {
        this.gameWindow    = gameWindow;
        this.arcadeManager = arcadeManager;
        setLayout(null);
        for (int i=0;i<ITEMS.length;i++) itemRects[i]=new Rectangle();
        loadImages();
        addMouseListeners();
    }

    // ── Load images ───────────────────────────────────────────────────────────
    private void loadImages() {
        bgImage      = loadImg(BG_PATH);
        arrowLeftImg = loadImg(ARROW_LEFT_PATH);
        arrowRightImg= loadImg(ARROW_RIGHT_PATH);
        btnBackImg   = loadImg(BTN_BACK_PATH);

        File kf=new File(KEEPER_PATH);
        if (kf.exists()) {
            keeperIcon=new ImageIcon(KEEPER_PATH);
            keeperIcon.setImageObserver(this);
        }
        for (int i=0;i<ITEMS.length;i++) itemImages[i]=loadImg(ITEMS[i][3]);
    }

    private BufferedImage loadImg(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { System.err.println("Missing: "+path); return null; }
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w=getWidth(), h=getHeight();

        // Background
        if (bgImage!=null) g2.drawImage(bgImage,0,0,w,h,null);
        else { g2.setColor(new Color(20,10,30)); g2.fillRect(0,0,w,h); }

        // ── Shop title ────────────────────────────────────────────────────────
        g2.setFont(new Font("Arial",Font.BOLD,32));
        g2.setColor(new Color(255,220,80));
        FontMetrics fmT=g2.getFontMetrics();
        String title="★ BATTLE SHOP ★";
        g2.drawString(title,(w-fmT.stringWidth(title))/2, 45);

        // ── Coins display ─────────────────────────────────────────────────────
        g2.setFont(new Font("Arial",Font.BOLD,18));
        g2.setColor(new Color(255,200,50));
        g2.drawString("Coins: "+arcadeManager.getCoins(), 20, 40);

        // ── Player HP display ─────────────────────────────────────────────────
        g2.setFont(new Font("Arial",Font.BOLD,16));
        g2.setColor(new Color(100,220,100));
        String hpTxt="HP: "+arcadeManager.getPlayerHp()+"/"+arcadeManager.getPlayerMaxHp();
        FontMetrics fmH=g2.getFontMetrics();
        g2.drawString(hpTxt, w-fmH.stringWidth(hpTxt)-20, 40);

        // ── Shopkeeper (left side) ────────────────────────────────────────────
        int keeperX=30;
        int keeperY=h/2-KEEPER_H/2;
        if (keeperIcon!=null) {
            g2.drawImage(keeperIcon.getImage(),keeperX,keeperY,KEEPER_W,KEEPER_H,this);
        } else {
            g2.setColor(new Color(100,80,60));
            g2.fillRoundRect(keeperX,keeperY,KEEPER_W,KEEPER_H,20,20);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.BOLD,14));
            g2.drawString("SHOPKEEPER",keeperX+20,keeperY+KEEPER_H/2);
        }

        // ── Shopkeeper dialogue ───────────────────────────────────────────────
        String[] dialogues = {
                "Welcome, warrior!",
                "Spend wisely...",
                "Good luck out there!",
                "Buy something?",
        };
        String dlg=dialogues[arcadeManager.getBattlesWon()%dialogues.length];
        drawSpeechBubble(g2, keeperX+KEEPER_W+10, keeperY+20, dlg);

        // ── Item grid (center) ────────────────────────────────────────────────
        int gridCols  = 4;
        int gridRows  = 2;
        int gridW     = gridCols*(ITEM_W+20)-20;
        int gridX     = keeperX+KEEPER_W+80;
        int gridY     = 80;

        for (int i=0;i<ITEMS.length;i++) {
            int col=i%gridCols, row=i/gridCols;
            int ix=gridX+col*(ITEM_W+20);
            int iy=gridY+row*(ITEM_H+20);
            itemRects[i].setBounds(ix,iy,ITEM_W,ITEM_H);
            drawItemCard(g2,i,ix,iy,i==currentItemIndex,i==hoverItem);
        }

        // ── Detail panel for selected item ────────────────────────────────────
        int detailX=gridX;
        int detailY=gridY+gridRows*(ITEM_H+20)+10;
        int detailW=gridW;
        int detailH=120;
        drawDetailPanel(g2,detailX,detailY,detailW,detailH);

        // ── Arrow left/right for item selection ───────────────────────────────
        int arrowY2=gridY+(gridRows*(ITEM_H+20))/2-40;
        arrowLeftRect.setBounds(gridX-70, arrowY2, 60, 60);
        arrowRightRect.setBounds(gridX+gridW+10, arrowY2, 60, 60);
        drawArrow(g2,arrowLeftImg, arrowLeftRect, hoverLeft,"<");
        drawArrow(g2,arrowRightImg,arrowRightRect,hoverRight,">");

        // ── Buy button ────────────────────────────────────────────────────────
        int buyX=(w-220)/2;
        int buyY=detailY+detailH+15;
        buyRect.setBounds(buyX,buyY,220,50);
        drawShopBtn(g2,buyRect,hoverBuy,"BUY  ("+ITEMS[currentItemIndex][2]+" coins)",true);

        // ── Continue button ───────────────────────────────────────────────────
        continueRect.setBounds(w-220-20, h-70, 220, 50);
        drawShopBtn(g2,continueRect,hoverContinue,"CONTINUE →",true);

        // ── Feedback message ──────────────────────────────────────────────────
        if (!feedbackMsg.isEmpty()) {
            g2.setFont(new Font("Arial",Font.BOLD,16));
            g2.setColor(feedbackColor);
            FontMetrics fmF=g2.getFontMetrics();
            g2.drawString(feedbackMsg,(w-fmF.stringWidth(feedbackMsg))/2,buyY+70);
        }

        // ── Boss progress bar at bottom ───────────────────────────────────────
        drawProgressBar(g2,w,h);
    }

    // ── Item card ─────────────────────────────────────────────────────────────
    private void drawItemCard(Graphics2D g2, int index,
                              int x, int y, boolean selected, boolean hover) {
        // Card background
        Color bg=selected?new Color(255,220,80,60):
                hover   ?new Color(255,255,255,40):
                        new Color(0,0,0,80);
        g2.setColor(bg);
        g2.fillRoundRect(x,y,ITEM_W,ITEM_H,12,12);

        // Border
        g2.setColor(selected?new Color(255,220,80):
                hover   ?new Color(255,255,255,180):
                        new Color(180,140,80,120));
        g2.setStroke(new BasicStroke(selected?2.5f:1.5f));
        g2.drawRoundRect(x,y,ITEM_W,ITEM_H,12,12);

        // Item image
        int imgSize=64;
        int imgX=x+(ITEM_W-imgSize)/2;
        int imgY=y+10;
        if (itemImages[index]!=null) {
            g2.drawImage(itemImages[index],imgX,imgY,imgSize,imgSize,null);
        } else {
            g2.setColor(new Color(100,80,60));
            g2.fillRoundRect(imgX,imgY,imgSize,imgSize,8,8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.BOLD,20));
            g2.drawString("?",imgX+24,imgY+38);
        }

        // Item name
        g2.setFont(new Font("Arial",Font.BOLD,11));
        g2.setColor(Color.WHITE);
        FontMetrics fm=g2.getFontMetrics();
        String name=ITEMS[index][0];
        // Wrap if needed
        if (fm.stringWidth(name)>ITEM_W-10) {
            String[] parts=name.split(" ");
            String l1=parts[0], l2=parts.length>1?parts[1]:"";
            g2.drawString(l1, x+(ITEM_W-fm.stringWidth(l1))/2, imgY+imgSize+16);
            g2.drawString(l2, x+(ITEM_W-fm.stringWidth(l2))/2, imgY+imgSize+30);
        } else {
            g2.drawString(name, x+(ITEM_W-fm.stringWidth(name))/2, imgY+imgSize+16);
        }

        // Cost badge
        g2.setColor(new Color(255,200,0));
        g2.setFont(new Font("Arial",Font.BOLD,12));
        FontMetrics fmC=g2.getFontMetrics();
        String cost=ITEMS[index][2]+"c";
        g2.drawString(cost, x+(ITEM_W-fmC.stringWidth(cost))/2, y+ITEM_H-8);
    }

    // ── Detail panel ──────────────────────────────────────────────────────────
    private void drawDetailPanel(Graphics2D g2,int x,int y,int w,int h) {
        g2.setColor(new Color(0,0,0,120));
        g2.fillRoundRect(x,y,w,h,12,12);
        g2.setColor(new Color(255,220,80,150));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x,y,w,h,12,12);

        String[] item=ITEMS[currentItemIndex];
        g2.setFont(new Font("Arial",Font.BOLD,18));
        g2.setColor(new Color(255,220,80));
        g2.drawString(item[0], x+16, y+28);

        g2.setFont(new Font("Arial",Font.PLAIN,14));
        g2.setColor(Color.WHITE);
        g2.drawString(item[1], x+16, y+52);

        g2.setFont(new Font("Arial",Font.BOLD,16));
        g2.setColor(new Color(255,200,0));
        g2.drawString("Cost: "+item[2]+" coins", x+16, y+78);

        // Affordability
        boolean canAfford=arcadeManager.getCoins()>=Integer.parseInt(item[2]);
        g2.setFont(new Font("Arial",Font.BOLD,13));
        g2.setColor(canAfford?new Color(100,220,100):new Color(220,80,80));
        g2.drawString(canAfford?"✓ You can afford this":"✗ Not enough coins",
                x+16, y+100);
    }

    // ── Speech bubble ─────────────────────────────────────────────────────────
    private void drawSpeechBubble(Graphics2D g2,int x,int y,String text) {
        g2.setFont(new Font("Arial",Font.BOLD,14));
        FontMetrics fm=g2.getFontMetrics();
        int bw=fm.stringWidth(text)+30, bh=40;
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x,y,bw,bh,12,12);
        g2.setColor(new Color(80,60,30));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x,y,bw,bh,12,12);
        g2.setColor(new Color(40,20,0));
        g2.drawString(text, x+15, y+bh-12);
        // Tail
        int[] tx={x+10,x,x+10}, ty={y+bh,y+bh+12,y+bh+5};
        g2.setColor(Color.WHITE); g2.fillPolygon(tx,ty,3);
        g2.setColor(new Color(80,60,30)); g2.drawPolyline(tx,ty,3);
    }

    // ── Progress bar ──────────────────────────────────────────────────────────
    private void drawProgressBar(Graphics2D g2,int w,int h) {
        int barW=w-40, barH=20, barX=20, barY=h-35;
        int total=7; // 5 mini + 1 minifinal + 1 final
        int done=arcadeManager.getCurrentBossIndex();

        g2.setColor(new Color(0,0,0,120));
        g2.fillRoundRect(barX,barY,barW,barH,10,10);
        g2.setColor(new Color(255,180,0));
        int fillW=(int)((double)done/total*barW);
        g2.fillRoundRect(barX,barY,fillW,barH,10,10);
        g2.setColor(new Color(255,255,255,80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(barX,barY,barW,barH,10,10);

        g2.setFont(new Font("Arial",Font.BOLD,12));
        g2.setColor(Color.WHITE);
        String prog="Progress: "+done+"/"+total+" battles";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(prog,(w-fm.stringWidth(prog))/2,barY+14);
    }

    // ── Arrow ─────────────────────────────────────────────────────────────────
    private void drawArrow(Graphics2D g2,BufferedImage img,
                           Rectangle rect,boolean hover,String fb) {
        if (img!=null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,hover?1.0f:0.75f));
            g2.drawImage(img,rect.x+(hover?-2:0),rect.y+(hover?-2:0),rect.width,rect.height,null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
        } else {
            g2.setColor(hover?new Color(255,200,80):new Color(180,140,50));
            g2.fillRoundRect(rect.x,rect.y,rect.width,rect.height,10,10);
            g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,22));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(fb,rect.x+(rect.width-fm.stringWidth(fb))/2,
                    rect.y+(rect.height+fm.getAscent()-fm.getDescent())/2);
        }
    }

    // ── Shop button ───────────────────────────────────────────────────────────
    private void drawShopBtn(Graphics2D g2,Rectangle r,boolean hover,String label,boolean enabled) {
        Color bg=hover?new Color(255,200,50):new Color(180,140,20);
        g2.setColor(bg); g2.fillRoundRect(r.x,r.y,r.width,r.height,12,12);
        g2.setColor(new Color(100,70,0));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(r.x,r.y,r.width,r.height,12,12);
        g2.setFont(new Font("Arial",Font.BOLD,15));
        g2.setColor(new Color(40,20,0));
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(label,r.x+(r.width-fm.stringWidth(label))/2,
                r.y+(r.height+fm.getAscent()-fm.getDescent())/2);
    }

    // ── Purchase logic ────────────────────────────────────────────────────────
    private void purchaseCurrentItem() {
        int cost=Integer.parseInt(ITEMS[currentItemIndex][2]);
        if (arcadeManager.getCoins()<cost) {
            feedbackMsg="Not enough coins!"; feedbackColor=new Color(220,80,80);
            repaint(); return;
        }
        arcadeManager.spendCoins(cost);
        arcadeManager.getShopInventory()[currentItemIndex]++;

        // Apply immediate effects
        switch(currentItemIndex) {
            case 3: // Full Heal
                arcadeManager.setPlayerHp(arcadeManager.getPlayerMaxHp());
                feedbackMsg="Full Heal used! HP fully restored!";
                break;
            case 7: // Extend Life
                arcadeManager.setPlayerMaxHp(arcadeManager.getPlayerMaxHp()+30);
                arcadeManager.setPlayerHp(arcadeManager.getPlayerHp()+30);
                feedbackMsg="Max HP increased by 30!";
                break;
            default:
                feedbackMsg=ITEMS[currentItemIndex][0]+" added to inventory!";
                break;
        }
        feedbackColor=new Color(100,220,100);
        repaint();
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hoverLeft    =arrowLeftRect.contains(e.getPoint());
                hoverRight   =arrowRightRect.contains(e.getPoint());
                hoverBuy     =buyRect.contains(e.getPoint());
                hoverContinue=continueRect.contains(e.getPoint());
                hoverItem=-1;
                for (int i=0;i<itemRects.length;i++) {
                    if (itemRects[i].contains(e.getPoint())) { hoverItem=i; break; }
                }
                setCursor((hoverLeft||hoverRight||hoverBuy||hoverContinue||hoverItem>=0)
                        ?Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        :Cursor.getDefaultCursor());
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (arrowLeftRect.contains(e.getPoint())) {
                    currentItemIndex=(currentItemIndex-1+ITEMS.length)%ITEMS.length;
                    feedbackMsg=""; repaint();
                } else if (arrowRightRect.contains(e.getPoint())) {
                    currentItemIndex=(currentItemIndex+1)%ITEMS.length;
                    feedbackMsg=""; repaint();
                } else if (buyRect.contains(e.getPoint())) {
                    purchaseCurrentItem();
                } else if (continueRect.contains(e.getPoint())) {
                    arcadeManager.onShopDone();
                } else {
                    for (int i=0;i<itemRects.length;i++) {
                        if (itemRects[i].contains(e.getPoint())) {
                            currentItemIndex=i; feedbackMsg=""; repaint(); break;
                        }
                    }
                }
            }
        });
    }
}