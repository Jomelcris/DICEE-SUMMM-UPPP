public class ArcadeManager {
    private final GameWindow gameWindow;
    private final int        playerIndex;

    // ── Boss data [name, class, hp, damage, special, gifPath] ────────────────
    public static final String[][] MINIBOSSES = {
            { "Dreined",    "Support",  "125", "2.0", "Healing Aura – Heals 20 HP (CD: 3 turns)",          "assets/miniboss/drei.gif"     },
            { "Dextereous", "Assassin", "100", "4.0", "Shadow Strike – Dodge next attack (CD: 4 turns)",   "assets/miniboss/dex.gif"      },
            { "Cromel",     "Fighter",  "135", "3.0", "Flame Burst – +8 damage bonus (CD: 5 turns)",       "assets/miniboss/mel.gif"      },
            { "Jeff Taller","Assassin", "100", "4.0", "Phantom Step – Extra turn (CD: 4 turns)",           "assets/miniboss/yan.gif"      },
            { "JoshDrich",  "Tank",     "170", "2.0", "Ground Slam – Stun 2 turns (CD: 4 turns)",          "assets/miniboss/selos.gif"    },
    };

    public static final String[] MINIFINAL_BOSS = {
            "Akhai", "Assassin", "250", "4.0",
            "Death Dance – Dodge + counter attack (CD: 5 turns)",
            "assets/boss/minifinal.gif"
    };

    public static final String[] FINAL_BOSS = {
            "Sir KhaiGu", "Master", "200", "3.0",
            "HAGBONG KA SAKEN BOI – +5 damage every 4 turns",
            "assets/boss/final.gif"
    };

    private int     currentBossIndex = 0; // 0-4 miniboss, 5 = minifinal, 6 = final
    private int     coins            = 0;
    private int     battlesWon       = 0;
    private int     playerHp;
    private int     playerMaxHp;
    private boolean miniFinalDefeated = false;
    private boolean finalDefeated     = false;

    // ── Shop inventory (item index → count) ───────────────────────────────────
    private int[] shopInventory = new int[8]; // indices match shop items

    public ArcadeManager(GameWindow gameWindow, int playerIndex) {
        this.gameWindow  = gameWindow;
        this.playerIndex = playerIndex;

        // Set player HP from character data
        String[][] chars = ArcadeBattleScreen.CHARACTERS;
        playerMaxHp = Integer.parseInt(chars[playerIndex][2]);
        playerHp    = playerMaxHp;
    }

    public void startNext() {
        if (currentBossIndex < 5) {
            // Mini boss battle
            String[] boss = MINIBOSSES[currentBossIndex];
            gameWindow.switchScreen(new ArcadeBattleScreen(gameWindow, this, playerIndex, boss, false, false));
        } else if (currentBossIndex == 5) {
            // Mini-final boss
            gameWindow.switchScreen(new ArcadeBattleScreen(gameWindow, this, playerIndex, MINIFINAL_BOSS, true, false));
        } else {
            // Final boss
            gameWindow.switchScreen(new ArcadeBattleScreen(gameWindow, this, playerIndex, FINAL_BOSS, false, true));
        }
    }

    public void onBattleWon() {
        battlesWon++;
        currentBossIndex++;

        // Coin rewards
        int earned;
        if (currentBossIndex <= 5) {
            int[] rewards = {50, 85, 130, 190, 265, 355};
            earned = rewards[Math.min(battlesWon - 1, rewards.length - 1)];
        } else {
            earned = 1000;
        }
        coins += earned;

        // Partial HP recovery after each win
        playerHp = Math.min(playerMaxHp, playerHp + 15);

        // Check if all done
        if (currentBossIndex > 6) {
            finalDefeated = true;
            gameWindow.switchScreen(new ArcadeVictoryScreen(gameWindow, coins, battlesWon));
        } else {
            // Go to shop
            gameWindow.switchScreen(new ArcadeShopScreen(gameWindow, this));
        }
    }

    public void onBattleLost() {
        gameWindow.switchScreen(new ArcadeGameOverScreen(gameWindow, coins, battlesWon));
    }

    public void onShopDone() {
        startNext();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public int  getCoins()         { return coins; }
    public void addCoins(int amt)  { coins += amt; }
    public void spendCoins(int amt){ coins -= amt; }
    public int  getBattlesWon()    { return battlesWon; }
    public int  getPlayerHp()      { return playerHp; }
    public void setPlayerHp(int hp){ playerHp = Math.min(playerMaxHp, Math.max(0, hp)); }
    public int  getPlayerMaxHp()   { return playerMaxHp; }
    public void setPlayerMaxHp(int v){ playerMaxHp = v; }
    public int  getPlayerIndex()   { return playerIndex; }
    public int  getCurrentBossIndex(){ return currentBossIndex; }
    public int[] getShopInventory(){ return shopInventory; }
}