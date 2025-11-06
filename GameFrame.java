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

// new Color(40, 0, 80)


// 레벨 별로 순위 보기 만들기
public class GameFrame extends JFrame{
	private CardLayout card = new CardLayout();
	private JPanel start = new JPanel(card);
	Background background = new Background("src/back.png");
	
	private String current;
	private Deque<String> historyStack = new ArrayDeque<>();
	
	public static String MAIN = "MAIN";
	public static String LEVEL = "LEVEL";
	public static String LOGIN = "LOGIN";
	public static String GAME = "GAME";
	public static String SCORE = "SCORE";
	
	private JButton startBtn = new JButton("시작");
	private JButton scoreBtn = new JButton("점수 보기");
	private JButton levelBtn = new JButton("난이도 조절");
	private JLabel lMessage;
	
	private LoginPanel login = new LoginPanel(this);
	private ScoreStartPanel scoreStart = new ScoreStartPanel();

	private LevelPanel level = new LevelPanel(this);
	private GamePanel gamePanel;
	private ScorePanel score = new ScorePanel(this);
	
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
					 // 이 부분부터 게임 시작임
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
		 
		 levelBtn.addActionListener(new ActionListener() {
			 @Override
			 public void actionPerformed(ActionEvent e) {
				 boolean found = login.getFound();
				 boolean ok = login.getOk();
				 
				 if(found == true && ok == true) { // 로그인 성공 시
					 // 게임 레벨 설정 시작
					 chooseLevel();
				 }
				 else { // 로그인 실패 시
					 lMessage.setText("로그인을 먼저 해주세요.");
					 return;
				 }
				 chooseLevel();
			 }
		 });
		 
		 startBtn.setBounds(300, 800, 100, 40);
		 scoreBtn.setBounds(450, 800, 100, 40);
		 levelBtn.setBounds(600, 800, 100, 40);
		 
		 login.setBounds(350, 520, 320, 200);
		 
		 scoreStart.setBounds(350, 50, 320, 467);
		 
		 background.add(startBtn);
		 background.add(scoreBtn);
		 background.add(levelBtn);
		 background.add(login);
		 background.add(scoreStart);
		 
		 start.add(background, MAIN);
		 start.add(level, LEVEL);
		 start.add(score, SCORE);
		 
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
	
	private void chooseLevel() {
		show(LEVEL);
	}
	
	private void initGame() {
		/*getContentPane().removeAll(); // 기존의 컴포넌트들 전부 삭제
		getContentPane().setLayout(null);
		
		revalidate();
		repaint();
		
		gamePanel = new GamePanel();
		gamePanel.setBounds(0, 0, this.getWidth(), this.getHeight());
		add(gamePanel);
		gamePanel.startGame();*/
		show(GAME);
		gamePanel.startGame();
	}
	
	public LoginPanel getLoginPanel() {
		return login;
	}
	
	public static void main(String[] args) {
		new GameFrame();
	}
}