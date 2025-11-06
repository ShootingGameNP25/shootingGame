import java.awt.*;
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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginPanel extends JPanel{
	private String userName = "";
	private JLabel name = new JLabel("이름 : ");
	private JTextField nameTf = new JTextField(10);
	private JLabel pw = new JLabel("비밀번호 : ");
	private JPasswordField pwTf = new JPasswordField(10);
	private JButton loginBtn = new JButton("로그인");
	private JButton upBtn = new JButton("회원가입");
	private GameFrame gameFrame;
	private boolean found;
	private boolean ok;
	
	public LoginPanel(GameFrame gameFrame) {
		this.setBackground(new Color(40, 0, 80)); // 배경색 지정
		this.gameFrame = gameFrame;
		
		setLayout(null);

		name.setBounds(50, 30, 100, 50);
		name.setForeground(Color.white);
		name.setHorizontalAlignment(JLabel.CENTER);
		add(name);
		
		nameTf.setBounds(160, 40, 100, 30);
		add(nameTf);
		
		pw.setBounds(50, 80, 100, 50);
		pw.setForeground(Color.white);
		pw.setHorizontalAlignment(JLabel.CENTER);
		add(pw);
		
		pwTf.setBounds(160, 90, 100, 30);
		add(pwTf);
		
		loginBtn.setBounds(50, 150, 100, 30);
		add(loginBtn);
		
		upBtn.setBounds(160, 150, 100, 30);
		add(upBtn);
		
		loginScreen();
	}
	
	public boolean getFound() {
		return found;
	}
	
	public boolean getOk() {
		return ok;
	}
	
	private void loginScreen() {
		loginBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String result = searchInfo();
				gameFrame.getLMessage().setText(result);
			}
		});
		
		upBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String result = makeInfo();
				//lMessage.setMessage(result);
				gameFrame.getLMessage().setText(result);
			}
		});
	}
	
	public String sendMessage(String message) {
		return message;
	}
	
	// 로그인 버튼 함수
	private String searchInfo() {
		String user = "";
		String pw;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/userInfo.txt"), StandardCharsets.UTF_8));
			String name = nameTf.getText();
			String password = new String(pwTf.getPassword());
			String line;
			found = false; // 아이디 존재 시 필요
			ok = false; // 비번까지 맞았을 때
			
			if(name.isEmpty() || password.isEmpty()) {
				found = false;
				ok = false;
				return "아이디 또는 비밀번호를 입력해주세요.";
			}
			
			while((line = reader.readLine()) != null){
				String sort[] = line.split("/");
				user = sort[0];
				pw = sort[1];

				
				if(user.equals(name) && password.equals(pw)) {
					found = true; // 로그인 성공
					ok = true;
					break;
				}
				else if(user.equals(name) && !password.equals(pw)){
					found = true; // 아이디 존재, 비번 틀림
					ok = false;
					return "비밀번호 틀림";
				}
			}
			
			if(!found) {
				return "회원정보가 없습니다. 회원가입 바랍니다.";
			}
			
			nameTf.setText("");
			pwTf.setText("");
		}
		catch(FileNotFoundException e) {
			System.out.println("파일을 찾을 수 없음");
			System.exit(0);
		}
		catch(IOException e) {
			System.out.println("파일 읽는 중 오류 발생");
		}
		return user + " 플레이어님 환영합니다!"; // + " 플레이어님 환영합니다!"
	}
	
	// 회원가입 버튼 함수
	private String makeInfo() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/userInfo.txt"), StandardCharsets.UTF_8));
			String name = nameTf.getText();
			String password = new String(pwTf.getPassword());
			String line;
			boolean found = false;
			
			if(name.isEmpty()) {
				return "정보를 입력해야 회원가입할 수 있습니다.";
			}
			
			while((line = reader.readLine()) != null){
				String sort[] = line.split("/");
				String user = sort[0];
				String pw = sort[1];
				
				if(user.equals(name)) {
					found = true;
					return "아이디 있음";
				}
			}
			
			if(!found) { // 아이디가 없을 경우
				BufferedWriter writer = new BufferedWriter(new FileWriter("src/userInfo.txt", true));
				writer.write(name + "/" + password);
				writer.newLine();
				writer.flush();
				writer.close();
			}
			
			nameTf.setText("");
			pwTf.setText("");
		}
		catch(FileNotFoundException e) {
			System.out.println("파일을 찾을 수 없음");
			System.exit(0);
		}
		catch(IOException e) {
			System.out.println("파일 읽는 중 오류 발생");
		}
		return "회원가입 성공";
	}
	
	/*private class loginInfo {
		private String userName;
		private int pw;
		
		public loginInfo(String userName, int pw) {
			this.userName = userName;
			this.pw = pw;
		}
		
		public String getUserName() {
			return userName;
		}
		
		public int getPw() {
			return pw;
		}
	}*/
}