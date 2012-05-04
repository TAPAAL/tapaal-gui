package dk.aau.cs.io;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import pipe.gui.CreateGui;

public class ResourceManager {
	private static ImageIcon satisfiedIcon = loadIcon("satisfied.png");
	private static ImageIcon notSatisfiedIcon = loadIcon("notsatisfied.png");
	private static ImageIcon inconclusiveIcon = loadIcon("maybe1.png");
	private static ImageIcon rerunIcon = loadIcon("maybe.png");
	private static ImageIcon infoIcon = loadIcon("info.png");
	private static ImageIcon appIcon = loadIcon("tapaal-icon.png");
	
	private static ImageIcon loadIcon(String name) {
		try {
			return new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + name)).getScaledInstance(52, 52, Image.SCALE_SMOOTH));
		} catch (IOException e) {
			e.printStackTrace();
		} catch(IllegalArgumentException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static ImageIcon satisfiedIcon(){
		return satisfiedIcon;
	}
	
	public static ImageIcon notSatisfiedIcon(){
		return notSatisfiedIcon;
	}
	
	public static ImageIcon inconclusiveIcon(){
		return inconclusiveIcon;
	}
	
	public static ImageIcon rerunIcon(){
		return rerunIcon;
	}
	
	public static ImageIcon infoIcon(){
		return infoIcon;
	}
	
	public static ImageIcon appIcon(){
		return appIcon;
	}
}
