import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * PlayerPanel
 *  - gameMode=false : 로비(ReadyPanel)에서 플레이어 목록/버튼 표시
 *  - gameMode=true  : 게임 HUD에서 2명(호스트/게스트) 정보 표시
 */
public class PlayerPanel extends Background {

    // 모드 설정: false = 로비, true = 게임
    private final boolean gameMode;
    private GameFrame gameFrame;
    private GameClient client;

    // --------------------------
    // 로비 컴포넌트
    // --------------------------
    private JLabel roomName;
    private JLabel player1;
    private JLabel player2;

    // 로비 버튼
    private JButton startBtn = new JButton("게임 시작");
    private JButton readyBtn = new JButton("게임 준비");
    private JButton outRoomBtn = new JButton("방 나가기");

    // 로비에서 이름 저장
    private String playerOneName = null;
    private String playerTwoName = null;

    // --------------------------
    // 게임 HUD (2명 표시)
    // --------------------------
    // ✅ "방장(위)" UI
    private JLabel hostNameLabel;     // "닉네임 : 닉 (id)"
    private JLabel hostHpLabel;       // "체력 : x / y"
    private JProgressBar hostHpBar;
    private JLabel hostScoreLabel;    // "점수 : n"

    // ✅ "나머지(아래)" UI
    private JLabel guestNameLabel;
    private JLabel guestHpLabel;
    private JProgressBar guestHpBar;
    private JLabel guestScoreLabel;

    // 업그레이드(내 것만 표시)
    private JPanel upgradeIconPanel;

    // ✅ 게임에서 “방장/나머지”를 구분해 저장
    private String hostId = null;     // 방장 아이디(유저네임)
    private String guestId = null;    // 게스트 아이디
    private String myId = null;       // 내 아이디

    // 기본 생성자 → 로비용
    public PlayerPanel(GameFrame gameFrame) {
        this(gameFrame, false);
    }

