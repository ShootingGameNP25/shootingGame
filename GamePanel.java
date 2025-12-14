// 475번째 줄 50으로 수정 (사용자 메모 유지)
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * GamePanel
 *  - 좌측: GameArea(실제 게임)
 *  - 우측 상단: PlayerPanel(gameMode=true) 2인 HUD
 *  - 우측 하단: ChatPanel
 */
public class GamePanel extends JPanel {

    private GameFrame gameFrame;

    // 네트워크 클라이언트(내 좌표/HP/점수 전송, 상대 좌표/HP/점수 수신 반영)
    private GameClient client;

    private GameArea gameArea;
    private PlayerPanel rightTop;
    private ChatPanel rightBottom;

    public GamePanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setLayout(new BorderLayout());

        gameArea = new GameArea();
        rightTop = new PlayerPanel(gameFrame, true);
        rightBottom = new ChatPanel();

        JSplitPane verticalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                rightTop,
                rightBottom
        );
        verticalSplit.setDividerLocation(500);
        verticalSplit.setDividerSize(4);

        JSplitPane horizonSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                gameArea,
                verticalSplit
        );
        horizonSplit.setDividerLocation(700);
        horizonSplit.setDividerSize(4);

        add(horizonSplit, BorderLayout.CENTER);
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    public PlayerPanel getRightTopPlayerPanel() {
        return rightTop;
    }

    public ChatPanel getChatPanel() {
        return rightBottom;
    }

    /** 서버에서 받은 상대(다른 유저) 비행기 좌표를 게임 화면에 반영 */
    public void updateOtherPlane(String name, int x, int y) {
        if (gameArea != null) {
            gameArea.updateOtherPlane(name, x, y);
        }
    }

    /** 서버에서 받은 HP 반영 (우측 HUD) */
    public void updateHpById(String id, int hp, int maxHp) {
        if (rightTop != null) rightTop.updateHpById(id, hp, maxHp);
    }

    /** 서버에서 받은 점수 반영 (우측 HUD) */
    public void updateScoreById(String id, int score) {
        if (rightTop != null) rightTop.updateScoreById(id, score);
    }

    public void startGame() {
        if (gameArea != null) gameArea.startGame();
    }

    public void stopGame() {
        if (gameArea != null) gameArea.stopGame();
    }

    /**
     * ✅ 핵심: “재시작”용
     * 이전 게임 화면이 남지 않게 모든 상태 초기화 후 시작
     */
    public void resetAndStartGame() {
        if (gameArea != null) {
            gameArea.resetGameState();
            gameArea.startGame();
        }
    }

    // =====================================================================
    //                           GAME AREA
    // =====================================================================
    class GameArea extends JPanel {

        // ===== Player
        private Player player;
        private int gameLevel = 1;

        // ===== Score
        private int score = 0;

        // ===== Background
        private Image bgImg;
        private int bgY1 = 0, bgY2 = -1;
        private int bgSpeed = 10;

        // ===== Player Image
        private Image planeImg;
        private int planeW = 64, planeH = 64;
        private int planeX = 0, planeY = 0;

        // ===== Other Player (상대 비행기) =====
        private int otherPlaneX = -1, otherPlaneY = -1;
        private boolean otherPlaneVisible = false;
        private String otherPlayerName = null;

        // ===== 좌표 전송 최적화 =====
        private int lastSentX = Integer.MIN_VALUE;
        private int lastSentY = Integer.MIN_VALUE;
        private int posSendCooldown = 0;
        private final int POS_SEND_INTERVAL_FRAMES = 2;

        private int planeSpeed = 5;

        private boolean up, down, left, right;

        // ===== Bullets =====
        private Image bulletImg;
        private int bulletW = 16, bulletH = 32;
        private int bulletSpeed = 15;

        private int bulletDamage = 1;
        private int bulletCount = 1;

        private List<Bullet> bullets = new ArrayList<>();
        private int fireCooldown = 0;
        private int fireIntervalFrames = 10;

        // ===== Monsters =====
        private Image monsterImg;
        private List<Monster> monsters = new ArrayList<>();

        private int monsterSpeed = 1;
        private int monstersPerRow = 5;

        private int monsterRowInterval = 240;
        private int monsterRowTimer = 0;

        private int monstersKilled = 0;

        // ===== Boss =====
        private Boss boss;
        private BossAttack bossAttack;
        private boolean bossAppeared = false;

        private Image bossBulletImg;
        private List<BossBullet> bossBullets = new ArrayList<>();
        private int bossBulletSize = 32;
        private double bossBulletSpeed = 10.0;

        private int bossFireCooldown = 0;
        private int bossFireMinInterval = 20;
        private int bossFireMaxInterval = 60;

        private Random random = new Random();

        // ===== Player hit
        private int playerInvincibleFrames = 0;

        // ===== Upgrade UI =====
        private UpgradePanel upgradePanel;
        private boolean upgradeShown = false;
        private Timer upgradeTimer;

        // ===== Main Timer
        private Timer mainTimer;

        // ===== GameOver 전송 1회만 =====
        private boolean gameOverSent = false;

        public GameArea() {
            setOpaque(true);
            setFocusable(true);
            setRequestFocusEnabled(true);

            // Load images
            bgImg = new ImageIcon("backingame.png").getImage();
            planeImg = new ImageIcon("plane.png").getImage();
            bulletImg = new ImageIcon("bullet.png").getImage();
            monsterImg = new ImageIcon("monster.png").getImage();
            bossBulletImg = new ImageIcon("bossbullet.png").getImage();

            // Boss
            boss = new Boss(200, 50, "boss.png");
            bossAttack = new BossAttack(boss);

            // Player
            player = new Player(gameLevel);

            // 클릭하면 게임 영역이 포커스를 가져감
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                }
            });

            // Resize event
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    int h = getHeight();
                    bgY2 = bgY1 - h;

                    planeX = (getWidth() - planeW) / 2;
                    planeY = (getHeight() - planeH) - 40;

                    if (upgradePanel != null) {
                        upgradePanel.setBounds(0, 0, getWidth(), getHeight());
                    }
                }
            });

            setupKeyBindings();
            resetBossFireCooldown();

            mainTimer = new Timer(16, ev -> gameLoop());

            // ===== 업그레이드 패널 생성 및 리스너 연결 =====
            setLayout(null);

            upgradePanel = new UpgradePanel(new UpgradePanel.UpgradeListener() {
                @Override
                public void onBulletCountUp() {
                    bulletCount++;
                    rightTop.addUpgradeIcon(upgradePanel.getIconBullet());
                }

                @Override
                public void onFireSpeedUp() {
                    if (fireIntervalFrames > 3)
                        fireIntervalFrames -= 3;
                    rightTop.addUpgradeIcon(upgradePanel.getIconSpeed());
                }

                @Override
                public void onDamageUp() {
                    bulletDamage++;
                    rightTop.addUpgradeIcon(upgradePanel.getIconDamage());
                }
            });

            upgradePanel.setBounds(0, 0, getWidth(), getHeight());
            add(upgradePanel);

            upgradeTimer = new Timer(10_000, e -> {
                hideUpgradePanel();
                upgradeTimer.stop();
            });
            upgradeTimer.setRepeats(false);
        }

        /** ✅ 재시작용: 내부 게임 상태 싹 초기화 */
        public void resetGameState() {
            // 타이머/업그레이드 정리
            try {
                if (mainTimer != null && mainTimer.isRunning()) mainTimer.stop();
            } catch (Exception ignore) {}

            try {
                if (upgradeTimer != null && upgradeTimer.isRunning()) upgradeTimer.stop();
            } catch (Exception ignore) {}

            upgradeShown = false;
            if (upgradePanel != null) upgradePanel.setVisible(false);

            // 점수/레벨 초기화
            score = 0;
            gameLevel = 1;

            // 플레이어 초기화
            player = new Player(gameLevel);
            gameOverSent = false;
            playerInvincibleFrames = 0;

            // 내 비행기 위치 초기화
            planeX = Math.max(0, (getWidth() - planeW) / 2);
            planeY = Math.max(0, (getHeight() - planeH) - 40);

            up = down = left = right = false;

            // 상대 비행기 초기화
            otherPlaneVisible = false;
            otherPlaneX = otherPlaneY = -1;
            otherPlayerName = null;

            // 좌표 전송 최적화 변수 초기화
            lastSentX = Integer.MIN_VALUE;
            lastSentY = Integer.MIN_VALUE;
            posSendCooldown = 0;

            // 탄환/몬스터 초기화
            bullets.clear();
            monsters.clear();

            // 몬스터/보스 관련 초기화
            monstersKilled = 0;
            monsterRowTimer = 0;

            bossAppeared = false;
            bossBullets.clear();
            boss.reset();
            resetBossFireCooldown();

            // 보스 스레드는 새로
            bossAttack = new BossAttack(boss);

            // 업그레이드 값 초기화
            bulletDamage = 1;
            bulletCount = 1;
            fireCooldown = 0;
            fireIntervalFrames = 10;

            // HUD 초기화 + 서버 전송
            if (rightTop != null) {
                rightTop.setHp(player.getHp(), player.getMaxHp());
                rightTop.setScore(0);
            }

            if (GamePanel.this.client != null) {
                GamePanel.this.client.sendHp(player.getHp(), player.getMaxHp());
                GamePanel.this.client.sendScore(0);
            }

            repaint();
        }

        public void startGame() {
            // 시작 시 내 HP/점수 로컬 표시 + 서버 전송
            rightTop.setHp(player.getHp(), player.getMaxHp());
            rightTop.setScore(0);

            if (GamePanel.this.client != null) {
                GamePanel.this.client.sendHp(player.getHp(), player.getMaxHp());
                GamePanel.this.client.sendScore(0);
            }

            if (!mainTimer.isRunning()) mainTimer.start();
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        public void stopGame() {
            if (mainTimer.isRunning()) mainTimer.stop();
        }

        // ✅ 상대 비행기 좌표 반영
        public void updateOtherPlane(String name, int x, int y) {
            try {
                if (GamePanel.this.client != null && name != null
                        && name.equals(GamePanel.this.client.getUserName())) {
                    return;
                }
            } catch (Exception ignore) {}

            this.otherPlayerName = name;
            this.otherPlaneX = x;
            this.otherPlaneY = y;
            this.otherPlaneVisible = true;
            repaint();
        }

        // ========================= 업그레이드 패널 제어 =========================
        private void showUpgradePanel() {
            upgradeShown = true;
            mainTimer.stop();

            upgradePanel.resetState();
            upgradePanel.setBounds(0, 0, getWidth(), getHeight());
            upgradePanel.setVisible(true);
            upgradePanel.repaint();

            if (upgradeTimer != null) {
                upgradeTimer.restart();
            }
        }

        private void hideUpgradePanel() {
            upgradePanel.setVisible(false);
            upgradeShown = false;

            if (upgradeTimer != null && upgradeTimer.isRunning()) {
                upgradeTimer.stop();
            }

            mainTimer.start();
        }

        // ========================= 키 바인딩 =========================
        private void setupKeyBindings() {
            InputMap im = getInputMap(WHEN_FOCUSED);
            ActionMap am = getActionMap();

            im.put(KeyStroke.getKeyStroke("pressed A"), "left_pressed");
            im.put(KeyStroke.getKeyStroke("released A"), "left_released");
            am.put("left_pressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { left = true; }
            });
            am.put("left_released", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { left = false; }
            });

            im.put(KeyStroke.getKeyStroke("pressed D"), "right_pressed");
            im.put(KeyStroke.getKeyStroke("released D"), "right_released");
            am.put("right_pressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { right = true; }
            });
            am.put("right_released", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { right = false; }
            });

            im.put(KeyStroke.getKeyStroke("pressed W"), "up_pressed");
            im.put(KeyStroke.getKeyStroke("released W"), "up_released");
            am.put("up_pressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { up = true; }
            });
            am.put("up_released", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { up = false; }
            });

            im.put(KeyStroke.getKeyStroke("pressed S"), "down_pressed");
            im.put(KeyStroke.getKeyStroke("released S"), "down_released");
            am.put("down_pressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { down = true; }
            });
            am.put("down_released", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { down = false; }
            });

            im.put(KeyStroke.getKeyStroke("pressed SPACE"), "shoot");
            am.put("shoot", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shootManual();
                }
            });
        }

        private void shootManual() {
            if (upgradeShown) return;
            fireBulletFromPlane();
        }

        // ========================= 게임 루프 =========================
        private void gameLoop() {
            if (upgradeShown) return;

            // 배경 스크롤
            bgY1 += bgSpeed;
            bgY2 += bgSpeed;

            int h = getHeight();
            if (bgY1 >= h) bgY1 = bgY2 - h;
            if (bgY2 >= h) bgY2 = bgY1 - h;

            // 플레이어 이동
            int dx = 0, dy = 0;
            if (up) dy -= planeSpeed;
            if (down) dy += planeSpeed;
            if (left) dx -= planeSpeed;
            if (right) dx += planeSpeed;

            planeX = Math.max(0, Math.min(getWidth() - planeW, planeX + dx));
            planeY = Math.max(0, Math.min(getHeight() - planeH, planeY + dy));

            // 내 좌표를 서버로 전송
            if (GamePanel.this.client != null) {
                if (posSendCooldown > 0) posSendCooldown--;
                boolean changed = (planeX != lastSentX) || (planeY != lastSentY);
                if (changed && posSendCooldown == 0) {
                    GamePanel.this.client.sendMove(planeX, planeY);
                    lastSentX = planeX;
                    lastSentY = planeY;
                    posSendCooldown = POS_SEND_INTERVAL_FRAMES;
                }
            }

            // 몬스터 스폰
            monsterRowTimer++;
            if (!bossAppeared && monsterRowTimer >= monsterRowInterval) {
                spawnMonsterRow();
                monsterRowTimer = 0;
            }

            updateMonsters();

            // 자동총알
            fireCooldown++;
            if (fireCooldown >= fireIntervalFrames) {
                fireBulletFromPlane();
                fireCooldown = 0;
            }

            updateBullets();
            updateBossFire();
            updateBossBullets();

            checkPlayerHitByBossBody();
            checkPlayerHitByMonster();

            if (playerInvincibleFrames > 0) playerInvincibleFrames--;

            // 게임오버 처리
            if (player.isDead()) {
                if (!gameOverSent && GamePanel.this.client != null) {
                    gameOverSent = true;
                    GamePanel.this.client.sendGameOver(score);
                }
                stopGame();
            }

            repaint();
        }

        // ========================= 몬스터 =========================
        private void spawnMonsterRow() {
            int mW = 64, mH = 64;

            int panelWidth = getWidth();
            if (panelWidth <= 0) panelWidth = 700;

            int spacing = 20;
            int totalWidth = mW * monstersPerRow + spacing * (monstersPerRow - 1);

            int startX = (panelWidth - totalWidth) / 2;
            int y = 30;

            for (int i = 0; i < monstersPerRow; i++) {
                int x = startX + i * (mW + spacing);
                monsters.add(new Monster(x, y, mW, mH));
            }
        }

        private void updateMonsters() {
            int panelHeight = getHeight();

            Iterator<Monster> it = monsters.iterator();
            while (it.hasNext()) {
                Monster m = it.next();
                m.y += monsterSpeed;

                if (m.y > panelHeight) {
                    it.remove();
                }
            }
        }

        private void checkPlayerHitByMonster() {
            Rectangle playerRect = new Rectangle(planeX, planeY, planeW, planeH);

            if (playerInvincibleFrames > 0) return;

            for (Monster m : monsters) {
                if (playerRect.intersects(m.getBounds())) {
                    applyDamageToPlayer(1);
                    break;
                }
            }
        }

        private void applyDamageToPlayer(int dmg) {
            if (playerInvincibleFrames > 0) return;

            player.hit(dmg);
            playerInvincibleFrames = 30;

            rightTop.setHp(player.getHp(), player.getMaxHp());

            if (GamePanel.this.client != null) {
                GamePanel.this.client.sendHp(player.getHp(), player.getMaxHp());
            }
        }

        // ========================= 총알 =========================
        private void fireBulletFromPlane() {
            int baseX = planeX + planeW / 2 - bulletW / 2;
            int baseY = planeY - bulletH;

            if (bulletCount == 1) {
                bullets.add(new Bullet(baseX, baseY, 0));
                return;
            }

            int spread = 3;
            for (int i = 0; i < bulletCount; i++) {
                int offset = (i - bulletCount / 2) * spread;
                bullets.add(new Bullet(baseX, baseY, offset));
            }
        }

        private void updateBullets() {
            Rectangle bossRect = (bossAppeared && !boss.isDead())
                    ? boss.getHitBounds()
                    : null;

            Iterator<Bullet> it = bullets.iterator();
            while (it.hasNext()) {
                Bullet b = it.next();

                b.y -= bulletSpeed;
                b.x += b.vx;

                if (b.y + bulletH < 0) {
                    it.remove();
                    continue;
                }

                Rectangle bulletRect = new Rectangle(b.x, b.y, bulletW, bulletH);

                boolean hit = false;

                Iterator<Monster> mit = monsters.iterator();
                while (mit.hasNext()) {
                    Monster m = mit.next();

                    if (bulletRect.intersects(m.getBounds())) {
                        m.hit(bulletDamage);

                        if (m.isDead()) {
                            mit.remove();
                            monstersKilled++;

                            score += 10;
                            rightTop.setScore(score);
                            if (GamePanel.this.client != null) GamePanel.this.client.sendScore(score);

                            if (!bossAppeared && monstersKilled >= 10) {
                                bossAppeared = true;

                                boss.reset();
                                bossBullets.clear();

                                bossAttack = new BossAttack(boss);
                                bossAttack.start();
                            }
                        }

                        it.remove();
                        hit = true;
                        break;
                    }
                }

                if (hit) continue;

                if (bossRect != null && bulletRect.intersects(bossRect)) {
                    boss.hit(1);
                    it.remove();

                    if (boss.isDead()) {
                        score += 100;
                        rightTop.setScore(score);
                        if (GamePanel.this.client != null) GamePanel.this.client.sendScore(score);

                        bossAppeared = false;
                        bossBullets.clear();
                        monstersKilled = 0;

                        showUpgradePanel();
                    }
                }
            }
        }

        // ========================= 보스 =========================
        private void resetBossFireCooldown() {
            bossFireCooldown = bossFireMinInterval +
                    random.nextInt(bossFireMaxInterval - bossFireMinInterval + 1);
        }

        private void updateBossFire() {
            if (!bossAppeared || boss.isDead()) return;

            bossFireCooldown--;

            if (bossFireCooldown <= 0) {
                fireBossBullet();
                resetBossFireCooldown();
            }
        }

        private void fireBossBullet() {
            int bossCenterX = boss.getX() + boss.getWidth() / 2;
            int bossCenterY = boss.getY() + boss.getHeight();

            int playerCenterX = planeX + planeW / 2;
            int playerCenterY = planeY + planeH / 2;

            double dx = playerCenterX - bossCenterX;
            double dy = playerCenterY - bossCenterY;

            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0) len = 1;
            dx /= len;
            dy /= len;

            double vx = dx * bossBulletSpeed;
            double vy = dy * bossBulletSpeed;

            bossBullets.add(new BossBullet(
                    bossCenterX - bossBulletSize / 2.0,
                    bossCenterY - bossBulletSize / 2.0,
                    vx, vy
            ));
        }

        private void updateBossBullets() {
            if (!bossAppeared) return;

            Rectangle playerRect = new Rectangle(planeX, planeY, planeW, planeH);

            Iterator<BossBullet> it = bossBullets.iterator();
            while (it.hasNext()) {
                BossBullet b = it.next();
                b.x += b.vx;
                b.y += b.vy;

                if (b.x + bossBulletSize < 0 || b.x > getWidth()
                        || b.y + bossBulletSize < 0 || b.y > getHeight()) {
                    it.remove();
                    continue;
                }

                if (playerInvincibleFrames <= 0 &&
                        b.getBounds().intersects(playerRect)) {
                    applyDamageToPlayer(1);
                    it.remove();
                }
            }
        }

        private void checkPlayerHitByBossBody() {
            if (!bossAppeared || boss.isDead()) return;

            Rectangle playerRect = new Rectangle(planeX, planeY, planeW, planeH);
            Rectangle bossRect = boss.getBounds();

            if (playerInvincibleFrames > 0) return;

            if (playerRect.intersects(bossRect)) {
                applyDamageToPlayer(10);
            }
        }

        // ========================= 렌더링 =========================
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth(), h = getHeight();

            g.drawImage(bgImg, 0, bgY1, w, h, null);
            g.drawImage(bgImg, 0, bgY2, w, h, null);

            // 몬스터
            for (Monster m : monsters) {
                if (monsterImg != null)
                    g.drawImage(monsterImg, m.x, m.y, m.w, m.h, null);
                else {
                    g.setColor(Color.GREEN);
                    g.fillRect(m.x, m.y, m.w, m.h);
                }

                int barW = m.w;
                int barH = 6;
                int barX = m.x;
                int barY = m.y - barH - 2;

                double ratio = (double) m.hp / m.maxHp;
                int fill = (int) (barW * ratio);

                g.setColor(Color.RED);
                g.fillRect(barX, barY, fill, barH);

                g.setColor(Color.WHITE);
                g.drawRect(barX, barY, barW, barH);
            }

            if (bossAppeared && !boss.isDead()) boss.bossDraw(g);

            // 상대 비행기
            if (otherPlaneVisible && otherPlaneX >= 0 && otherPlaneY >= 0) {
                g.drawImage(planeImg, otherPlaneX, otherPlaneY, planeW, planeH, null);
            }

            // 내 비행기
            g.drawImage(planeImg, planeX, planeY, planeW, planeH, null);

            for (Bullet b : bullets) {
                g.drawImage(bulletImg, b.x, b.y, bulletW, bulletH, null);
            }

            for (BossBullet b : bossBullets) {
                g.drawImage(bossBulletImg, (int) b.x, (int) b.y,
                        bossBulletSize, bossBulletSize, null);
            }
        }

        // ========================= 내부 클래스들 =========================
        class Bullet {
            int x, y;
            int vx;

            Bullet(int x, int y, int vx) {
                this.x = x;
                this.y = y;
                this.vx = vx;
            }
        }

        class BossBullet {
            double x, y, vx, vy;

            BossBullet(double x, double y, double vx, double vy) {
                this.x = x; this.y = y;
                this.vx = vx; this.vy = vy;
            }

            Rectangle getBounds() {
                return new Rectangle((int) x, (int) y, bossBulletSize, bossBulletSize);
            }
        }

        class Monster {
            int x, y, w, h;
            int hp, maxHp;

            Monster(int x, int y, int w, int h) {
                this.x = x; this.y = y;
                this.w = w; this.h = h;
                this.maxHp = 5;
                this.hp = maxHp;
            }

            void hit(int damage) {
                hp -= damage;
                if (hp < 0) hp = 0;
            }

            boolean isDead() { return hp <= 0; }

            Rectangle getBounds() {
                return new Rectangle(x, y, w, h);
            }
        }

        class BossAttack extends Thread {
            private Boss boss;

            public BossAttack(Boss boss) {
                this.boss = boss;
            }

            @Override
            public void run() {
                while (!boss.isDead()) {
                    try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                }
            }
        }

        class Boss {
            private int x, y, width, height;
            private Image img;

            private int maxHp = 100;
            private int hp = maxHp;

            public Boss(int x, int y, String path) {
                this.x = x;
                this.y = y;
                this.img = new ImageIcon(path).getImage();

                int iw = img.getWidth(null);
                int ih = img.getHeight(null);

                width = 300;
                double scale = width / (double) iw;
                height = (int) (ih * scale);
            }

            public void reset() {
                hp = maxHp;
            }

            public void bossDraw(Graphics g) {
                g.drawImage(img, x, y, width, height, null);

                int barMax = width;
                int barH = 10;
                int fill = (int) (barMax * (hp / (double) maxHp));

                int barX = x;
                int barY = y - barH - 5;

                g.setColor(Color.RED);
                g.fillRect(barX, barY, fill, barH);

                g.setColor(Color.WHITE);
                g.drawRect(barX, barY, barMax, barH);
            }

            public Rectangle getBounds() {
                return new Rectangle(x, y, width, height);
            }

            public Rectangle getHitBounds() {
                int mx = (int) (width * 0.15);
                int my = (int) (height * 0.2);
                return new Rectangle(
                        x + mx,
                        y + my,
                        width - mx * 2,
                        height - my * 2
                );
            }

            public void hit(int dmg) {
                hp -= dmg;
                if (hp < 0) hp = 0;
            }

            public boolean isDead() { return hp <= 0; }

            public int getX() { return x; }
            public int getY() { return y; }
            public int getWidth() { return width; }
            public int getHeight() { return height; }
        }

        class Player {
            private int hp, maxHp;

            Player(int lv) {
                maxHp = hp = 5;
            }

            void hit(int d) {
                hp -= d;
                if (hp < 0) hp = 0;
            }

            int getHp() { return hp; }
            int getMaxHp() { return maxHp; }

            boolean isDead() { return hp <= 0; }
        }
    } // GameArea end
} // GamePanel end
