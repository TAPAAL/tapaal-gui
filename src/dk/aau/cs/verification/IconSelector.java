package dk.aau.cs.verification;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


import pipe.gui.CreateGui;

public abstract class IconSelector {
	protected static ImageIcon satisfiedIcon = loadIcon("satisfied");
	protected static ImageIcon notSatisfiedIcon = loadIcon("notsatisfied");
	protected static ImageIcon inconclusiveIcon = loadIcon("maybe");

	private static ImageIcon loadIcon(String name) {
		try {
			return new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + name + ".png")).getScaledInstance(52, 52, Image.SCALE_SMOOTH));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public abstract ImageIcon getIconFor(QueryResult result);
}