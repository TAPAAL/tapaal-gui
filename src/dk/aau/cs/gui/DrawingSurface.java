package dk.aau.cs.gui;

import java.awt.Component;

public interface DrawingSurface {
	void updatePreferredSize();

	Component add(Component component);

	void remove(Component component);
}
