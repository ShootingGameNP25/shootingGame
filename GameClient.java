import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * GameClient (클라이언트)
 *  - 서버로: 채팅/방/ready/start/좌표(xy)/HP/점수/게임오버 전송
 *  - 서버에서: 좌표/HP/점수/게임결과/채팅/방목록 수신 후 UI 반영
 */
public class GameClient extends Thread {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String UserName;

    // 현재 본인이 속한 방. 없으면 null
    private String currentRoom = null;

    // 방 생성 dialog 정보를 얻기 위한 변수
    private JDialog dialog;

    // 방 삭제 dialog 정보를 얻기 위한 변수
    private JDialog delLog;

    // 준비 완료 토글
    private boolean isReady = false;

    // IP와 Port는 GameFrame에서 고정된 값을 받아옴
    private String IPAddress;
    private String PortNumber;

    private JLabel lMessage; // GameFrame의 메시지 라벨을 업데이트
    private GamePanel gamePanel;
    private ChatPanel chatPanel;
    private ReadyPanel readyPanel;
    private PlayerPanel playerPanel;
    private GameFrame gameFrame;

    public GameClient(String userName, String ip, String port, JLabel lMessage) {
        this.UserName = userName;
        this.IPAddress = ip;
        this.PortNumber = port;
        this.lMessage = lMessage;
    }

