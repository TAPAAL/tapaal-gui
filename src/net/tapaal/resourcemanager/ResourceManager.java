package net.tapaal.resourcemanager;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ResourceManager {
    public static final String imgPath = "resources/Images/";

	private static final ImageIcon satisfiedIcon = loadIcon("satisfied.png");
	private static final ImageIcon notSatisfiedIcon = loadIcon("notsatisfied.png");
	private static final ImageIcon inconclusiveIcon = loadIcon("maybe1.png");
	private static final ImageIcon rerunIcon = loadIcon("maybe.png");
	private static final ImageIcon infoIcon = loadIcon("info.png");
	private static final ImageIcon appIcon = loadIcon("tapaal-icon.png");
	
	private static ImageIcon loadIcon(String name) {
		try {
			return new ImageIcon(ImageIO.read(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(imgPath + name))).getScaledInstance(52, 52, Image.SCALE_SMOOTH));
		} catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
		}
        return null;
	}

    public static ImageIcon getIcon(String name) {
        return Objects.requireNonNull(getIconIfExists(name), "Icon " + name + " not found");
    }

    public static ImageIcon getIconIfExists(String name) {
        URL resourceURL = Thread.currentThread().getContextClassLoader().getResource(ResourceManager.imgPath + name);
        if (resourceURL == null) return null;

        return new ImageIcon(resourceURL);
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
