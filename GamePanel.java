import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class GamePanel extends JPanel{
	private Boss boss;
	private Player player;
	private BossAttack bossAttack;
	private GameLoop gameLoop;
	private int gameLevel;
	private GameFrame gameFrame;
	private LevelPanel level;
	
	public GamePanel(GameFrame gameFrame) {
		this.gameFrame = gameFrame;
		this.setOpaque(false);
		boss = new Boss(200, 50, "src/boss.jpg");
		bossAttack = new BossAttack(boss);
	}
	
	public void startGame() {
		// 보스 객체
		// 플레이어 객체
		// 총알 객체 -> ArrayList로 된다고 하니 해봄
		// 보스 공격 -> 스레드
		// 게임 전체 총괄 -> 스레드
		//gameLoop.start(); // 게임 전체 총괄
		bossAttack.start(); // 보스 공격
	}
	
	private void bossAttack1() {
		
	}
	
	private void bossAttack2() {
		
	}
	
	private void bossAttack3() {
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    boss.bossDraw(g); // 보스 그리기
	}
	
	private void setGameLevel() {
		
	}
	
	class BossAttack extends Thread{ // 보스의 공격을 관리하는 스레드	
		private Boss boss;
		
		public BossAttack(Boss boss) {
			this.boss = boss;
		}
		
		@Override
		public void run() {
			// 여기서는 보스 공격에 관련된 초기 설정
			// 레벨에 따라 공격간의 턴이 짧아짐
			System.out.println("보스 등장함!");
			while(true) {
				try {
					sleep(100);
				}
				catch(InterruptedException e) {
					return;
				}
			}
		}
	}
	
	class GameLoop extends Thread{ // 게임을 총괄하는 스레드
		
		public GameLoop() {
			
		}
		
		@Override
		public void run() {
			while(true) {
				
			}
		}
	}
	
	class Boss{ // 보스 객체
		private int x, y;
		private int width = 300;
		private int height = 150;
		private ImageIcon icon;
		private Image img;
		
		public Boss(int x, int y, String imagePath) {
			this.x = x;
			this.y = y;
			
			icon = new ImageIcon(imagePath);
			img = icon.getImage();
			
		}
		
		public void bossDraw(Graphics g) {
			g.drawImage(img, x, y, width, height, null);
		}
	}
	
	class Player{ // 플레이어 객체
		private int heart; // 여기서 레벨에 따른 하트 수 조절
		
		public Player(int gameLevel) {
			if(gameLevel == 1) {
				heart = 5;
			}
			else if(gameLevel == 2) {
				heart = 4;
			}
			else if(gameLevel == 3) {
				heart = 3;
			}
		}
	}
}
