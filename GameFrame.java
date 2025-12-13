import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

// 레벨 별로 순위 보기 만들기
public class GameFrame extends JFrame{
	// Card 관리
	private CardLayout card = new CardLayout();
	private JPanel start = new JPanel(card);
	
	// 화면 ID
	public static String MAIN = "MAIN";
	public static String LOGIN = "LOGIN";
	public static String GAME = "GAME";
	public static String SCORE = "SCORE";
	public static String READY = "READY";
	
	// 화면 전환 기록
	private String current;
	private Deque<String> historyStack = new ArrayDeque<>();
	
	// 메인 화면 컴포넌트
	Background background = new Background("shootingBack.png");
	private JButton startBtn = new JButton("시작");
	private JButton scoreBtn = new JButton("점수 보기");
	private JLabel lMessage;
	
	// 패널들
	private ChatPanel chatPanel = new ChatPanel();
	private LoginPanel login = new LoginPanel(this);
	private ScoreStartPanel scoreStartPanel = new ScoreStartPanel(); // 로딩시 보이는 화면에 있는 상위 10명의 점수
	private ReadyPanel ready;
	private GamePanel gamePanel;
	private ScorePanel score = new ScorePanel(this);
	private GameClient client;
	
	// PlayerPanel 재사용
	private PlayerPanel playerPanel = new PlayerPanel(this, false);
	
	public GameFrame() {
		setTitle("게임"); 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
		setSize(1000, 1000);
		this.setResizable(false);
		
		setContentPane(start);
		mainCard(); // 메인카드
		
		setVisible(true); 
	}
	
	private void mainCard() {
		 background.setLayout(null);
		 
		 // 메시지 라벨
		 lMessage = new JLabel("");
		 lMessage.setBounds(350, 725, 320, 70);
		 lMessage.setOpaque(true);  
		 lMessage.setBackground(new Color(40, 0, 80));
		 lMessage.setForeground(Color.white);
		 lMessage.setHorizontalAlignment(JLabel.CENTER);
		 background.add(lMessage);
		 
		 // 시작 버튼
		 startBtn.setBounds(300, 800, 100, 40);
		 startBtn.addActionListener(new ActionListener() {
			 @Override
			 public void actionPerformed(ActionEvent e) {
				 if(login == null) {
					 lMessage.setText("로그인 화면 오류");
					 return;
				 }
				 
				 boolean found = login.getFound();
				 boolean ok = login.getOk();
				 
				 // 로그인 성공 시
				 if(found == true && ok == true) { 
					 initGame();
				 }
				 else { 
					 lMessage.setText("로그인을 먼저 해주세요.");
					 return;
				 }
			 }
		 });
		 
		 // 점수 보기 버튼
		 scoreBtn.setBounds(600, 800, 100, 40);
		 scoreBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				show(SCORE);
			}
		 });
		 
		 // 로그인 버튼
		 login.setBounds(350, 520, 320, 200);
		 
		 scoreStartPanel.setBounds(350, 50, 320, 467);
		 
		 background.add(startBtn);
		 background.add(scoreBtn);
		 background.add(login);
		 background.add(scoreStartPanel);
		  
		 start.add(background, MAIN);
		 start.add(score, SCORE);
		 start.add(ready, READY);
		 
		 gamePanel = new GamePanel(this);
		 ready = new ReadyPanel(this, chatPanel, playerPanel);
		 
		 start.add(gamePanel, GAME);
		 
	}
	
	// GameFrame의 lMessage 공유를 위해
	public JLabel getLMessage() { 
		return lMessage;
	}
	
	public void show(String id) {
		// 같은 화면이면 무시
		if(Objects.equals(current, id)) 
			return;
		
		// 이전 화면 기록
		if(current != null) 
			historyStack.push(current);
		
		card.show(start, id);
		current = id;
		
		start.revalidate();
		start.repaint();
	}
	
	public void back() {
		// 과거의 기록이 없다면 그냥 리턴
		if(historyStack.isEmpty()) 
			return; 
		
		String past = historyStack.pop();
		card.show(start, past);
		current = past;
		
		start.revalidate();
		start.repaint();
	}
	
	private void initGame() {
		// ReadyPanel 표시
		show(READY);
		
		// 로그인 정보 가져오기
		String userName = login.getLoggedInUsername();
		if(userName == null || userName.trim().isEmpty()) {
			lMessage.setText("사용자 정보 오류");
			return;
		}
		
		// IP와 Port 고정
		String ip = "127.0.0.1";
		String port = "30000";
		
		client = new GameClient(userName, ip, port, lMessage);
		
		playerPanel.setClient(client);
		
		// ReadyPanel -> GameClient
		ready.setGameClient(client);
		
		// client -> ReadyPanel
		client.setReadyPanel(ready);
		
		// ChatPanel 연결
		client.setChatPanel(chatPanel);
		
		// GamePanel -> client
		client.setGamePanel(gamePanel);
		
		// client -> GameFrame
		client.setGameFrame(this);
		
		// 서버 접속
		client.connect();
	}
	
	// GameFrame이 PlayerPanel을 재생성하고 ReadyPanel에 전달
	public void resetPlayerPanel() {
	    playerPanel.resetAll();
	}
	
	public GamePanel getGamePanel() {
		return gamePanel;
	}
	
	public ChatPanel getSharedChatPanel() {
		return chatPanel;
	}
	
	public static void main(String[] args) {
		new GameFrame();
	}
}
