import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatPanel extends Background{
	private JTextField txtInput;
	private JTextArea textArea;
	private GameClient client;
	
	// 채팅을 담을 컨테이너
	private JPanel chatContainer;
	private JButton imgBtn;
	
	private JScrollPane scrollPane;
	
	public ChatPanel() {
		super("chatBack.png");
		
		setLayout(null);
		
		setPreferredSize(new Dimension(280, 450));
		
		// ===== 채팅 메시지 컨테이너 =====
		chatContainer = new JPanel();
		chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
		chatContainer.setOpaque(false); 
		
		// ===== 스크롤 패널 =====
		scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 32, 239, 338);
		scrollPane.setOpaque(false); //스크롤 패널 자체 투명화
		scrollPane.getViewport().setOpaque(false); // 투명화
		scrollPane.setBorder(null); // 테두리 없애기
		scrollPane.setViewportView(chatContainer);
		add(scrollPane);
		
		// ===== 입력창 =====
		Myaction action = new Myaction();
		txtInput = new JTextField();
		txtInput.setBounds(20, 404, 180, 33);
		add(txtInput);
		txtInput.addActionListener(action);
		
		// ===== 이모티콘 버튼 =====
        imgBtn = new JButton("+");
		imgBtn = new JButton("+");
		imgBtn.setBounds(205, 404, 54, 33);
		imgBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openEmoteDialog();
			}
		});
		add(imgBtn);
		
	}
	
	// 채팅 추가 후 항상 맨 아래로 스크롤
	public void refreshScroll() {
		chatContainer.revalidate();
		chatContainer.repaint();
		
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	}
	
	// GameClient와 연결
	public void setGameClient(GameClient client) {
		this.client = client;
	}
	
	// 일반 채팅 메시지 출력
	public void appendChat(String sender, String msg) {
		JPanel row = new JPanel();
		row.setOpaque(false);
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		
		row.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel name = new JLabel("[" + sender + "]");
		name.setForeground(Color.WHITE);
		
		JLabel text = new JLabel(msg);
		text.setForeground(Color.WHITE);
		
		row.add(name);
		row.add(text);
		
		chatContainer.add(row);
		refreshScroll();
	}
	
	// 서버에서 전달되는 시스템 메시지 출력
	public void appendSystemChat(String msg) {
	    JPanel row = new JPanel();
	    row.setOpaque(false);
	    row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
	    
	    row.setAlignmentX(LEFT_ALIGNMENT);

	    JLabel label = new JLabel("[SERVER]" + msg);
	    label.setForeground(Color.WHITE);

	    row.add(label);
	    chatContainer.add(row);
	    refreshScroll();
	}
	
	// 이모티콘 메시지 출력
	public void appendEmote(String sender, String emoteName) {
		JPanel row = new JPanel();
		row.setOpaque(false);
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		
		row.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel name = new JLabel("[" + sender + "]");
		name.setForeground(Color.WHITE);
		
		// 기존 이미지 로드
		ImageIcon imageCon = new ImageIcon(ChatPanel.class.getResource("/emoticons/" + emoteName));
		
		// 채팅용으로 축소
		Image img = imageCon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
		
		JLabel imgLabel = new JLabel(new ImageIcon(img));
		
		row.add(name);
		row.add(imgLabel);
		
		chatContainer.add(row);
		refreshScroll();
	}
	
	// 이모티콘 선택 다이얼로그
	private void openEmoteDialog() {
		JDialog dialog = new JDialog();
		dialog.setTitle("이모티콘을 선택하세요");
		dialog.setModal(true);
		dialog.setLayout(new GridLayout(2, 2, 5, 5));
		
		String[] emotes = {
				"happy.png",
				"sad.png",
				"shouting.png",
				"goodjob.png"
		};
		
		for(int i = 0; i < emotes.length; i++) {
			String emote = emotes[i];
			
			ImageIcon imageCon = new ImageIcon(ChatPanel.class.getResource("/emoticons/" + emote));
			Image img = imageCon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
			JButton btn = new JButton(new ImageIcon(img));
			
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(client != null) {
						// 서버로 전송
						client.sendEmote(emote);
					}
					dialog.dispose();
				}
			});
			dialog.add(btn);
		}
	      dialog.setSize(300, 250);
	      dialog.setLocationRelativeTo(this);
	      dialog.setResizable(false);
	      dialog.setVisible(true);
	}
	
	// 입력창 엔터 처리
	class Myaction implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == txtInput) {
				// 공백 제거
				String msg = txtInput.getText().trim(); 
	            
	            // 빈 메시지가 아닐 때만 전송
	            if (msg.length() > 0) {
	            	
	            	// client 널값 방지
	            	if(client != null) {
	            		client.sendChat(msg);
	            	}
	            	
	            	// 입력창 비우기
	                txtInput.setText(""); 
	            }
			}
		}
	}
}
