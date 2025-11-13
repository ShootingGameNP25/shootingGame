import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel {
    // === 기존 필드 ===
    private Boss boss;
    private Player player;
    private BossAttack bossAttack;
    private GameLoop gameLoop;
    private int gameLevel;
    private GameFrame gameFrame;
    private LevelPanel level;

    // === 배경 스크롤 ===
    private Image bgImg;
    private int bgY1 = 0;
    private int bgY2 = -1;
    private int bgSpeed = 10;

    // === 비행기(플레이어 스프라이트) ===
    private Image planeImg;          // src/plane.png
    private int planeW = 64;         // 그릴 크기(픽셀)
    private int planeH = 64;
    private int planeX = 0;          // 좌상단 기준 위치
    private int planeY = 0;
    private int planeSpeed = 5;      // 이동 속도

    // 입력 상태
    private boolean up, down, left, right;

    // === 메인 타이머(60fps): 배경 스크롤 + 플레이어 이동 + 리페인트 ===
    private final Timer mainTimer;

    public GamePanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setOpaque(true);
        setFocusable(true);

        // 배경/스프라이트 이미지 로드
        bgImg    = new ImageIcon("src/backingame.png").getImage();
        planeImg = new ImageIcon("src/plane.png").getImage();

        // 보스(기존 구조 유지)
        boss = new Boss(200, 50, "src/boss.jpg");           // 
        bossAttack = new BossAttack(boss);

        // 패널 리사이즈되면 배경/비행기 기준점 맞추기
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int h = getHeight();
                bgY2 = bgY1 - h;

                // 비행기 시작 위치: 화면 하단 중앙 근처
                planeX = (getWidth()  - planeW) / 2;
                planeY = (getHeight() - planeH) - 40;
            }
        });

        // 키 바인딩(포커스 문제 적고, 눌림/뗌 구분 가능)
        setupKeyBindings();

        // 60fps 메인 타이머
        mainTimer = new Timer(16, ev -> {
            // 배경 스크롤
            bgY1 += bgSpeed;
            bgY2 += bgSpeed;
            int h = getHeight();
            if (bgY1 >= h) bgY1 = bgY2 - h;
            if (bgY2 >= h) bgY2 = bgY1 - h;

            // 입력 상태에 따라 비행기 이동
            int dx = 0, dy = 0;
            if (up)    dy -= planeSpeed;
            if (down)  dy += planeSpeed;
            if (left)  dx -= planeSpeed;
            if (right) dx += planeSpeed;

            planeX += dx;
            planeY += dy;

            // 화면 경계 클램프
            planeX = Math.max(0, Math.min(getWidth()  - planeW, planeX));
            planeY = Math.max(0, Math.min(getHeight() - planeH, planeY));

            repaint();
        });
    }

    /** GameFrame에서 게임 시작 시 호출 */
    public void startGame() {
        if (!mainTimer.isRunning()) mainTimer.start();
        if (!bossAttack.isAlive())  bossAttack.start();  // 기존 구조 유지 
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
    }

    /** 일시정지/종료 시(원하면 사용) */
    public void stopGame() {
        if (mainTimer.isRunning()) mainTimer.stop();
    }

    /** 배경 스크롤 속도 조정(옵션) */
    public void setBgSpeed(int pixelsPerFrame) { bgSpeed = pixelsPerFrame; }

    /** 키 바인딩 등록 */
    private void setupKeyBindings() {
        int WHEN_FOCUSED = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap im = getInputMap(WHEN_FOCUSED);
        ActionMap am = getActionMap();

        // ★ 키가 눌릴 때
        im.put(KeyStroke.getKeyStroke("pressed W"), "pressW");
        im.put(KeyStroke.getKeyStroke("pressed A"), "pressA");
        im.put(KeyStroke.getKeyStroke("pressed S"), "pressS");
        im.put(KeyStroke.getKeyStroke("pressed D"), "pressD");

        // ★ 키가 떼질 때
        im.put(KeyStroke.getKeyStroke("released W"), "releaseW");
        im.put(KeyStroke.getKeyStroke("released A"), "releaseA");
        im.put(KeyStroke.getKeyStroke("released S"), "releaseS");
        im.put(KeyStroke.getKeyStroke("released D"), "releaseD");

        am.put("pressW",   new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { up = true; }});
        am.put("pressA",   new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { left = true; }});
        am.put("pressS",   new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { down = true; }});
        am.put("pressD",   new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { right = true; }});

        am.put("releaseW", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { up = false; }});
        am.put("releaseA", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { left = false; }});
        am.put("releaseS", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { down = false; }});
        am.put("releaseD", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { right = false; }});
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // === 배경 두 장 이어 그리기 ===
        int w = getWidth(), h = getHeight();
        g.drawImage(bgImg, 0, bgY1, w, h, null);
        g.drawImage(bgImg, 0, bgY2, w, h, null);

        // === 게임 오브젝트 ===
        boss.bossDraw(g); // 기존 보스 렌더 

        // === 플레이어 비행기 ===
        // plane.png 실제 픽셀 크기를 그대로 쓰고 싶으면 planeW/planeH 대신 planeImg의 원본 크기를 사용하세요.
        g.drawImage(planeImg, planeX, planeY, planeW, planeH, null);
    }

    // ========== 기존 내부 클래스들 ==========
    class BossAttack extends Thread {
        private Boss boss;
        public BossAttack(Boss boss) { this.boss = boss; }

        @Override public void run() {
            System.out.println("보스 등장함!");
            while (true) {
                try { sleep(100); } catch (InterruptedException e) { return; }
            }
        }
    }

    class GameLoop extends Thread {
        @Override public void run() {
            while (true) {
                // 필요 시 충돌/스폰 등 전체 루프
            }
        }
    }

    class Boss {
        private int x, y;
        private int width = 300, height = 150;
        private Image img;

        public Boss(int x, int y, String imagePath) {
            this.x = x; this.y = y;
            this.img = new ImageIcon(imagePath).getImage();
        }

        public void bossDraw(Graphics g) {
            g.drawImage(img, x, y, width, height, null);
        }
    }

    class Player {
        private int heart;
        public Player(int gameLevel) {
            if (gameLevel == 1) heart = 5;
            else if (gameLevel == 2) heart = 4;
            else if (gameLevel == 3) heart = 3;
        }
    }
}
