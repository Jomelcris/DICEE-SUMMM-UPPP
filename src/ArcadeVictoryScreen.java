import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ArcadeVictoryScreen extends JPanel {
    private final GameWindow gameWindow;
    private final int coins, battlesWon;
    private Rectangle menuRect = new Rectangle();
    private boolean hoverMenu = false;

    public ArcadeVictoryScreen(GameWindow gameWindow, int coins, int battlesWon) {
        this.gameWindow = gameWindow;
        this.coins      = coins;
        this.battlesWon = battlesWon;
        setLayout(null);
        addMouseListeners();
        MusicManager.get().playBGM(MusicManager.BGM_VICTORY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        int w=getWidth(), h=getHeight();
        g2.setColor(new Color(10,20,10)); g2.fillRect(0,0,w,h);

        g2.setFont(new Font("Arial",Font.BOLD,56));
        g2.setColor(new Color(255,220,50));
        FontMetrics fm=g2.getFontMetrics();
        String t="YOU WIN!";
        g2.drawString(t,(w-fm.stringWidth(t))/2,h/2-100);

        g2.setFont(new Font("Arial",Font.BOLD,20));
        g2.setColor(Color.WHITE);
        FontMetrics fm2=g2.getFontMetrics();
        String[] lines={
                "🏆 ARCADE MODE COMPLETED!",
                "Battles Won: "+battlesWon,
                "Total Coins: "+coins,
                "Sir KhaiGu has been defeated!"
        };
        int ly=h/2-40;
        for (String line:lines){
            g2.drawString(line,(w-fm2.stringWidth(line))/2,ly);
            ly+=35;
        }

        menuRect.setBounds((w-220)/2,ly+20,220,50);
        g2.setColor(hoverMenu?new Color(100,200,100):new Color(50,140,50));
        g2.fillRoundRect(menuRect.x,menuRect.y,menuRect.width,menuRect.height,12,12);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,16));
        FontMetrics fm3=g2.getFontMetrics();
        String btn="Return to Menu";
        g2.drawString(btn,menuRect.x+(menuRect.width-fm3.stringWidth(btn))/2,
                menuRect.y+(menuRect.height+fm3.getAscent()-fm3.getDescent())/2);
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