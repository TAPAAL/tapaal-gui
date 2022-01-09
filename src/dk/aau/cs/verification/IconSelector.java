package dk.aau.cs.verification;

import javax.swing.ImageIcon;

import net.tapaal.resourcemanager.ResourceManager;

public abstract class IconSelector {
	protected static final ImageIcon satisfiedIcon = ResourceManager.satisfiedIcon();
	protected static final ImageIcon notSatisfiedIcon = ResourceManager.notSatisfiedIcon();
	protected static final ImageIcon inconclusiveIcon = ResourceManager.inconclusiveIcon();
	protected static final ImageIcon rerunIcon = ResourceManager.rerunIcon();
	
	public abstract ImageIcon getIconFor(VerificationResult<?> result);
}