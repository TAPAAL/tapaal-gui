/*
 * Created on 07-Mar-2004
 */
package pipe.gui.action;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import pipe.gui.CreateGui;


/**
 * GuiAction class
 * @author Maxim and others
 *
 * Handles loading icon based on action name and setting up other stuff
 *
 * Toggleable actions store the toggle state in a way that allows ChangeListeners
 * to be notified of changes
 */
public abstract class GuiAction 
extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2625265285612924719L;


	public GuiAction(String name, String tooltip, String keystroke) {

		super(name);
		URL iconURL=null;

		iconURL = Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + name + ".png");

		if (iconURL != null) {
			putValue(SMALL_ICON, new ImageIcon(iconURL));
		}

		if (tooltip != null) {
			putValue(SHORT_DESCRIPTION, tooltip);
		}

		if (keystroke != null) {
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(keystroke));
		}
	}

	public GuiAction(String name, String tooltip, String keystroke, 
			boolean toggleable) {
		this(name, tooltip, keystroke);
		putValue("selected",new Boolean(false));
	}


	public GuiAction(String name, String tooltip, KeyStroke keyStroke) {
		super(name);
		URL iconURL=null;

		iconURL = Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + name + ".png");

		if (iconURL != null) {
			putValue(SMALL_ICON, new ImageIcon(iconURL));
		}

		if (tooltip != null) {
			putValue(SHORT_DESCRIPTION, tooltip);
		}

		if (keyStroke != null) {
			putValue(ACCELERATOR_KEY,keyStroke);
		} 
	}

	public boolean isSelected() {
		Boolean b = (Boolean)getValue("selected");

		if (b != null) {
			return b.booleanValue();
		}
		return false; // default
	}


	public void setSelected(boolean selected) {

		if (getValue("selected") != null){ 
			putValue("selected", null);
			putValue("selected", new Boolean(selected));

		}


	}

}
