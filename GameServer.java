import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class GameServer extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;

    private ServerSocket socket;            // 서버소켓
    private Socket client_socket;           // accept()에서 생성된 client 소켓
    private Vector<UserService> UserVec = new Vector<>(); // 연결된 사용자를 저장할 벡터

    // 생성된 방 목록
    private ArrayList<RoomInfo> roomList = new ArrayList<>();

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                GameServer frame = new GameServer();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public GameServer() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 338, 386);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 300, 244);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblNewLabel = new JLabel("Port Number");
        lblNewLabel.setBounds(12, 264, 87, 26);
        contentPane.add(lblNewLabel);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");
        txtPortNumber.setBounds(111, 264, 199, 26);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnServerStart = new JButton("Server Start");
        btnServerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                } catch (NumberFormatException | IOException e1) {
                    e1.printStackTrace();
                    AppendText("서버 시작 실패: " + e1.getMessage());
                    return;
                }

                AppendText("Game Server Running..");
                btnServerStart.setText("Game Server Running..");
                btnServerStart.setEnabled(false);
                txtPortNumber.setEnabled(false);

                AcceptServer accept_server = new AcceptServer();
                accept_server.start();
            }
        });
        btnServerStart.setBounds(12, 300, 300, 35);
        contentPane.add(btnServerStart);
    }

    // 새로운 참가자 accept() 하고 user thread를 새로 생성
    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting clients ...");
                    client_socket = socket.accept();
                    AppendText("새로운 참가자 from " + client_socket);

                    UserService new_user = new UserService(client_socket);
                    UserVec.add(new_user);
                    AppendText("사용자 입장. 현재 참가자 수 " + UserVec.size());
                    new_user.start();
                } catch (IOException e) {
                    AppendText("!!!! accept 에러 발생... !!!! " + e.getMessage());
                }
            }
        }
    }

    // JTextArea에 문자열 출력
    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    class UserService extends Thread {
        private InputStream is;
        private OutputStream os;
        private DataInputStream dis;
        private DataOutputStream dos;

        private Socket client_socket;
        private Vector<UserService> user_vc;
        private String UserName = "";

        // 유저가 어떤 방에 있는지 저장
        private String currentRoom = null;

        public UserService(Socket client_socket) {
            this.client_socket = client_socket;
            this.user_vc = UserVec;

            try {
                is = client_socket.getInputStream();
                dis = new DataInputStream(is);
                os = client_socket.getOutputStream();
                dos = new DataOutputStream(os);

                // 최초 입장 메시지 예: "/login 아이디"
                String line1 = dis.readUTF();
                String[] msg = line1.split(" ");

                if (msg.length >= 2) {
                    UserName = msg[1].trim();
                } else {
                    UserName = "UNKNOWN";
                }

                AppendText("새로운 참가자 " + UserName + " 입장.");
                sendGlobalChat(UserName + "님이 입장했습니다.");
            } catch (Exception e) {
                AppendText("userService error: " + e.getMessage());
            }
        }

        // 방 입장 전 전체 채팅
        public void sendGlobalChat(String msg) {
            for (int i = 0; i < user_vc.size(); i++) {
                UserService user = user_vc.get(i);
                if (user.currentRoom == null) {
                    try {
                        user.dos.writeUTF("/chat|SERVER|" + msg);
                    } catch (IOException e) { }
                }
            }
        }

        // 방 입장 후 방 인원에게만 채팅
        public void sendRoomChat(String room, String msg) {
            for (int i = 0; i < user_vc.size(); i++) {
                UserService user = user_vc.get(i);
                if (room.equals(user.currentRoom)) {
                    try {
                        user.dos.writeUTF("/chat|" + UserName + "|" + msg);
                    } catch (IOException e) { }
                }
            }
        }

        // 방 전체에 최신 멤버 리스트 보내는 함수
        public void sendJoinedRoomList(RoomInfo room) {
            StringBuilder sb = new StringBuilder();
            sb.append("/joinedRoomList|").append(room.roomName).append("|");

            for (int i = 0; i < room.users.size(); i++) {
                sb.append(room.users.get(i));
                if (i < room.users.size() - 1) sb.append(",");
            }

            String msg = sb.toString();

            // 해당 방 유저들에게만 전송
            for (int i = 0; i < user_vc.size(); i++) {
                UserService user = user_vc.get(i);
                if (room.users.contains(user.UserName)) {
                    try {
                        user.dos.writeUTF(msg);
                    } catch (IOException e) { }
                }
            }
        }

        public void logout() {
            UserVec.removeElement(this);
            sendGlobalChat("[알림]|" + UserName + "님이 퇴장하였습니다.");
            AppendText("사용자 퇴장. 현재 참가자 수 " + UserVec.size());
        }

        private RoomInfo findRoomByName(String roomName) {
            for (int i = 0; i < roomList.size(); i++) {
                RoomInfo r = roomList.get(i);
                if (r.roomName.equals(roomName)) return r;
            }
            return null;
        }

        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF().trim();
                    String[] args = msg.split("\\|");

                    if (args.length == 0) continue;

                    ALL:
                    switch (args[0]) {

                        case "/xy": { // /xy|방|유저|x|y  또는 /xy|x|y(구버전)
                            String room = null;
                            String name = this.UserName;
                            int x, y;

                            if (args.length >= 5) {
                                room = args[1];
                                name = args[2];
                                x = Integer.parseInt(args[3]);
                                y = Integer.parseInt(args[4]);
                                this.currentRoom = room; // 혹시 누락 대비
                            } else if (args.length >= 3) {
                                x = Integer.parseInt(args[1]);
                                y = Integer.parseInt(args[2]);
                                room = this.currentRoom;
                            } else {
                                break;
                            }

                            if (room == null) break;

                            for (UserService user : user_vc) {
                                if (room.equals(user.currentRoom) && user != this) {
                                    try {
                                        user.dos.writeUTF("/xy|" + name + "|" + x + "|" + y);
                                    } catch (IOException e) { }
                                }
                            }
                            break;
                        }

                        case "/hp": { // /hp|방|아이디|hp|maxHp
                            if (args.length >= 5) {
                                String room = args[1];
                                String id = args[2];
                                String hp = args[3];
                                String maxHp = args[4];

                                for (UserService user : user_vc) {
                                    if (room.equals(user.currentRoom)) {
                                        try {
                                            user.dos.writeUTF("/hp|" + id + "|" + hp + "|" + maxHp);
                                        } catch (IOException e) { }
                                    }
                                }
                            }
                            break;
                        }

                        case "/score": { // /score|방|아이디|점수
                            if (args.length >= 4) {
                                String room = args[1];
                                String id = args[2];
                                String score = args[3];

                                for (UserService user : user_vc) {
                                    if (room.equals(user.currentRoom)) {
                                        try {
                                            user.dos.writeUTF("/score|" + id + "|" + score);
                                        } catch (IOException e) { }
                                    }
                                }
                            }
                            break;
                        }

                        case "/gameOver": { // /gameOver|방이름|아이디|점수
                            if (args.length >= 4) {
                                String room = args[1];
                                String id = args[2];
                                int score = 0;
                                try { score = Integer.parseInt(args[3]); } catch (Exception ignore) {}

                                RoomInfo target = findRoomByName(room);
                                if (target == null) break;

                                target.gameOverScores.put(id, score);

                                // 2명 모두 gameOver 되면 합산 점수 전송
                                if (target.gameOverScores.size() >= target.MAX_PLAYER) {
                                    ArrayList<String> names = new ArrayList<>(target.gameOverScores.keySet());
                                    String p1 = names.get(0);
                                    String p2 = names.get(1);
                                    int total = target.gameOverScores.get(p1) + target.gameOverScores.get(p2);

                                    for (UserService user : user_vc) {
                                        if (room.equals(user.currentRoom)) {
                                            try {
                                                user.dos.writeUTF("/gameResult|" + p1 + "|" + p2 + "|" + total);
                                            } catch (IOException e) { }
                                        }
                                    }
                                    target.gameOverScores.clear();
                                }
                            }
                            break;
                        }

                        case "/chat": {
                            // 로비(방 입장 전) 전체 채팅만
                            if (args.length >= 2) {
                                if (currentRoom == null) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 1; i < args.length; i++) {
                                        sb.append(args[i]);
                                        if (i < args.length - 1) sb.append(" ");
                                    }
                                    String text = sb.toString();

                                    for (int i = 0; i < user_vc.size(); i++) {
                                        UserService user = user_vc.get(i);
                                        if (user.currentRoom == null) {
                                            user.dos.writeUTF("/chat|" + UserName + "|" + text);
                                        }
                                    }
                                    AppendText("[GlobalChat] " + UserName + " : " + text);
                                }
                            }
                            break;
                        }

                        case "/roomchat": {
                            if (args.length >= 3) {
                                String roomName = args[1];

                                StringBuilder sb = new StringBuilder();
                                for (int i = 2; i < args.length; i++) {
                                    sb.append(args[i]);
                                    if (i < args.length - 1) sb.append(" ");
                                }
                                String text = sb.toString();

                                if (roomName.equals(currentRoom)) {
                                    sendRoomChat(roomName, text);
                                    AppendText("[RoomChat][" + roomName + "] " + UserName + " : " + text);
                                }
                            }
                            break;
                        }

                        case "/room": { // /room|방이름|설명|pw
                            if (args.length >= 4) {
                                String newRoomName = args[1].trim();
                                String explain = args[2].trim();
                                String pw = args[3].trim();

                                boolean exists = false;
                                for (int i = 0; i < roomList.size(); i++) {
                                    RoomInfo room = roomList.get(i);
                                    if (room.roomName.equals(newRoomName)) {
                                        exists = true;
                                        break;
                                    }
                                }

                                if (exists) {
                                    try {
                                        dos.writeUTF("/alert|이미 동일한 이름의 방이 존재합니다!");
                                    } catch (IOException e) {
                                        AppendText("중복 경고 메시지 전송 오류");
                                    }
                                } else {
                                    RoomInfo newRoom = new RoomInfo(newRoomName, explain, pw);

                                    // 방 만든 사람 자동 참가
                                    newRoom.users.add(UserName);

                                    // 방장 준비 완료
                                    newRoom.ready.put(UserName, true);

                                    roomList.add(newRoom);
                                    AppendText("새 방 생성됨! : " + newRoomName);

                                    currentRoom = newRoomName;

                                    dos.writeUTF("/alert|방이 성공적으로 생성되었습니다.");
                                    dos.writeUTF("/joinedRoom|" + newRoomName + "|" + UserName);
                                    sendJoinedRoomList(newRoom);
                                }
                            }
                            break;
                        }

                        case "/refreshRoomList": {
                            if (roomList.size() == 0) {
                                dos.writeUTF("방이 없습니다.");
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append("/roomList|");

                                for (int i = 0; i < roomList.size(); i++) {
                                    RoomInfo room = roomList.get(i);

                                    sb.append(room.roomName).append(",")
                                      .append(room.explain).append(",")
                                      .append(room.users.size())
                                      .append("/")
                                      .append(room.MAX_PLAYER);

                                    if (i < roomList.size() - 1) sb.append(";");
                                }
                                dos.writeUTF(sb.toString());
                            }
                            break;
                        }

                        case "/joinRoom": { // /joinRoom|방이름
                            if (args.length >= 2) {
                                String roomName = args[1];
                                
                                // ===== 2. 이미 다른 방에 들어가 있는 경우 차단 =====
                                if(currentRoom != null) {
                                	try {
                                		dos.writeUTF("/alert|이미 다른 방에 참가 중입니다!");
                                	}
                                	catch(IOException e) { }
                                	break;
                                }
                                
                                RoomInfo targetRoom = findRoomByName(roomName);
                                
                                // 방 존재 여부 확인
                                if (targetRoom == null) {
                                    try {
                                        dos.writeUTF("/alert|존재하지 않는 방입니다.");
                                    } catch (IOException e) { }
                                    break;
                                }

                                if (targetRoom != null) {
                                    if (targetRoom.users.size() >= targetRoom.MAX_PLAYER) {
                                        dos.writeUTF("/alert|이미 인원이 가득 찬 방입니다!");
                                        break;
                                    }

                                    targetRoom.users.add(UserName);
                                    targetRoom.ready.put(UserName, false);
                                    currentRoom = roomName;

                                    dos.writeUTF("/joinedRoom|" + currentRoom + "|" + UserName);
                                    sendJoinedRoomList(targetRoom);
                                }
                            }
                            break;
                        }

                        case "/ready": { // /ready|방|유저|true/false
                            if (args.length == 4) {
                                String userRoom = args[1];
                                String userName = args[2];
                                boolean ready = Boolean.parseBoolean(args[3]);

                                RoomInfo check = findRoomByName(userRoom);
                                if (check == null) break;

                                check.ready.put(userName, ready);

                                StringBuilder sb = new StringBuilder();
                                sb.append("/readyList|").append(check.roomName).append("|");

                                Object[] keys = check.ready.keySet().toArray();
                                for (int i = 0; i < keys.length; i++) {
                                    String name = (String) keys[i];
                                    boolean isReady = check.ready.get(name);

                                    sb.append(name).append(",").append(isReady);
                                    if (i < keys.length - 1) sb.append(";");
                                }

                                String payload = sb.toString();

                                for (int i = 0; i < user_vc.size(); i++) {
                                    UserService user = user_vc.get(i);
                                    if (Objects.equals(user.currentRoom, userRoom)) {
                                        try {
                                            user.dos.writeUTF(payload);
                                        } catch (IOException e) { }
                                    }
                                }
                            }
                            break;
                        }

                        case "/startGame": { // /startGame|방이름
                            if (args.length >= 2) {
                                String startRoomName = args[1];
                                RoomInfo room = findRoomByName(startRoomName);
                                if (room == null) break;

                                boolean allReady = true;
                                Object[] names = room.ready.keySet().toArray();
                                for (Object o : names) {
                                    String name = (String) o;
                                    if (!Boolean.TRUE.equals(room.ready.get(name))) {
                                        allReady = false;
                                        break;
                                    }
                                }

                                if (allReady) {
                                    String gameStartMsg = "/gameStart|" + startRoomName;
                                    for (int j = 0; j < user_vc.size(); j++) {
                                        UserService user = user_vc.get(j);
                                        if (startRoomName.equals(user.currentRoom)) {
                                            try {
                                                user.dos.writeUTF(gameStartMsg);
                                            } catch (IOException e) { }
                                        }
                                    }
                                } else {
                                    try {
                                        dos.writeUTF("/alert|아직 준비되지 않은 플레이어가 있습니다!");
                                    } catch (IOException e) { }
                                }
                            }
                            break;
                        }

                        case "/outRoom": { // /outRoom|방|유저
                            if (args.length >= 3) {
                                String cRoom = args[1];
                                String uName = args[2];

                                for (int i = 0; i < roomList.size(); i++) {
                                    RoomInfo room = roomList.get(i);

                                    if (room.roomName.equals(cRoom)) {

                                        for (int j = room.users.size() - 1; j >= 0; j--) {
                                            if (room.users.get(j).equals(uName)) {

                                                // 방장인데 인원이 2명 이상이면 못나감
                                                if (uName.equals(room.users.get(0)) && room.users.size() > 1) {
                                                    for (int k = 0; k < user_vc.size(); k++) {
                                                        UserService user = user_vc.get(k);
                                                        if (user.UserName.equals(uName)) {
                                                            try {
                                                                user.dos.writeUTF("/error|다른 사람이 있기 때문에 나갈 수 없습니다!");
                                                            } catch (IOException e) { }
                                                        }
                                                    }
                                                    break ALL;
                                                }

                                                room.users.remove(j);
                                                room.ready.remove(uName);

                                                // 남은 사람이 없으면 방 삭제
                                                if (room.users.size() == 0) {
                                                    for (int k = 0; k < user_vc.size(); k++) {
                                                        UserService user = user_vc.get(k);
                                                        if (user.UserName.equals(uName)) {
                                                            try {
                                                                user.dos.writeUTF("/outRoomMe|" + cRoom + "|" + uName);
                                                            } catch (IOException e) { }
                                                        }
                                                    }
                                                    roomList.remove(room);
                                                    break ALL;
                                                }
                                                break;
                                            }
                                        }

                                        // 본인에게 보내기
                                        for (int j = 0; j < user_vc.size(); j++) {
                                            UserService user = user_vc.get(j);
                                            if (user.UserName.equals(uName)) {
                                                try {
                                                    user.currentRoom = null;
                                                    user.dos.writeUTF("/outRoomMe|" + cRoom + "|" + uName);
                                                } catch (IOException e) { }
                                            }
                                        }

                                        // 방에 남은 사람들에게 알려줌
                                        for (int j = 0; j < user_vc.size(); j++) {
                                            UserService user = user_vc.get(j);
                                            if (Objects.equals(user.currentRoom, cRoom) && !user.UserName.equals(uName)) {
                                                try {
                                                    user.dos.writeUTF("/outRoomOther|" + cRoom + "|" + uName);
                                                } catch (IOException e) { }
                                            }
                                        }

                                        sendJoinedRoomList(room);
                                        break;
                                    }
                                }
                            }
                            break;
                        }

                        case "/deleteRoom": { // /deleteRoom|방|pw
                            if (args.length >= 3) {
                                String delRoomName = args[1];
                                String pw = args[2];

                                RoomInfo targetDelRoom = findRoomByName(delRoomName);

                                if (targetDelRoom == null) {
                                    dos.writeUTF("/deleteRoomFail|존재하지 않는 방입니다.");
                                    break;
                                }

                                if (!targetDelRoom.users.get(0).equals(UserName)) {
                                    dos.writeUTF("/deleteRoomFail|방장만 방을 삭제할 수 있습니다.");
                                    break;
                                }

                                if (!targetDelRoom.pw.equals(pw)) {
                                    dos.writeUTF("/deleteRoomFail|비밀번호가 틀렸습니다.");
                                    break;
                                }

                                // 방 인원 전부 로비로 내보내기
                                for (int i = 0; i < user_vc.size(); i++) {
                                    UserService user = user_vc.get(i);
                                    if (delRoomName.equals(user.currentRoom)) {
                                        user.currentRoom = null;
                                        user.dos.writeUTF("/outRoomMe|" + delRoomName + "|" + user.UserName);
                                    }
                                }

                                roomList.remove(targetDelRoom);
                                dos.writeUTF("/deleteRoomSuccess");
                            }
                            break;
                        }

                        case "/emote": { // /emote|...|sender|emoteName
                            if (args.length >= 4) {
                                String sender = args[2];
                                String emoteName = args[3];

                                String senderRoom = this.currentRoom;

                                for (int i = 0; i < UserVec.size(); i++) {
                                    UserService user = UserVec.get(i);

                                    try {
                                        if (senderRoom != null) {
                                            if (senderRoom.equals(user.currentRoom)) {
                                                user.dos.writeUTF("/emote|" + sender + "|" + emoteName);
                                            }
                                        } else {
                                            user.dos.writeUTF("/emote|" + sender + "|" + emoteName);
                                        }
                                    } catch (IOException e) { }
                                }
                            }
                            break;
                        }

                        default:
                            // 모르는 프로토콜은 무시(원하면 로그 찍어도 됨)
                            break;
                    }

                } catch (IOException e) {
                    AppendText("dis.readUTF() error: " + e.getMessage());
                    try {
                        dos.close();
                        dis.close();
                        client_socket.close();
                        logout();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }

    class RoomInfo {
        String roomName;
        String explain;
        String pw;
        final int MAX_PLAYER = 2;

        // 방 참여자 목록
        List<String> users = new ArrayList<>();

        // 준비 여부
        HashMap<String, Boolean> ready = new HashMap<>();

        // 게임오버 점수 임시 저장
        HashMap<String, Integer> gameOverScores = new HashMap<>();

        public RoomInfo(String roomName, String explain, String pw) {
            this.roomName = roomName;
            this.explain = explain;
            this.pw = pw;
        }
    }
}
