import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LevelPanel extends Background{
	private GameFrame gameFrame;
	private int gameLevel = 0;
	
	private JLabel explain = new JLabel(
			"<html>" +
			"<div style = 'text-align:center;'>" +
			"레벨을 선택해주세요.<br>" + 
			"<br>" +
			"Easy의 경우 보스의 공격 텀이 길어서 생존하기 쉬워집니다.<br>" +
			"점수는 적게 올라갑니다.<br>" +
			"<br>" +
			"Normal의 경우 보스의 공격 텀이 보통입니다.<br>" +
			"점수는 평범하게 올라갑니다.<br>" +
			"<br>" +
			"Hard의 경우 보스의 공격 텀이 짧아집니다.<br>" +
			"대신 그만큼 점수는 많이 올라갑니다." +
			"</div>" +
			"</html>"
	);
	private JButton easyBtn = new JButton("Easy");
	private JButton normalBtn = new JButton("Normal");
	private JButton hardBtn = new JButton("Hard");
	private JButton backBtn = new JButton("Back");
	
	private JLabel resultLabel;
	
	public LevelPanel(GameFrame gameFrame) {
		super("src/back.png");
		this.gameFrame = gameFrame;
		
		setLayout(null);
		
		setExplains();
		setButtons();
		funBtn();
		
		resultLabel = new JLabel("");
		resultLabel.setBounds(321, 370, 330, 100);
		resultLabel.setHorizontalAlignment(JLabel.CENTER);
		resultLabel.setForeground(Color.white);
		resultLabel.setBackground(new Color(40, 0, 80));
		resultLabel.setOpaque(true);
		add(resultLabel);
	}
	
	private void showResult() {
		int resLevel = getGameLevel();
		
		if(resLevel == 1) {
			resultLabel.setText("Easy 난이도가 선택되었습니다.");
		}
		else if(resLevel == 2) {
			resultLabel.setText("Normal 난이도가 선택되었습니다.");
		}
		else if(resLevel == 3) {
			resultLabel.setText("Hard 난이도가 선택되었습니다.");
		}
	}

	private void setExplains() {
		explain.setBounds(321, 130, 330, 200);
		explain.setForeground(Color.white);
		explain.setOpaque(true);
		explain.setBackground(new Color(40, 0, 80));
		
		add(explain);
	}
	
	
	private void setButtons() {
		easyBtn.setBounds(327, 500, 100, 30);
		normalBtn.setBounds(437, 500, 100, 30);
		hardBtn.setBounds(547, 500, 100, 30);
		backBtn.setBounds(437, 540, 100, 30);
		
		add(easyBtn);
		add(normalBtn);
		add(hardBtn);
		add(backBtn);
	}
	
	private void funBtn() { // 여기서 게임 레벨을 정해줘야 됨
		easyBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLevel(1);
			}
		});
		
		normalBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLevel(2);
			}
		});
		
		hardBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLevel(3);
			}
		});
		
		backBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gameFrame.show(GameFrame.MAIN);
			}
		});
	}
	
	private void setLevel(int gameLevel) { // 레벨 저장
		this.gameLevel = gameLevel;
		showResult();
	}
	
	public int getGameLevel() { // 레벨 리턴
		return gameLevel;
	}
}
