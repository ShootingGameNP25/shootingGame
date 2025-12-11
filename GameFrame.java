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
	private CardLayout card = new CardLayout();
	private JPanel start = new JPanel(card);
	Background background = new Background("shootingBack.png");
	
	private String current;
	private Deque<String> historyStack = new ArrayDeque<>();
	
	public static String MAIN = "MAIN";
	public static String LOGIN = "LOGIN";
	public static String GAME = "GAME";
	public static String SCORE = "SCORE";
	public static String READY = "READY";
	
	private JButton startBtn = new JButton("시작");
	private JButton scoreBtn = new JButton("점수 보기");
	private JLabel lMessage;
	
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
		mainCard(); // 메인카드임. 게임 스타트도 여기서 처리
		
		setVisible(true); 
	}
	
	private void mainCard() {
		 background.setLayout(null);
		 
		 lMessage = new JLabel("");
		 lMessage.setBounds(350, 725, 320, 70);
		 lMessage.setOpaque(true);  
		 lMessage.setBackground(new Color(40, 0, 80));
		 lMessage.setForeground(Color.white);
		 lMessage.setHorizontalAlignment(JLabel.CENTER);
		 background.add(lMessage);
		 
		 startBtn.addActionListener(new ActionListener() {
			 @Override
			 public void actionPerformed(ActionEvent e) {
				 boolean found = login.getFound();
				 boolean ok = login.getOk();
				 
				 if(found == true && ok == true) { // 로그인 성공 시
					 initGame();
				 }
				 else { // 로그인 실패 시
					 lMessage.setText("로그인을 먼저 해주세요.");
					 return;
				 }
			 }
		 });
		 
		 scoreBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				show(SCORE);
			}
		 });
		 
		 startBtn.setBounds(300, 800, 100, 40);
		 scoreBtn.setBounds(600, 800, 100, 40);
		 
		 login.setBounds(350, 520, 320, 200);
		 
		 scoreStartPanel.setBounds(350, 50, 320, 467);
		 
		 background.add(startBtn);
		 background.add(scoreBtn);
		 background.add(login);
		 background.add(scoreStartPanel);
		 
		 ready = new ReadyPanel(this, chatPanel, playerPanel);
		 
		 start.add(background, MAIN);
		 start.add(score, SCORE);
		 start.add(ready, READY);
		 
		 gamePanel = new GamePanel(this);
		 start.add(gamePanel, GAME);
		 
	}
	
	public JLabel getLMessage() { // GameFrame의 lMessage 공유를 위해
		return lMessage;
	}
	
	public void show(String id) {
		if(Objects.equals(current, id)) return;
		if(current != null) historyStack.push(current);
		card.show(start, id);
		current = id;
		
		start.revalidate();
		start.repaint();
	}
	
	public void back() {
		if(historyStack.isEmpty()) return; // 과거의 기록이 없다면 그냥 리턴
		String past = historyStack.pop();
		card.show(start, past);
		current = past;
		
		start.revalidate();
		start.repaint();
	}
	
	private void initGame() {
		// 게임 시작시 ReadyPanel 표시
		show(READY);
		
		// 로그인 정보 가져오기
		String username = login.getLoggedInUsername();
		
		// IP와 Port 고정
		String ip = "127.0.0.1";
		String port = "30000";
		
		client = new GameClient(username, ip, port, lMessage);
		
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
	
	public static void main(String[] args) {
		new GameFrame();
	}
}
