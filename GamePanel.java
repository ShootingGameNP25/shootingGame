// 475번쨰 줄 50으로 수정
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

public class GamePanel extends JPanel {

    private GameFrame gameFrame;

    private GameArea gameArea;
    private PlayerPanel rightTop;
    private ChatPanel rightBottom;

    public GamePanel(GameFrame gameFrame) { // GameFrame gameFrame, ChatPanel sharedChatPanel
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

    public void startGame() {
        if (gameArea != null) gameArea.startGame();
    }

    public void stopGame() {
        if (gameArea != null) gameArea.stopGame();
    }

    public void setPlayerNickname(String nickname) {
        if (rightTop != null) rightTop.setNickname(nickname);
    }
    
    public ChatPanel getChatPanel() {
    	return rightBottom;
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

                    // 업그레이드 패널도 전체를 덮도록 사이즈 조정
                    if (upgradePanel != null) {
                        upgradePanel.setBounds(0, 0, getWidth(), getHeight());
                    }
                }
            });

            setupKeyBindings();
            resetBossFireCooldown();

            mainTimer = new Timer(16, ev -> gameLoop());

            // ===== 업그레이드 패널 생성 및 리스너 연결 =====
            setLayout(null); // GameArea에 직접 자식 컴포넌트(UpgradePanel) 올리기

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

            // 10초 업그레이드 선택 타이머
            upgradeTimer = new Timer(10_000, e -> {
                hideUpgradePanel();
                upgradeTimer.stop();
            });
            upgradeTimer.setRepeats(false);
        }

        public void startGame() {
            rightTop.setHp(player.getHp(), player.getMaxHp());
            rightTop.setScore(0);

            if (!mainTimer.isRunning()) mainTimer.start();
            SwingUtilities.invokeLater(() -> requestFocusInWindow());
        }

        public void stopGame() {
            if (mainTimer.isRunning()) mainTimer.stop();
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

            if (upgradeTimer.isRunning()) {
                upgradeTimer.stop();
            }

            mainTimer.start(); // 게임 재시작
        }

        // ========================= 키 바인딩 =========================
        private void setupKeyBindings() {
            InputMap im = getInputMap(WHEN_FOCUSED); // WHEN_IN_FOCUSED_WINDOW
            ActionMap am = getActionMap();

            // LEFT
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

            // RIGHT
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

            // UP
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

            // DOWN
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

            // SHOOT
            im.put(KeyStroke.getKeyStroke("pressed SPACE"), "shoot");
            am.put("shoot", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shootManual();
                }
            });
        }

        private void shootManual() {
            if (upgradeShown) return;  // 업그레이드 중엔 발사 막기
            fireBulletFromPlane();
        }

        // ========================= 게임 루프 =========================
        private void gameLoop() {
            if (upgradeShown) return; // 업그레이드 화면 뜨면 게임 중지

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

            if (player.isDead()) {
                stopGame();
                System.out.println("GAME OVER");
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

            Iterator<Monster> it = monsters.iterator();
            while (it.hasNext()) {
                Monster m = it.next();

                if (playerRect.intersects(m.getBounds())) {
                    player.hit(1);
                    playerInvincibleFrames = 30;

                    rightTop.setHp(player.getHp(), player.getMaxHp());
                }
            }
        }

        // ========================= 총알 여러개 발사 =========================
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

                            // 몬스터 50마리 잡으면 보스 등장
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

                        bossAppeared = false;
                        bossBullets.clear();
                        monstersKilled = 0;

                        showUpgradePanel(); // 업그레이드 패널 띄우기
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

                if (b.x + bossBulletSize < 0 || b.x > getWidth() ||
                        b.y + bossBulletSize < 0 || b.y > getHeight()) {
                    it.remove();
                    continue;
                }

                if (playerInvincibleFrames <= 0 &&
                        b.getBounds().intersects(playerRect)) {

                    player.hit(1);
                    playerInvincibleFrames = 30;

                    rightTop.setHp(player.getHp(), player.getMaxHp());

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
                player.hit(10);
                playerInvincibleFrames = 30;

                rightTop.setHp(player.getHp(), player.getMaxHp());
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
                return new Rectangle((int)x, (int)y, bossBulletSize, bossBulletSize);
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
                int mx = (int)(width * 0.15);
                int my = (int)(height * 0.2);
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
