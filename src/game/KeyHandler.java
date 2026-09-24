package game;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class KeyHandler implements KeyListener {

    // volatile: di-set thread UI (Swing), dibaca thread game
    public volatile boolean upPressed, downPressed, leftPressed, rightPressed, numPressed;

    public volatile boolean enterPressed, pausePressed;
    public volatile int numkey; // nyimpen nomor yg di teka

    public KeyHandler() {

    }

    // Hapus semua input yang belum diproses (dipanggil saat mulai game baru)
    public void reset() {
        upPressed = downPressed = leftPressed = rightPressed = numPressed = enterPressed = pausePressed = false;
        numkey = 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_UP:
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = true;
                break;
            case KeyEvent.VK_ENTER:
                enterPressed = true;
                break;
            case KeyEvent.VK_P:
            case KeyEvent.VK_ESCAPE:
                pausePressed = true;
                break;
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
            case KeyEvent.VK_6:
            case KeyEvent.VK_7:
                numkey = keyCode - KeyEvent.VK_0; // numkey di-set dulu sebelum numPressed
                numPressed = true;
                break;
        }
    }

    // Tombol tidak di-reset saat dilepas; GameLevel yang me-reset setelah tombol diproses,
    // jadi tekanan singkat tidak hilang.
    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
