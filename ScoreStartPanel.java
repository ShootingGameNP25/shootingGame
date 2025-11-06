import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ScoreStartPanel extends JPanel{
	private int score = 0;
	private JLabel titleLabel = new JLabel("<순위>");
	private HashMap<String, Integer> scoreMap = new HashMap<>();
	
	private JLabel rankE = new JLabel("등수");
	private JLabel nameE = new JLabel("이름");
	private JLabel scoreE = new JLabel("점수");
	private JLabel levelE = new JLabel("레벨");
	
	public ScoreStartPanel() {
		setBackground(new Color(40, 0, 80));
		
		setLayout(new GridLayout(12, 1, 5, 5));
		
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		titleLabel.setForeground(Color.white);
		add(titleLabel);
		
		JPanel explain = new JPanel(new GridLayout(0, 4, 5, 5));
		explain.setOpaque(false);
		add(explain);
		
		rankE.setHorizontalAlignment(JLabel.CENTER);
		rankE.setForeground(Color.white);
		explain.add(rankE);
		
		nameE.setHorizontalAlignment(JLabel.CENTER);
		nameE.setForeground(Color.white);
		explain.add(nameE);
		
		scoreE.setHorizontalAlignment(JLabel.CENTER);
		scoreE.setForeground(Color.white);
		explain.add(scoreE);
		
		levelE.setHorizontalAlignment(JLabel.CENTER);
		levelE.setForeground(Color.white);
		explain.add(levelE);
		
		showScore();
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
			
			if(scoreList.size() <= 10) {
				for(int i = 0; i < scoreList.size(); i++) {
					Map.Entry<String, Integer> sortScore = scoreList.get(i);
					JLabel rankLabel = new JLabel(String.valueOf(i + 1));
					JLabel nameLabel = new JLabel(sortScore.getKey());
					JLabel scoreLabel = new JLabel(String.valueOf(sortScore.getValue() / 10));
					JLabel levelLabel = new JLabel(String.valueOf(sortScore.getValue() % 10));
					
					rankLabel.setForeground(Color.white);
					nameLabel.setForeground(Color.white);
					scoreLabel.setForeground(Color.white);
					levelLabel.setForeground(Color.white);
					
					rankLabel.setHorizontalAlignment(JLabel.CENTER);
					nameLabel.setHorizontalAlignment(JLabel.CENTER);
					scoreLabel.setHorizontalAlignment(JLabel.CENTER);
					levelLabel.setHorizontalTextPosition(JLabel.CENTER);
					
					JPanel row = new JPanel(new GridLayout(1, 4));
					row.setBackground(new Color(40, 0, 80));
					row.add(rankLabel);
					row.add(nameLabel);
					row.add(scoreLabel);
					row.add(levelLabel);
					
					this.add(row);
				}
			}
			else { // 상위 10명
				for(int i = 0; i < 10; i++) {
					Map.Entry<String, Integer> sortScore = scoreList.get(i);
					JLabel rankLabel = new JLabel(String.valueOf(i + 1));
					JLabel nameLabel = new JLabel(sortScore.getKey());
					JLabel scoreLabel = new JLabel(String.valueOf(sortScore.getValue()));
					JLabel levelLabel = new JLabel(String.valueOf(sortScore.getValue() % 10));
					
					rankLabel.setForeground(Color.white);
					nameLabel.setForeground(Color.white);
					scoreLabel.setForeground(Color.white);
					levelLabel.setForeground(Color.white);
					
					rankLabel.setHorizontalAlignment(JLabel.CENTER);
					nameLabel.setHorizontalAlignment(JLabel.CENTER);
					scoreLabel.setHorizontalAlignment(JLabel.CENTER);
					levelLabel.setHorizontalAlignment(JLabel.CENTER);
					
					JPanel row = new JPanel(new GridLayout(1, 3));
					row.setBackground(new Color(40, 0, 80));
					row.add(rankLabel);
					row.add(nameLabel);
					row.add(scoreLabel);
					row.add(levelLabel);
					
					this.add(row);
				}
			}//
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