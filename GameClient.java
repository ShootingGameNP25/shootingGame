import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class GameClient extends Thread {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private InputStream is;
    private OutputStream os;
    
    private String UserName;
    
    // 현재 본인이 속한 방. 없으면 null값
    private String currentRoom = null;
    
    // 방 생성 dialog 정보를 얻기 위한 변수
    private JDialog dialog;
    
    // 방 삭제 delLog 정보를 얻기 위한 변수
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
    
    public void setChatPanel(ChatPanel chatPanel) {
    	this.chatPanel = chatPanel;
    }
    
    public void setGamePanel(GamePanel gamePanel) {
    	this.gamePanel = gamePanel;
    }
    
    public void setReadyPanel(ReadyPanel readyPanel) {
    	this.readyPanel = readyPanel;
    }
    
    // PlayerPanel과 연결
    public void setPlayerPanel(PlayerPanel playerPanel) {
    	this.playerPanel = playerPanel;
    	this.playerPanel.setClient(this);
    }

    public void connect() {
        try {
            socket = new Socket(IPAddress, Integer.parseInt(PortNumber));
            lMessage.setText("서버 연결 성공");
            
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            
            // 최초 로그인 (기존 방식 유지)
            dos.writeUTF("/login " + UserName);
            
            this.start(); // 수신 스레드 시작
        } catch (Exception e) {
            e.printStackTrace();
            lMessage.setText("연결 실패");
        }
    }
    
    // 전체 전송
    public void sendGlobalChat(String msg) {
    	try {
    		dos.writeUTF("/chat|" + msg);
    	}
    	catch(IOException e) { }
    }
    
    public void sendRoomChat(String msg) {
    	// 현재 사용자가 방에 안들어갔을 경우
    	if(currentRoom == null) {
    		sendGlobalChat(msg);
    		return;
    	}
    	
    	// 방이 있을 경우
    	try {
    		dos.writeUTF("/roomchat|" + currentRoom + "|" + msg);
    	}
    	catch(IOException e) { }
    }
    
    public void sendChat(String msg) {
        try {
            if(currentRoom == null) {
                // 전체 채팅
                dos.writeUTF("/chat|" + msg);
            }
            else {
                // 방 채팅
                dos.writeUTF("/roomchat|" + currentRoom + "|" + msg);
            }
        } catch (IOException e) { }
    }
    
    public void sendMove(int x, int y) {
        try {
            dos.writeUTF("/xy|" + x + "|" + y);
        } catch (IOException e) { }
    }
    
    public String getUserName() { return UserName; }
    
    public void deliverInfo(String roomName, String explain, String pw) {
    	String msg = "/room|" + roomName + "|" + explain + "|" + pw;
    	
    	try {
    		dos.writeUTF(msg);
    	}
    	catch(IOException e) { }
    }
    
    public void giveMeRoomList() {
    	try{
    		dos.writeUTF("/refreshRoomList");
    	}
    	catch(IOException e) { }
    }
    
    public void joinRoom(String roomName) {
    	try {
    		dos.writeUTF("/joinRoom|" + roomName);
    	}
    	catch(IOException e) { }
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
    	// 준비 완료 버튼을 누를 때마다 true -> false, false -> true로 변함
    	isReady = !isReady;
    	
    	try {
    		dos.writeUTF("/ready|" + currentRoom + "|" + UserName + "|" + isReady);
    	}
    	catch(IOException e){ }
    }
    
    public void sendStartGame() {
    	try {
    		dos.writeUTF("/startGame|" + currentRoom);
    	}
    	catch(IOException e) { }
    }
    
    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }
    
    public void outRoom() {
    	try {
    		dos.writeUTF("/outRoom|" + currentRoom + "|" + UserName);
    	}
    	catch(IOException e) { }
    }
    
    public void deleteRoom(String roomName, String password) {
    	try {
    		dos.writeUTF("/deleteRoom|" + roomName + "|" + password);
    	}
    	catch(IOException e) { }
    }
    
    public void sendEmote(String emoteName) {
    	try {
    		dos.writeUTF("/emote|" + currentRoom + "|" + UserName + "|" + emoteName);
    	}
    	catch(IOException e) { }
    }
    
    @Override
    public void run() {
        while (true) {
            try {
            	String msg = dis.readUTF().trim();
            	String args[] = msg.split("\\|");
                
                switch(args[0]) {
                    case "/xy": // /xy|유저이름|x좌표|y좌표 -> 라고 가정
                    	if(args.length >= 4) {
                    		String name = args[1];
                    		
                    		try {
                    			int x = Integer.parseInt(args[2]);
                    			int y = Integer.parseInt(args[3]);
                    			
                    			if(gamePanel != null) {
                    				// 해당 부분은 임의 수정하셔도 됩니다.
                    				//gamePanel.updateYou(name, x, y);
                    			}
                    		}
                    		catch(NumberFormatException e) {
                    				
                    		}
                    	}
                        break;
                        
                    case "/chat": // GameServer의 sendAll() 메소드에서 넘어온 값이 여기로 옴
                    	// -> /chat 보낸사람 메시지내용 -> 3개
                        if(args.length >= 3) {
                        	String sender = args[1];
                        	
                        	String message = "";
                    		
                    		// 끊어져 있는 보내는 내용을 하나로 합치는 작업
                    		for(int i = 2; i < args.length; i++) {
                    			message += args[i];
                    			
                    			// 마지막 문자열이 아니라면 띄어쓰기 넣어주는 작업
                    			if(i < args.length - 1) message += " ";
                    		}
                    		
                    		if(chatPanel != null) { // 진짜로 출력
                    			if("SERVER".equals(sender)) { // 서버에서 보낸거라면 -> 입장, 퇴장 등
                    				chatPanel.appendSystemChat(message);
                    			}
                    			else {
                    				chatPanel.appendChat(sender, message);
                    			}
                    		}
                        }
                        break;
                        
                    case "/roomchat":
                    	if(args.length >= 4) {
                    		String room = args[1];
                    		String sender = args[2];
                    		StringBuilder sb = new StringBuilder();
                    		
                    		for(int i = 3; i < args.length; i++) {
                    			sb.append(args[i]);
                    			if(i < args.length - 1) sb.append(" ");
                    		}
                    	}
                    	break;
                        
                    case "/alert":
                    	JOptionPane.showMessageDialog(null, args[1]);
                    	
                    	// 방 생성 창이 떠 있으면 닫기
                    	if(dialog != null) {
                    		dialog.dispose();
                    		dialog = null;
                    	}
                    	break;
                    	
                    case "/error":
                    	JOptionPane.showMessageDialog(null, args[1]);
                    	break;
                    
                    case "/roomList": // ReadyPanel의 DefaultTableModel로 넘겨야 됨
                    	// 방1,설명1,123;방2,설명2,456;
                    	if(args.length >= 2) {
                    		readyPanel.clearRoomList();
                    		
                    		String []roomTotal = args[1].split(";"); // 방1,설명1,123,1/2
                        	
                        	for(int i = 0; i < roomTotal.length; i++) {
                        		String []oneRoom = roomTotal[i].split(",");
                        		
                        		String roomName = oneRoom[0];
                        		String explain = oneRoom[1];
                        		String count = oneRoom[2];
                        		readyPanel.updateRoomList(roomName, explain, count);
                        	}
                    	}
                    	break;
                    	
                    case "/joinedRoom": // 방 참가
                    	if(args.length >= 3) {
                    		String roomName = args[1];
                        	String userName = args[2];
                        	
                        	currentRoom = roomName;
                        	
                        	// 플레이어를 해당 방에 추가
                        	readyPanel.updatePlayerList(roomName, userName);
                        
                    	}
                    	break;
                    	
                    case "/joinedRoomList":
                    	String roomName = args[1];
                    	String[] users = args[2].split(",");
                    	
                    	currentRoom = roomName;
                    	
                    	// 기존 플레이어 초기화
                    	readyPanel.clearPlayers();
                    	playerPanel.clearPlayers();
                    	
                    	// 전송된 유저 목록 전체 갱신
                    	for(int i = 0; i < users.length; i++) {
                    		String u = users[i];
                    		readyPanel.addPlayer(u);
                    	}
                    	
                    	// 현재 내 이름의 위치를 찾기 위한 변수                 	
                    	if(users[0].equals(UserName)) 
                    		playerPanel.showStartBtn();
                    	else 
                    		playerPanel.showReadyBtn();
                    	
                    	break;
                    	
                    case "/readyList": // /readyList|방이름|홍길동,true;김철수,false; 이렇게 들어올 예정
                    	if(args.length >= 3) {
                    		String userRoomName = args[1];
                    		String userAbool[] = args[2].split(";"); //userAbool[0] = 홍길동,true userAbool[1] = 김철수,false
                    		readyPanel.updateReadyStatus(userAbool);
                    	}
                    	break;
                    	
                    case "/gameStart":
                        gameFrame.show(GameFrame.GAME);
                        
                        GamePanel gamePanel = gameFrame.getGamePanel();
                        
                        // 게임 시작 시 붙인 ChatPanel과의 연결
                        ChatPanel gameChat = gamePanel.getChatPanel();
                        gameChat.setGameClient(this);
                        this.setChatPanel(gameChat);
                        
                        gamePanel.startGame();
                        break;
                        
                    case "/outRoomMe":
                    	if(args.length >= 3) {
                    		String userRoomName = args[1];
                    		String userName = args[2];
                    		
                    		// 방 초기화
                    		currentRoom = null;
                    		
                    		// 먼저 초기화를 안하면 방이 하나 더 생기는현상 발생
                    		readyPanel.clearRoomList();
                    		
                    		// GameFrame에서 PlayerPanel 재생성
                    		if(gameFrame != null) {
                    			readyPanel.resetPlayerPanel();
                    		}
                    		
                    		try {
                    			dos.writeUTF("/refreshRoomList");
                    		}
                    		catch(IOException e) { }
                    	}
                    	break;
                    	
                    case "/outRoomOther":
                    	if(args.length >= 3) {
                    		String room = args[1];
                    		String oUser = args[2];
                    		
                    		playerPanel.removePlayer(oUser);
                    	}
                    	break;

                    case "/deleteRoomSuccess": // 방 삭제 성공
                    	currentRoom = null;
                    	
                    	// 플레이어 UI 초기화
                    	readyPanel.clearPlayers();
                    	readyPanel.resetPlayerPanel();
                    	
                    	// 방 목록 초기화 후 서버 기준으로 다시 받기
                    	readyPanel.clearRoomList();
                    	giveMeRoomList();
                    	
                    	JOptionPane.showMessageDialog(null, "방이 삭제되었습니다");
                    	break;
                    	
                    case "/deleteRoomFail": // 방 삭제 실패
                    	JOptionPane.showMessageDialog(null, args[1]);
                    	break;
                    	
                    case "/emote":
                    	if(args.length >= 3) {
                    		String sender = args[1];
                    		String emoteName = args[2];
                    		chatPanel.appendEmote(sender, emoteName);
                    	}
                    	break;
                }
            } 
            catch (IOException e) {
                lMessage.setText("서버 연결 끊김");
                break;
            }
        }
    }
}