    // 모드 선택 생성자
    public PlayerPanel(GameFrame gameFrame, boolean gameMode) {
        super("playerBack.png");
        this.gameFrame = gameFrame;
        this.gameMode = gameMode;
        initComponents();
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    private void initComponents() {
        setLayout(null);

        // =========================
        // 로비 UI
        // =========================
        if (!gameMode) {
            roomName = new JLabel("방 이름");
            roomName.setBounds(20, 20, 240, 20);
            roomName.setHorizontalAlignment(JLabel.CENTER);
            roomName.setForeground(Color.WHITE);
            roomName.setFont(new Font("굴림", Font.BOLD, 16));
            add(roomName);

            player1 = new JLabel("");
            player1.setBounds(61, 80, 200, 20);
            player1.setForeground(Color.WHITE);
            player1.setFont(new Font("굴림", Font.BOLD, 16));
            add(player1);

            player2 = new JLabel("");
            player2.setBounds(61, 120, 200, 20);
            player2.setForeground(Color.WHITE);
            player2.setFont(new Font("굴림", Font.BOLD, 16));
            add(player2);

            startBtn.setBounds(35, 440, 100, 20);
            add(startBtn);
            startBtn.setVisible(false);
            startBtn.addActionListener(e -> {
                if (client != null) client.sendStartGame();
            });

            readyBtn.setBounds(35, 440, 100, 20);
            add(readyBtn);
            readyBtn.setVisible(false);
            readyBtn.addActionListener(e -> {
                if (client != null) client.sendReady();
            });

            outRoomBtn.setBounds(145, 440, 100, 20);
            add(outRoomBtn);
            outRoomBtn.setVisible(false);
            outRoomBtn.addActionListener(e -> {
                if (client != null) client.outRoom();
            });

        } else {
            // =========================
            // 게임 HUD (2명 표시)
            // =========================

            // ---- 방장(위) ----
            hostNameLabel = new JLabel("👑 닉네임 : ");
            hostNameLabel.setBounds(20, 20, 250, 25);
            hostNameLabel.setForeground(Color.WHITE);
            hostNameLabel.setFont(new Font("굴림", Font.BOLD, 15));
            add(hostNameLabel);

            hostHpLabel = new JLabel("체력 : 0 / 0");
            hostHpLabel.setBounds(20, 45, 200, 20);
            hostHpLabel.setForeground(Color.WHITE);
            hostHpLabel.setFont(new Font("굴림", Font.PLAIN, 13));
            add(hostHpLabel);

            hostHpBar = new JProgressBar();
            hostHpBar.setBounds(20, 65, 200, 18);
            hostHpBar.setMinimum(0);
            hostHpBar.setMaximum(1);
            hostHpBar.setValue(0);
            hostHpBar.setStringPainted(true);
            hostHpBar.setForeground(Color.RED);
            add(hostHpBar);

            hostScoreLabel = new JLabel("점수 : 0");
            hostScoreLabel.setBounds(20, 85, 200, 18);
            hostScoreLabel.setForeground(Color.YELLOW);
            hostScoreLabel.setFont(new Font("굴림", Font.BOLD, 13));
            add(hostScoreLabel);

            // ---- 게스트(아래) ----
            guestNameLabel = new JLabel("닉네임 : ");
            guestNameLabel.setBounds(20, 115, 250, 25);
            guestNameLabel.setForeground(Color.WHITE);
            guestNameLabel.setFont(new Font("굴림", Font.BOLD, 15));
            add(guestNameLabel);

            guestHpLabel = new JLabel("체력 : 0 / 0");
            guestHpLabel.setBounds(20, 140, 200, 20);
            guestHpLabel.setForeground(Color.WHITE);
            guestHpLabel.setFont(new Font("굴림", Font.PLAIN, 13));
            add(guestHpLabel);

            guestHpBar = new JProgressBar();
            guestHpBar.setBounds(20, 160, 200, 18);
            guestHpBar.setMinimum(0);
            guestHpBar.setMaximum(1);
            guestHpBar.setValue(0);
            guestHpBar.setStringPainted(true);
            guestHpBar.setForeground(Color.RED);
            add(guestHpBar);

            guestScoreLabel = new JLabel("점수 : 0");
            guestScoreLabel.setBounds(20, 180, 200, 18);
            guestScoreLabel.setForeground(Color.YELLOW);
            guestScoreLabel.setFont(new Font("굴림", Font.BOLD, 13));
            add(guestScoreLabel);

            // ---- 업그레이드 아이콘(내 것만) ----
            upgradeIconPanel = new JPanel();
            upgradeIconPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
            upgradeIconPanel.setOpaque(false);
            upgradeIconPanel.setBounds(20, 210, 220, 60);
            add(upgradeIconPanel);
        }
    }

    // =========================
    // 로비 API
    // =========================
    public void addPlayer(String userName) {
        if (gameMode) return;

        if (playerOneName == null) {
            playerOneName = userName;
            player1.setText(userName);
        } else if (playerTwoName == null) {
            playerTwoName = userName;
            player2.setText(userName);
        }
    }

    public void clearPlayers() {
        if (!gameMode) {
            if (player1 != null) player1.setText("");
            if (player2 != null) player2.setText("");
            playerOneName = null;
            playerTwoName = null;
        } else {
            resetAll();
        }
    }

    public void setRoomName(String rName) {
        if (!gameMode && roomName != null) {
            roomName.setText("방 이름 : " + rName);
        }
    }

    public void showStartBtn() {
        if (gameMode) return;
        startBtn.setVisible(true);
        readyBtn.setVisible(false);
        outRoomBtn.setVisible(true);
    }

    public void showReadyBtn() {
        if (gameMode) return;
        readyBtn.setVisible(true);
        startBtn.setVisible(false);
        outRoomBtn.setVisible(true);
    }

    public void setReadyStatus(String name, boolean ready) {
        if (name == null || gameMode) return;

        if (name.equals(playerOneName)) {
            player1.setText(ready ? playerOneName + " (준비)" : playerOneName);
        } else if (name.equals(playerTwoName)) {
            player2.setText(ready ? playerTwoName + " (준비)" : playerTwoName);
        }
    }

    public void removePlayer(String name) {
        if (name == null || gameMode) return;

        if (name.equals(playerOneName)) {
            playerOneName = null;
            player1.setText("");
        } else if (name.equals(playerTwoName)) {
            playerTwoName = null;
            player2.setText("");
        }
    }

    // =========================
    // 게임 HUD API
    // =========================
    public void setMyId(String myId) {
        if (!gameMode) return;
        this.myId = myId;
    }

    /** users[0]이 방장이라는 규칙이면 그대로 넣으면 됨 */
    public void setPlayersWithHost(String hostId, String hostNick, String otherId, String otherNick) {
        if (!gameMode) return;
        this.hostId = hostId;
        this.guestId = otherId;

        setHostInfo(hostId, hostNick);
        setGuestInfo(otherId, otherNick);

        // 초기 점수 0으로 표시
        setHostScore(0);
        setGuestScore(0);
    }

    public void updateHpById(String id, int hp, int maxHp) {
        if (!gameMode || id == null) return;
        if (id.equals(hostId)) setHostHp(hp, maxHp);
        else if (id.equals(guestId)) setGuestHp(hp, maxHp);
    }

    public void updateScoreById(String id, int score) {
        if (!gameMode || id == null) return;
        if (id.equals(hostId)) setHostScore(score);
        else if (id.equals(guestId)) setGuestScore(score);
    }

    // ---- 호환 API: 기존 코드가 setHp/setScore를 호출하던 경우 ----
    public void setHp(int hp, int maxHp) {
        if (!gameMode) return;
        if (myId != null && myId.equals(hostId)) setHostHp(hp, maxHp);
        else if (myId != null && myId.equals(guestId)) setGuestHp(hp, maxHp);
        else setHostHp(hp, maxHp);
    }

    public void setScore(int score) {
        if (!gameMode) return;
        // 내 점수만 갱신(내가 host/guest 중 어디인지에 따라)
        if (myId != null && myId.equals(hostId)) setHostScore(score);
        else if (myId != null && myId.equals(guestId)) setGuestScore(score);
        else setHostScore(score);
    }

    public void addUpgradeIcon(ImageIcon icon) {
        if (!gameMode) return;
        if (icon == null || upgradeIconPanel == null) return;

        Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaled));
        iconLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        upgradeIconPanel.add(iconLabel);
        upgradeIconPanel.revalidate();
        upgradeIconPanel.repaint();
    }

    public void resetAll() {
        if (!gameMode) {
            if (roomName != null) roomName.setText("방 이름");
            if (player1 != null) player1.setText("");
            if (player2 != null) player2.setText("");

            playerOneName = null;
            playerTwoName = null;

            startBtn.setVisible(false);
            readyBtn.setVisible(false);
            outRoomBtn.setVisible(false);
        } else {
            hostId = null;
            guestId = null;
            myId = null;

            setHostInfo(null, null);
            setGuestInfo(null, null);
            setHostHp(0, 0);
            setGuestHp(0, 0);
            setHostScore(0);
            setGuestScore(0);

            if (upgradeIconPanel != null) {
                upgradeIconPanel.removeAll();
                upgradeIconPanel.revalidate();
                upgradeIconPanel.repaint();
            }
        }
    }

    // -------------------------
    // 내부 렌더링 도우미
    // -------------------------
    private void setHostInfo(String id, String nick) {
        if (hostNameLabel == null) return;
        String displayNick = (nick == null || nick.isEmpty()) ? (id == null ? "" : id) : nick;
        String displayId = (id == null ? "" : id);
        hostNameLabel.setText("👑 닉네임 : " + displayNick + " (" + displayId + ")");
    }

    private void setGuestInfo(String id, String nick) {
        if (guestNameLabel == null) return;
        String displayNick = (nick == null || nick.isEmpty()) ? (id == null ? "" : id) : nick;
        String displayId = (id == null ? "" : id);
        guestNameLabel.setText("닉네임 : " + displayNick + " (" + displayId + ")");
    }

    private void setHostHp(int hp, int maxHp) {
        if (hostHpBar == null || hostHpLabel == null) return;

        if (maxHp <= 0) {
            hostHpBar.setMaximum(1);
            hostHpBar.setValue(0);
            hostHpLabel.setText("체력 : 0 / 0");
            return;
        }
        hp = Math.max(0, Math.min(hp, maxHp));
        hostHpBar.setMaximum(maxHp);
        hostHpBar.setValue(hp);
        hostHpLabel.setText("체력 : " + hp + " / " + maxHp);
    }

    private void setGuestHp(int hp, int maxHp) {
        if (guestHpBar == null || guestHpLabel == null) return;

        if (maxHp <= 0) {
            guestHpBar.setMaximum(1);
            guestHpBar.setValue(0);
            guestHpLabel.setText("체력 : 0 / 0");
            return;
        }
        hp = Math.max(0, Math.min(hp, maxHp));
        guestHpBar.setMaximum(maxHp);
        guestHpBar.setValue(hp);
        guestHpLabel.setText("체력 : " + hp + " / " + maxHp);
    }

    private void setHostScore(int score) {
        if (hostScoreLabel != null) hostScoreLabel.setText("점수 : " + score);
    }

    private void setGuestScore(int score) {
        if (guestScoreLabel != null) guestScoreLabel.setText("점수 : " + score);
    }
}
