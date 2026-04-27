import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicManager {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static MusicManager instance;
    public static MusicManager get() {
        if (instance == null) instance = new MusicManager();
        return instance;
    }

    // ── BGM state ─────────────────────────────────────────────────────────────
    private Clip   bgmClip;
    private String currentBgm = "";
    private float  bgmVolume  = 0.8f;

    // ── SFX state ─────────────────────────────────────────────────────────────
    private float sfxVolume = 1.0f;

    // ── BGM file paths ────────────────────────────────────────────────────────
    public static final String BGM_HOME       = "assets/audio/bgm_home.wav";
    public static final String BGM_VERSUS     = "assets/audio/bgm_versus.wav";
    public static final String BGM_BATTLE     = "assets/audio/bgm_battle.wav";
    public static final String BGM_BATTLE_PVC = "assets/audio/bgm_battle_pvc.wav";
    public static final String BGM_ARCADE     = "assets/audio/bgm_arcade.wav";
    public static final String BGM_BOSS       = "assets/audio/bgm_boss.wav";
    public static final String BGM_VICTORY    = "assets/audio/bgm_victory.wav";
    public static final String BGM_GAMEOVER   = "assets/audio/bgm_gameover.wav";

    // ── SFX file paths ────────────────────────────────────────────────────────
    public static final String SFX_ATTACK   = "assets/audio/sfx_attack.wav";
    public static final String SFX_SPECIAL  = "assets/audio/sfx_special.wav";
    public static final String SFX_DEFEND   = "assets/audio/sfx_defend.wav";
    public static final String SFX_HIT      = "assets/audio/sfx_hit.wav";
    public static final String SFX_WILDCARD = "assets/audio/sfx_wildcard.wav";
    public static final String SFX_DICE     = "assets/audio/sfx_dice.wav";
    public static final String SFX_WIN      = "assets/audio/sfx_win.wav";
    public static final String SFX_BUTTON   = "assets/audio/sfx_button.wav";
    public static final String SFX_SELECT   = "assets/audio/sfx_select.wav";

    private MusicManager() {}

    // ─────────────────────────────────────────────────────────────────────────
    //  BGM — looping background music
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Play a WAV file on loop.
     * If the same track is already playing, does nothing.
     */
    public void playBGM(String path) {
        if (path == null) return;

        // Don't restart if already playing the same track
        if (path.equals(currentBgm) && bgmClip != null && bgmClip.isRunning()) return;

        stopBGM();

        try {
            File f = new File(path);
            if (!f.exists()) {
                System.out.println("BGM not found: " + path);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            setClipVolume(bgmClip, bgmVolume);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // loops forever
            bgmClip.start();
            currentBgm = path;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Could not play BGM: " + path + " — " + e.getMessage());
        }
    }

    /** Stop background music. */
    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
        currentBgm = "";
    }

    /** Pause background music (can be resumed). */
    public void pauseBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    /** Resume paused background music. */
    public void resumeBGM() {
        if (bgmClip != null && !bgmClip.isRunning()) {
            bgmClip.start();
        }
    }

    /** Set BGM volume — 0.0 is silent, 1.0 is full. */
    public void setBGMVolume(float vol) {
        bgmVolume = Math.max(0f, Math.min(1f, vol));
        if (bgmClip != null) setClipVolume(bgmClip, bgmVolume);
    }

    public float  getBGMVolume()   { return bgmVolume; }
    public boolean isPlaying()     { return bgmClip != null && bgmClip.isRunning(); }
    public String getCurrentBgm()  { return currentBgm; }

    // ─────────────────────────────────────────────────────────────────────────
    //  SFX — one-shot sound effects
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Play a WAV sound effect once. Non-blocking.
     */
    public void playSFX(String path) {
        if (path == null) return;

        new Thread(() -> {
            try {
                File f = new File(path);
                if (!f.exists()) {
                    System.out.println("SFX not found: " + path);
                    return;
                }

                AudioInputStream ais  = AudioSystem.getAudioInputStream(f);
                Clip             clip = AudioSystem.getClip();
                clip.open(ais);
                setClipVolume(clip, sfxVolume);
                clip.start();

                // Auto-close when playback finishes
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) clip.close();
                });

            } catch (Exception e) {
                System.err.println("SFX error: " + path + " — " + e.getMessage());
            }
        }).start();
    }

    /** Set SFX volume — 0.0 is silent, 1.0 is full. */
    public void setSFXVolume(float vol) { sfxVolume = Math.max(0f, Math.min(1f, vol)); }
    public float getSFXVolume()         { return sfxVolume; }

    // ─────────────────────────────────────────────────────────────────────────
    //  INTERNAL
    // ─────────────────────────────────────────────────────────────────────────

    /** Convert 0.0–1.0 linear volume to decibels and apply to clip. */
    private void setClipVolume(Clip clip, float volume) {
        try {
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = volume <= 0f
                    ? fc.getMinimum()
                    : (float)(Math.log10(volume) * 20.0);
            fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), dB)));
        } catch (IllegalArgumentException ignored) {}
    }

    /** Call this when the game exits. */
    public void shutdown() { stopBGM(); }
}