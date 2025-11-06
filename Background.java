import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Background extends JPanel{
	  private ImageIcon icon; 
	  private Image img; 
	  
	  public Background(String imagePath) {
		  icon = new ImageIcon(imagePath);
		  img = icon.getImage();
	  } 
	  
	  @Override 
	  public void paintComponent(Graphics g) { 
		  super.paintComponent(g); 
		  g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null); 
	  }
}
