package dk.aau.cs.verification;

import javax.swing.ImageIcon;

import dk.aau.cs.io.ResourceManager;

public abstract class IconSelector {
	protected static ImageIcon satisfiedIcon = ResourceManager.satisfiedIcon();
	protected static ImageIcon notSatisfiedIcon = ResourceManager.notSatisfiedIcon();
	protected static ImageIcon inconclusiveIcon = ResourceManager.inconclusiveIcon();

	public abstract ImageIcon getIconFor(QueryResult result);
}