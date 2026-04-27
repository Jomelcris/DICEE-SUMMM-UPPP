import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ArcadeGameOverScreen extends JPanel {
    private final GameWindow gameWindow;
    private final int coins, battlesWon;
    private Rectangle retryRect = new Rectangle();
    private Rectangle menuRect  = new Rectangle();
    private boolean hoverRetry=false, hoverMenu=false;

    public ArcadeGameOverScreen(GameWindow gameWindow, int coins, int battlesWon) {
        this.gameWindow  = gameWindow;
        this.coins       = coins;
        this.battlesWon  = battlesWon;
        setLayout(null);
        addMouseListeners();
        MusicManager.get().playBGM(MusicManager.BGM_GAMEOVER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        int w=getWidth(), h=getHeight();
        g2.setColor(new Color(20,0,0)); g2.fillRect(0,0,w,h);

        g2.setFont(new Font("Arial",Font.BOLD,64));
        g2.setColor(new Color(220,50,50));
        FontMetrics fm=g2.getFontMetrics();
        String t="DEFEATED";
        g2.drawString(t,(w-fm.stringWidth(t))/2,h/2-80);

        g2.setFont(new Font("Arial",Font.BOLD,22));
        g2.setColor(Color.WHITE);
        FontMetrics fm2=g2.getFontMetrics();
        String s1="Battles Won: "+battlesWon;
        String s2="Coins Earned: "+coins;
        g2.drawString(s1,(w-fm2.stringWidth(s1))/2,h/2-20);
        g2.drawString(s2,(w-fm2.stringWidth(s2))/2,h/2+20);

        menuRect.setBounds((w-220)/2, h/2+70, 220, 50);
        drawBtn(g2,menuRect,hoverMenu,"Return to Menu");
    }

    private void drawBtn(Graphics2D g2,Rectangle r,boolean hover,String label) {
        g2.setColor(hover?new Color(200,60,60):new Color(140,30,30));
        g2.fillRoundRect(r.x,r.y,r.width,r.height,12,12);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,16));
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(label,r.x+(r.width-fm.stringWidth(label))/2,
                r.y+(r.height+fm.getAscent()-fm.getDescent())/2);
    }

    private void addMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hoverMenu=menuRect.contains(e.getPoint());
                setCursor(hoverMenu?Cursor.getPredefinedCursor(Cursor.HAND_CURSOR):Cursor.getDefaultCursor());
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (menuRect.contains(e.getPoint()))
                    gameWindow.switchScreen(new HomeScreen(gameWindow));
            }
        });
    }
}