/*
 * Created on 07-Mar-2004
 */
package pipe.gui.action;

import java.awt.*;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.tapaal.resourcemanager.ResourceManager;

/**
 * GuiAction class
 * 
 * @author Maxim and others
 * 
 *         Handles loading icon based on action name and setting up other stuff
 * 
 *         Toggleable actions store the toggle state in a way that allows
 *         ChangeListeners to be notified of changes
 */
public abstract class GuiAction extends AbstractAction {

	public GuiAction(String name, String tooltip) {
		this(name, tooltip, (KeyStroke)null);
	}

	public GuiAction(String name, String tooltip, String keystroke) {

		super(name);
		URL iconURL = null;

		iconURL = Thread.currentThread().getContextClassLoader().getResource(ResourceManager.imgPath + name + ".png");

		if (iconURL != null) {
			putValue(SMALL_ICON, new ImageIcon(iconURL));
		}

		if (tooltip != null) {
			putValue(SHORT_DESCRIPTION, tooltip);
		}

		if (keystroke != null) {
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keystroke));
		}
	}

	private void setAsToggleable(boolean toggleable) {
		
		if (toggleable) {		
			//The key used for storing a Boolean that corresponds to the selected state. This is typically used only for components that have a meaningful selection state. For example, JRadioButton and JCheckBox make use of this but instances of JMenu don't.
			//https://docs.oracle.com/javase/7/docs/api/javax/swing/Action.html#SELECTED_KEY 
			putValue(SELECTED_KEY, false);	
		}
			
	}
	
	public GuiAction(String name, String tooltip, String keystroke, boolean toggleable) {
		this(name, tooltip, keystroke);
		setAsToggleable(toggleable);
		
	}

	public GuiAction(String name, String tooltip, KeyStroke keystroke, boolean toggleable) {
		this(name, tooltip, keystroke);
		setAsToggleable(toggleable);
	}
	
	public GuiAction(String name, String tooltip, boolean toggleable) {
		this(name, tooltip);
		setAsToggleable(toggleable);
	}
	
	public GuiAction(String name, String tooltip, KeyStroke keyStroke) {
		super(name);
		URL iconURL = null;

		iconURL = Thread.currentThread().getContextClassLoader().getResource(ResourceManager.imgPath + name + ".png");

		if (iconURL != null) {
			putValue(SMALL_ICON, new ImageIcon(iconURL));
		}

		if (tooltip != null) {
			putValue(SHORT_DESCRIPTION, tooltip);
		}

		if (keyStroke != null) {
			putValue(ACCELERATOR_KEY, keyStroke);
		}
	}

	public boolean isSelected() {
		Boolean b = (Boolean) getValue(SELECTED_KEY);

		if (b != null) {
			return b;
		}
		return false;
	}

	public void setSelected(boolean selected) {

		if (getValue(SELECTED_KEY) != null) {
			putValue(SELECTED_KEY, selected);
		}

	}

	public void setName(String newName){
	    putValue(NAME, newName);
    }

    public void setTooltip(String newTooltip){
        putValue(SHORT_DESCRIPTION, newTooltip);
    }

}
