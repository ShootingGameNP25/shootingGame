import javax.swing.*;
import java.awt.*;

public class PlayerInfoPanel extends JPanel {

    private JLabel nameLabel;
    private JProgressBar hpBar;

    private String userId;
    private boolean isHost;

    public PlayerInfoPanel(String userId, String nickname, boolean isHost) {
        this.userId = userId;
        this.isHost = isHost;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(260, 60));
        setOpaque(false);

        nameLabel = new JLabel(
            (isHost ? "👑 " : "") + nickname + " (" + userId + ")"
        );
        nameLabel.setForeground(Color.WHITE);

        hpBar = new JProgressBar(0, 100);
        hpBar.setValue(100);
        hpBar.setStringPainted(true);

        add(nameLabel, BorderLayout.NORTH);
        add(hpBar, BorderLayout.SOUTH);
    }

    public void setHp(int current, int max) {
        int percent = (int)((current / (double)max) * 100);
        hpBar.setValue(percent);
    }

    public String getUserId() {
        return userId;
    }
}