    public String getUserName() { return UserName; }
    public String getCurrentRoom() { return currentRoom; }

    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
        if (this.chatPanel != null) this.chatPanel.setGameClient(this);
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        if (this.gamePanel != null) {
            this.gamePanel.setClient(this);
        }
    }

    public void setReadyPanel(ReadyPanel readyPanel) {
        this.readyPanel = readyPanel;
    }

    // PlayerPanel과 연결
    public void setPlayerPanel(PlayerPanel playerPanel) {
        this.playerPanel = playerPanel;
        if (this.playerPanel != null) this.playerPanel.setClient(this);
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    public void connect() {
        try {
            socket = new Socket(IPAddress, Integer.parseInt(PortNumber));

            if (lMessage != null) lMessage.setText("서버 연결 성공");

            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            // 최초 로그인 (서버가 "/login 아이디" 형태로 받는 구조 유지)
            dos.writeUTF("/login " + UserName);

            this.start(); // 수신 스레드 시작
        } catch (Exception e) {
            e.printStackTrace();
            if (lMessage != null) lMessage.setText("연결 실패");
        }
    }

    // =========================
    // 송신
    // =========================
    public void sendGlobalChat(String msg) {
        try { dos.writeUTF("/chat|" + msg); } catch(IOException e) { }
    }

    public void sendRoomChat(String msg) {
        if (currentRoom == null) { sendGlobalChat(msg); return; }
        try { dos.writeUTF("/roomchat|" + currentRoom + "|" + msg); } catch(IOException e) { }
    }

    public void sendChat(String msg) {
        try {
            if (currentRoom == null) dos.writeUTF("/chat|" + msg);
            else dos.writeUTF("/roomchat|" + currentRoom + "|" + msg);
        } catch (IOException e) { }
    }

    public void sendMove(int x, int y) {
        try {
            // 방 기준으로 보내야 서버가 룸 브로드캐스트 하기 편함
            if (currentRoom != null) dos.writeUTF("/xy|" + currentRoom + "|" + UserName + "|" + x + "|" + y);
        } catch (IOException e) { }
    }

    public void sendHp(int hp, int maxHp) {
        if (currentRoom == null) return;
        try { dos.writeUTF("/hp|" + currentRoom + "|" + UserName + "|" + hp + "|" + maxHp); }
        catch (IOException e) { }
    }

    public void sendScore(int score) {
        if (currentRoom == null) return;
        try { dos.writeUTF("/score|" + currentRoom + "|" + UserName + "|" + score); }
        catch (IOException e) { }
    }

    public void sendGameOver(int score) {
        if (currentRoom == null) return;
        try { dos.writeUTF("/gameOver|" + currentRoom + "|" + UserName + "|" + score); }
        catch (IOException e) { }
    }

    public void deliverInfo(String roomName, String explain, String pw) {
        String msg = "/room|" + roomName + "|" + explain + "|" + pw;
        try { dos.writeUTF(msg); } catch(IOException e) { }
    }

    public void giveMeRoomList() {
        try { dos.writeUTF("/refreshRoomList"); } catch(IOException e) { }
    }

    public void joinRoom(String roomName) {
        try { dos.writeUTF("/joinRoom|" + roomName); } catch(IOException e) { }
    }

    // 방 생성할 때 나오는 창에 대한 정보를 저장
    public void setCreateRoomDialog(JDialog dialog) {
        this.dialog = dialog;
    }

    public void setDelRoomDialog(JDialog delLog) {
        this.delLog = delLog;
    }

    // 서버에 해당 유저가 ready 되었다고 신호를 보내기
    public void sendReady() {
        if (currentRoom == null) {
            JOptionPane.showMessageDialog(null, "방에 먼저 참가하세요!");
            return;
        }
        isReady = !isReady;
        try { dos.writeUTF("/ready|" + currentRoom + "|" + UserName + "|" + isReady); }
        catch(IOException e){ }
    }

    public void sendStartGame() {
        if (currentRoom == null) return;
        try { dos.writeUTF("/startGame|" + currentRoom); } catch(IOException e) { }
    }

    public void outRoom() {
        if (currentRoom == null) return;
        try { dos.writeUTF("/outRoom|" + currentRoom + "|" + UserName); }
        catch(IOException e) { }
    }

    public void deleteRoom(String roomName, String password) {
        try { dos.writeUTF("/deleteRoom|" + roomName + "|" + password); }
        catch(IOException e) { }
    }

    public void sendEmote(String emoteName) {
        try { 
        	dos.writeUTF("/emote|" + currentRoom + "|" + UserName + "|" + emoteName); 
        }
        catch(IOException e) { }
    }

    // =========================
    // 수신 루프
    // =========================
    @Override
    public void run() {
        while (true) {
            try {
                String msg = dis.readUTF().trim();
                String[] args = msg.split("\\|");
                if (args.length == 0) continue;

                switch (args[0]) {

                    case "/hp": {
                        // /hp|아이디|hp|maxHp
                        if (args.length >= 4 && gamePanel != null) {
                            String id = args[1];
                            int hp = Integer.parseInt(args[2]);
                            int maxHp = Integer.parseInt(args[3]);
                            SwingUtilities.invokeLater(() -> gamePanel.updateHpById(id, hp, maxHp));
                        }
                        break;
                    }

                    case "/score": {
                        // /score|아이디|점수
                        if (args.length >= 3 && gamePanel != null) {
                            String id = args[1];
                            int score = Integer.parseInt(args[2]);
                            SwingUtilities.invokeLater(() -> gamePanel.updateScoreById(id, score));
                        }
                        break;
                    }

                    case "/gameResult": {
                        // /gameResult|p1|p2|total
                        if (args.length >= 4) {
                            String p1 = args[1];
                            String p2 = args[2];
                            int total = 0;
                            try { total = Integer.parseInt(args[3]); } catch (Exception ignore) {}

                            final int fTotal = total;
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    if (gameFrame != null) {
                                        ScorePanel sp = gameFrame.getScorePanel();
                                        if (sp != null) 
                                        sp.setBackTarget(GameFrame.READY); 
                                        sp.setGameResult(p1, p2, fTotal);
                                        gameFrame.showFromGameOver(GameFrame.SCORE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        break;
                    }

                    case "/xy": {
                        // /xy|유저이름|x|y
                        if (args.length >= 4 && gamePanel != null) {
                            String name = args[1];
                            int x = Integer.parseInt(args[2]);
                            int y = Integer.parseInt(args[3]);
                            if (!UserName.equals(name)) {
                                SwingUtilities.invokeLater(() -> gamePanel.updateOtherPlane(name, x, y));
                            }
                        }
                        break;
                    }

                    case "/chat": {
                        // 서버가 로비/방 채팅 모두 "/chat|보낸사람|메시지"로 보냄 (서버 sendRoomChat도 /chat 사용)
                        if (args.length >= 3 && chatPanel != null) {
                            String sender = args[1];
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]);
                                if (i < args.length - 1) sb.append(" ");
                            }
                            String message = sb.toString();

                            SwingUtilities.invokeLater(() -> {
                                if ("SERVER".equals(sender)) chatPanel.appendSystemChat(message);
                                else chatPanel.appendChat(sender, message);
                            });
                        }
                        break;
                    }

                    case "/roomchat": {
                        // (호환용) 혹시 서버가 /roomchat으로 보내는 버전일 경우 대비
                        if (args.length >= 3 && chatPanel != null) {
                            int start = 2;
                            String sender;
                            String message;

                            // /roomchat|방|보낸사람|메시지... 일 수도 있어서 길이 체크
                            if (args.length >= 4) {
                                sender = args[2];
                                start = 3;
                            } else {
                                sender = "UNKNOWN";
                            }

                            StringBuilder sb = new StringBuilder();
                            for (int i = start; i < args.length; i++) {
                                sb.append(args[i]);
                                if (i < args.length - 1) sb.append(" ");
                            }
                            message = sb.toString();

                            final String fSender = sender;
                            final String fMessage = message;
                            SwingUtilities.invokeLater(() -> chatPanel.appendChat(fSender, fMessage));
                        }
                        break;
                    }

                    case "/alert": {
                        if (args.length >= 2) JOptionPane.showMessageDialog(null, args[1]);

                        if (dialog != null) { dialog.dispose(); dialog = null; }
                        if (delLog != null) { delLog.dispose(); delLog = null; }
                        break;
                    }

                    case "/error": {
                        if (args.length >= 2) JOptionPane.showMessageDialog(null, args[1]);
                        break;
                    }

                    case "/roomList": {
                        // /roomList|방1,설명1,1/2;방2,설명2,0/2
                        if (args.length >= 2 && readyPanel != null) {
                            SwingUtilities.invokeLater(() -> {
                                readyPanel.clearRoomList();

                                String payload = args[1];
                                if (payload == null || payload.isEmpty()) return;

                                String[] roomTotal = payload.split(";");
                                for (String one : roomTotal) {
                                    if (one == null || one.isEmpty()) continue;
                                    String[] oneRoom = one.split(",");
                                    if (oneRoom.length < 3) continue;
                                    readyPanel.updateRoomList(oneRoom[0], oneRoom[1], oneRoom[2]);
                                }
                            });
                        }
                        break;
                    }

                    case "/joinedRoom": {
                        // /joinedRoom|방이름|유저이름
                        if (args.length >= 3 && readyPanel != null) {
                            String roomName = args[1];
                            String userName = args[2];
                            currentRoom = roomName;

                            SwingUtilities.invokeLater(() -> readyPanel.updatePlayerList(roomName, userName));
                        }
                        break;
                    }

                    case "/joinedRoomList": {
                        // /joinedRoomList|방이름|u1,u2
                        if (args.length >= 3 && readyPanel != null && playerPanel != null) {
                            String roomName = args[1];
                            String[] users = args[2].split(",");
                            
                            // 이미 같은 방이면 "초기화" 하지 않음
                            boolean sameRoom = (currentRoom != null && currentRoom.equals(roomName));

                            currentRoom = roomName;

                            SwingUtilities.invokeLater(() -> {
                                readyPanel.clearPlayers();
                                
                                // 같은 방이 아닐 때만 PlayerPanel 초기화
                                if(!sameRoom) {
                                	playerPanel.clearPlayers();
                                }

                                for (String u : users) readyPanel.addPlayer(u);

                                if (users.length > 0 && users[0].equals(UserName)) playerPanel.showStartBtn();
                                else playerPanel.showReadyBtn();

                                // 게임 패널이 이미 준비되어 있다면(미리 생성) 2인 HUD 정보도 세팅
                                if (gamePanel != null) {
                                    PlayerPanel gp = gamePanel.getRightTopPlayerPanel();
                                    if (gp != null) {
                                        String hostId = users.length > 0 ? users[0] : null;
                                        String guestId = users.length > 1 ? users[1] : null;

                                        gp.setMyId(UserName);

                                        // “아이디를 닉네임 대신” 쓰는 구조라면 id 그대로 넘김
                                        gp.setPlayersWithHost(hostId, hostId, guestId, guestId);
                                    }
                                }
                            });
                        }
                        break;
                    }

                    case "/readyList": {
                        // /readyList|방이름|홍길동,true;김철수,false
                        if (args.length >= 3 && readyPanel != null) {
                            String[] userAbool = args[2].split(";");
                            SwingUtilities.invokeLater(() -> readyPanel.updateReadyStatus(userAbool));
                        }
                        break;
                    }

                    case "/gameStart": {
                        // /gameStart|방이름
                        if (gameFrame != null) {
                            SwingUtilities.invokeLater(() -> {
                                gameFrame.show(GameFrame.GAME);

                                GamePanel gp = gameFrame.getGamePanel();
                                setGamePanel(gp);

                                // 게임용 PlayerPanel 연결(2인 HUD)
                                PlayerPanel gamePlayerPanel = gp.getRightTopPlayerPanel();
                                setPlayerPanel(gamePlayerPanel);
                                if (gamePlayerPanel != null) gamePlayerPanel.setMyId(UserName);

                                // 게임 채팅 연결
                                ChatPanel gameChat = gp.getChatPanel();
                                if (gameChat != null) {
                                    gameChat.setGameClient(this);
                                    setChatPanel(gameChat);
                                }

                                // ✅ 여기만 바꾸면 됨 (초기화 + 시작)
                                gp.resetAndStartGame();
                            });
                        }
                        break;
                    }

                    case "/outRoomMe": {
                        // /outRoomMe|방|유저
                        if (args.length >= 3 && readyPanel != null) {
                        	String userRoomName = args[1];
                        	String userName = args[2];
                        	
                            if(currentRoom != null && currentRoom.equals(userRoomName)) {
                            	currentRoom = null;
                                isReady = false;

                                SwingUtilities.invokeLater(() -> {
                                    readyPanel.clearRoomList();
                                    if (gameFrame != null) readyPanel.resetPlayerPanel();
                                });

                                try { dos.writeUTF("/refreshRoomList"); } catch(IOException e) { }
                            }
                        }
                        break;
                    }

                    case "/outRoomOther": {
                        // /outRoomOther|방|유저
                        if (args.length >= 3 && playerPanel != null) {
                            String oUser = args[2];
                            SwingUtilities.invokeLater(() -> playerPanel.removePlayer(oUser));
                        }
                        break;
                    }

                    case "/deleteRoomSuccess": {
                        currentRoom = null;
                        isReady = false;

                        if (readyPanel != null) {
                            SwingUtilities.invokeLater(() -> {
                                readyPanel.clearPlayers();
                                readyPanel.resetPlayerPanel();
                                readyPanel.clearRoomList();
                            });
                        }
                        giveMeRoomList();
                        JOptionPane.showMessageDialog(null, "방이 삭제되었습니다");
                        break;
                    }

                    case "/deleteRoomFail": {
                        if (args.length >= 2) JOptionPane.showMessageDialog(null, args[1]);
                        break;
                    }

                    case "/emote": {
                        // /emote|sender|emoteName  (서버가 이렇게 보내는 구조)
                        if (args.length >= 3 && chatPanel != null) {
                            String sender = args[1];
                            String emoteName = args[2];
                            SwingUtilities.invokeLater(() -> chatPanel.appendEmote(sender, emoteName));
                        }
                        break;
                    }

                    default:
                        // 알 수 없는 프로토콜은 무시
                        break;
                }

            } catch (IOException e) {
                if (lMessage != null) lMessage.setText("서버 연결 끊김");
                safeClose();
                break;
            } catch (Exception e) {
                // 파싱 에러 등으로 스레드가 죽지 않게
                e.printStackTrace();
            }
        }
    }

    private void safeClose() {
        try { if (dos != null) dos.close(); } catch (Exception ignore) {}
        try { if (dis != null) dis.close(); } catch (Exception ignore) {}
        try { if (socket != null) socket.close(); } catch (Exception ignore) {}
    }
}
