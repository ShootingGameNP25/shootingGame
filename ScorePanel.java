import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ScorePanel extends Background{
	private GameFrame gameFrame;
	private JButton backBtn = new JButton("Back");
	private HashMap<String, Integer> scoreMap = new HashMap<>();
	private HashMap<String, Integer> firstMap = new HashMap<>();
	private HashMap<String, Integer> secondMap = new HashMap<>();
	private HashMap<String, Integer> thirdMap = new HashMap<>();
	
	private JPanel level1 = new JPanel();
	private JPanel level2 = new JPanel();
	private JPanel level3 = new JPanel();
	
	private JLabel level1E = new JLabel("<1단계 순위>");
	private JLabel level2E = new JLabel("<2단계 순위>");
	private JLabel level3E = new JLabel("<3단계 순위>");
	
	private JLabel rankE;
	private JLabel nameE;
	private JLabel scoreE;
	
	public ScorePanel(GameFrame gameFrame) {
		super("src/back.png");
		this.gameFrame = gameFrame;
		
		level1.setBounds(30, 50, 300, 467);
		level2.setBounds(350, 50, 300, 467);
		level3.setBounds(670, 50, 300, 467);
		
		level1.setBackground(new Color(40, 0, 80));
		level2.setBackground(new Color(40, 0, 80));
		level3.setBackground(new Color(40, 0, 80));
		
		level1E.setHorizontalAlignment(JLabel.CENTER);
		level1E.setForeground(Color.white);
		level2E.setHorizontalAlignment(JLabel.CENTER);
		level2E.setForeground(Color.white);
		level3E.setHorizontalAlignment(JLabel.CENTER);
		level3E.setForeground(Color.white);
		
		level1.setLayout(new GridLayout(12, 1, 5, 5));
		level1.add(level1E);
		
		level2.setLayout(new GridLayout(12, 1, 5, 5));
		level2.add(level2E);
		
		level3.setLayout(new GridLayout(12, 1, 5, 5));
		level3.add(level3E);
		
		add(level1); makeExplainPanel(1);
		add(level2); makeExplainPanel(2);
		add(level3); makeExplainPanel(3);
		
		showScore();

		setLayout(null);
		backBtn.setBounds(0, 0, 100, 30);
		add(backBtn);
		
		backBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gameFrame.show(GameFrame.MAIN);
			}
		});
	}
	
	private void showScoreByLevel(HashMap h, String level) { // 10명 이상 이하를 구분하고 레벨 별로 출력
		ArrayList<Map.Entry<String, Integer>> scoreLevelList = new ArrayList<>(h.entrySet());
		scoreLevelList.sort((score1, score2) -> score2.getValue() - score1.getValue());
		
		if(level.equals("1")) {
			if(scoreLevelList.size() > 10) {
				rabelLevel(scoreLevelList, 10, 1);
			}
			else {
				rabelLevel(scoreLevelList, scoreLevelList.size(), 1);
			}
		}
		else if(level.equals("2")) {
			if(scoreLevelList.size() > 10) {
				rabelLevel(scoreLevelList, 10, 2);
			}
			else {
				rabelLevel(scoreLevelList, scoreLevelList.size(), 2);
			}
		}
		else if(level.equals("3")) {
			if(scoreLevelList.size() > 10) {
				rabelLevel(scoreLevelList, 10, 3);
			}
			else {
				rabelLevel(scoreLevelList, scoreLevelList.size(), 3);
			}
		}
	}
	
	private void rabelLevel(ArrayList<Map.Entry<String, Integer>> al, int size, int num) { // 여기서 레이블 관련 설정
		for(int i = 0; i < size; i++) {
			Map.Entry<String, Integer> sortScore = al.get(i);
			JLabel rankLabel = new JLabel(String.valueOf(i + 1));
			JLabel nameLabel = new JLabel(sortScore.getKey());
			JLabel scoreLabel = new JLabel(String.valueOf(sortScore.getValue()));
			
			rankLabel.setForeground(Color.white); nameLabel.setForeground(Color.white); scoreLabel.setForeground(Color.white);
			 
			rankLabel.setHorizontalAlignment(JLabel.CENTER);
			nameLabel.setHorizontalAlignment(JLabel.CENTER);
			scoreLabel.setHorizontalAlignment(JLabel.CENTER);
			 
			JPanel row = new JPanel(new GridLayout(1, 4)); 
			row.setBackground(new Color(40, 0, 80)); 
			row.add(rankLabel); row.add(nameLabel); row.add(scoreLabel);
			  
			if(num == 1) {
				level1.add(row);
			}
			else if(num == 2) {
				level2.add(row);
			}
			else if(num == 3) {
				level3.add(row);
			}
		}
	}
	
	private void makeExplainPanel(int level) {
		rankE = new JLabel("등수");
		nameE = new JLabel("이름");
		scoreE = new JLabel("점수");
		
		JPanel explain = new JPanel(new GridLayout(0, 3, 5, 5));
		explain.setOpaque(false);
		
		rankE.setHorizontalAlignment(JLabel.CENTER);
		rankE.setForeground(Color.white);
		explain.add(rankE);
		
		nameE.setHorizontalAlignment(JLabel.CENTER);
		nameE.setForeground(Color.white);
		explain.add(nameE);
		
		scoreE.setHorizontalAlignment(JLabel.CENTER);
		scoreE.setForeground(Color.white);
		explain.add(scoreE);
		
		if(level == 1) {
			level1.add(explain);
		}
		else if(level == 2) {
			level2.add(explain);
		}
		else if(level == 3) {
			level3.add(explain);
		}
	}
	
	private void showScore() {
		String user = "";
		int score = 0;
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/score.txt"), StandardCharsets.UTF_8));
			String line;
			
			while((line = reader.readLine()) != null) {
				String sort[] = line.split("/");
				user = sort[0];
				score = Integer.parseInt(sort[1]);
				scoreMap.put(user, score);
			} // 여기까지 해시맵에 넣는 작업
			
			ArrayList<Map.Entry<String, Integer>> scoreList = new ArrayList<>(scoreMap.entrySet());
			scoreList.sort((score1, score2) -> score2.getValue() - score1.getValue());
			
			for(int i = 0; i < scoreList.size(); i++) {
				Map.Entry<String, Integer> sortScore = scoreList.get(i);
				String level = String.valueOf(sortScore.getValue() % 10);
				
				if(level.equals("1")) {
					String firstName = sortScore.getKey();
					int firstScore = sortScore.getValue() / 10;
					
					firstMap.put(firstName, firstScore);
				}
				else if(level.equals("2")) {
					String secondName = sortScore.getKey();
					int secondScore = sortScore.getValue() / 10;
					
					secondMap.put(secondName, secondScore);
				}
				else if(level.equals("3")) {
					String thirdName = sortScore.getKey();
					int thirdScore = sortScore.getValue() / 10;
					
					thirdMap.put(thirdName, thirdScore);
				}
			}
			showScoreByLevel(firstMap, "1");
			showScoreByLevel(secondMap, "2");
			showScoreByLevel(thirdMap, "3");
		}
		catch(FileNotFoundException e) {
			System.out.println("파일을 찾을 수 없음");
			System.exit(0);
		}
		catch(IOException e) {
			System.out.println("파일 읽는 중 오류 발생");
		}
	}
}
