import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ScorePanel extends Background {

    private GameFrame gameFrame;
    private JButton backBtn = new JButton("Back");
    private HashMap<String, Integer> teamScoreMap = new HashMap<>();

    private JPanel scoreShowPanel = new JPanel();

    public ScorePanel(GameFrame gameFrame) {
        super("src/startBack.png");
        this.gameFrame = gameFrame;

        setLayout(null);

        // 메인 패널 설정
        scoreShowPanel.setLayout(new GridLayout(0, 1, 5, 5));
        scoreShowPanel.setBackground(new Color(40, 0, 80));
        scoreShowPanel.setBounds(350, 50, 300, 467);

        // 설명 라인 추가
        makeExplainPanel();

        // 스크롤 설정
        JScrollPane scrollPane = new JScrollPane(scoreShowPanel);
        scrollPane.setBounds(350, 50, 300, 467);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane);

        // 점수 로드 + 출력
        showScore();

        // 뒤로가기 버튼
        backBtn.setBounds(0, 0, 100, 30);
        add(backBtn);

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameFrame.show(GameFrame.MAIN);
            }
        });
    }

    private void showScore() {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream("src/score.txt"), StandardCharsets.UTF_8)
            );

            String line;

            // 파일 읽고 해시맵 저장
            while ((line = reader.readLine()) != null) {
                String[] sort = line.split("/");
                if (sort.length != 3) continue;

                String user1 = sort[0];
                String user2 = sort[1];
                int score = Integer.parseInt(sort[2]);

                String team = user1 + "-" + user2;
                teamScoreMap.put(team, score);
            }

            // 정렬
            ArrayList<Map.Entry<String, Integer>> scoreList = new ArrayList<>(teamScoreMap.entrySet());
            scoreList.sort((score1, score2) -> score2.getValue() - score1.getValue());

            // 정렬된 전체 출력
            for (int i = 0; i < scoreList.size(); i++) {
                Map.Entry<String, Integer> entry = scoreList.get(i);
                addScore(i + 1, entry);
            }

        } catch (IOException e) {
            System.out.println("파일 읽는 중 오류 발생");
        }
    }

    // 이름/점수 생성
    private void addScore(int rank, Map.Entry<String, Integer> entry) {
        String[] names = entry.getKey().split("-");
        String user1 = names[0];
        String user2 = names[1];
        int score = entry.getValue();

        JLabel rankLabel = makeLabel(String.valueOf(rank));
        JLabel nameLabel1 = makeLabel(user1);
        JLabel nameLabel2 = makeLabel(user2);
        JLabel scoreLabel = makeLabel(String.valueOf(score));

        JPanel row = new JPanel(new GridLayout(1, 4));
        row.setBackground(new Color(40, 0, 80));
        
        row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        row.add(rankLabel);
        row.add(nameLabel1);
        row.add(nameLabel2);
        row.add(scoreLabel);

        scoreShowPanel.add(row);
    }

    // 데이터 레이블 생성
    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.white);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        return label;
    }

    // 설명 패널
    private void makeExplainPanel() {
        JPanel explain = new JPanel(new GridLayout(1, 4, 5, 5));
        explain.setOpaque(false);

        explain.add(makeLabel("등수"));
        explain.add(makeLabel("이름1"));
        explain.add(makeLabel("이름2"));
        explain.add(makeLabel("점수"));

        scoreShowPanel.add(explain);
    }
}
