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
	private JLabel titleLabel = new JLabel("<순위>");
	private HashMap<String, Integer> tenHightMap = new HashMap<>();
	
	private JLabel rankE = new JLabel("등수");
	private JLabel nameE = new JLabel("이름1");
	private JLabel nameE2 = new JLabel("이름2");
	private JLabel scoreE = new JLabel("점수");
	
	public ScoreStartPanel() {
		setBackground(new Color(40, 0, 80));
		setLayout(new GridLayout(12, 1, 5, 5));
		
		// 제목
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		titleLabel.setForeground(Color.white);
		add(titleLabel);
		
		// 등수, 이름 등 출력을 관리
		JPanel explain = new JPanel(new GridLayout(0, 4, 5, 5));
		explain.setOpaque(false);
		add(explain);
		
		addHeadLabel(rankE, explain);
		addHeadLabel(nameE, explain);
		addHeadLabel(nameE2, explain);
		addHeadLabel(scoreE, explain);
		
		showScore();
	}
	
	private void addHeadLabel(JLabel label, JPanel panel) {
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setForeground(Color.white);
        panel.add(label);
	}
	
	private void showScore() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("score.txt"), StandardCharsets.UTF_8));
			String line;
			
			// 파일 읽기 및 해시맵에 넣는 작업
			while((line = reader.readLine()) != null) {
				String sort[] = line.split("/");
				if(sort.length < 3) continue;
				
				String user1 = sort[0];
				String user2 = sort[1];
				int score = Integer.parseInt(sort[2]);
				
				String team = user1 + "-" + user2;
				tenHightMap.put(team, score);
			}
			
			ArrayList<Map.Entry<String, Integer>> scoreList = new ArrayList<>(tenHightMap.entrySet());
			scoreList.sort((score1, score2) -> score2.getValue() - score1.getValue());
			
			// 출력할 개수 결정
			int displayCount = Math.min(10, scoreList.size());
			
            // 출력
            for (int i = 0; i < displayCount; i++) {
                addRow(i + 1, scoreList.get(i));
            }
		}
		catch(FileNotFoundException e) {
			System.out.println("score.txt 파일을 찾을 수 없음");
			System.exit(0);
		}
		catch(IOException e) {
			System.out.println("score.txt 파일 읽는 중 오류 발생");
		}
	}
	
    private void addRow(int rank, Map.Entry<String, Integer> entry) {

        // 팀 이름 분리
        String[] names = entry.getKey().split("-");
        String person1 = names[0];
        String person2 = names[1];

        int score = entry.getValue();

        JLabel rankLabel = makeDataLabel(String.valueOf(rank));
        JLabel nameLabel1 = makeDataLabel(person1);
        JLabel nameLabel2 = makeDataLabel(person2);
        JLabel scoreLabel = makeDataLabel(String.valueOf(score));

        JPanel row = new JPanel(new GridLayout(1, 4));
        row.setBackground(new Color(40, 0, 80));

        row.add(rankLabel);
        row.add(nameLabel1);
        row.add(nameLabel2);
        row.add(scoreLabel);

        this.add(row);
    }

    private JLabel makeDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.white);
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
}
