import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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

/**
 * ScorePanel
 * - MAIN에서 점수보기로 들어오면 Back -> MAIN
 * - 게임 종료 후 결과로 자동 진입하면 Back -> READY
 */
public class ScorePanel extends Background {
    private GameFrame gameFrame;

    private JButton backBtn = new JButton("Back");
    private HashMap<String, Integer> teamScoreMap = new HashMap<>();
    private JPanel scoreShowPanel = new JPanel();

    // 이번 판 결과 라벨
    private JLabel gameResultLabel;

    // ✅ Back 버튼이 돌아갈 대상(기본 MAIN)
    private String backTarget = GameFrame.MAIN;

    public ScorePanel(GameFrame gameFrame) {
        super("shootingBack.png");
        this.gameFrame = gameFrame;

        setLayout(null);

        scoreShowPanel.setLayout(new GridLayout(0, 1, 5, 5));
        scoreShowPanel.setBackground(new Color(40, 0, 80));
        scoreShowPanel.setBounds(250, 80, 500, 700);

        makeGameResultPanel();
        makeExplainPanel();

        JScrollPane scrollPane = new JScrollPane(scoreShowPanel);
        scrollPane.setBounds(250, 80, 500, 700);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane);

        refreshScoreBoard();

        backBtn.setBounds(450, 800, 100, 30);
        add(backBtn);

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ✅ 상황별로 MAIN or READY
                gameFrame.show(backTarget);
            }
        });
    }

    /** ✅ ScorePanel 열기 전에 GameFrame/GameClient에서 호출해서 Back 목적지를 세팅 */
    public void setBackTarget(String cardName) {
        if (cardName == null) return;
        this.backTarget = cardName;
    }

    // =========================
    // 점수판 새로고침(패널 전체 재구성)
    // =========================
    public void refreshScoreBoard() {
        while (scoreShowPanel.getComponentCount() > 2) {
            scoreShowPanel.remove(2);
        }

        showScore();

        scoreShowPanel.revalidate();
        scoreShowPanel.repaint();
    }

    private void showScore() {
        teamScoreMap.clear();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream("score.txt"), StandardCharsets.UTF_8)
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] sort = line.split("/");
                if (sort.length != 3) continue;

                String user1 = sort[0].trim();
                String user2 = sort[1].trim();

                int score;
                try {
                    score = Integer.parseInt(sort[2].trim());
                } catch (NumberFormatException nfe) {
                    continue;
                }

                String team = user1 + "-" + user2;

                int prev = teamScoreMap.getOrDefault(team, Integer.MIN_VALUE);
                if (score > prev) teamScoreMap.put(team, score);
            }

            ArrayList<Map.Entry<String, Integer>> scoreList = new ArrayList<>(teamScoreMap.entrySet());
            scoreList.sort((a, b) -> b.getValue() - a.getValue());

            for (int i = 0; i < scoreList.size(); i++) {
                Map.Entry<String, Integer> entry = scoreList.get(i);
                addScore(i + 1, entry);
            }

        } catch (FileNotFoundException e) {
            System.out.println("score.txt 파일을 찾을 수 없음 (점수 기록 없음)");
        } catch (IOException e) {
            System.out.println("score.txt 파일 읽는 중 오류 발생: " + e.getMessage());
        }
    }

    private void saveScoreToFile(String p1, String p2, int totalScore) {
        String a = (p1 == null) ? "" : p1.trim();
        String b = (p2 == null) ? "" : p2.trim();
        if (a.isEmpty() && b.isEmpty()) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("score.txt", true))) {
            bw.write(a + "/" + b + "/" + totalScore);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("score.txt 저장 중 오류: " + e.getMessage());
        }
    }

    private void addScore(int rank, Map.Entry<String, Integer> entry) {
        String[] names = entry.getKey().split("-");
        String user1 = names.length > 0 ? names[0] : "";
        String user2 = names.length > 1 ? names[1] : "";
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

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.white);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        return label;
    }

    private void makeGameResultPanel() {
        gameResultLabel = new JLabel("이번 판 결과 : -");
        gameResultLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        gameResultLabel.setForeground(Color.YELLOW);
        gameResultLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreShowPanel.add(gameResultLabel);
    }

    public void setGameResult(String p1, String p2, int totalScore) {
        if (gameResultLabel == null) return;

        String a = (p1 == null) ? "" : p1;
        String b = (p2 == null) ? "" : p2;

        gameResultLabel.setText("이번 판 결과 : " + a + " + " + b + "  =  " + totalScore);
        gameResultLabel.revalidate();
        gameResultLabel.repaint();

        saveScoreToFile(a, b, totalScore);
        refreshScoreBoard();
    }

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
