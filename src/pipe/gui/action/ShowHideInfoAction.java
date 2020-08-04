package pipe.gui.action;

import java.awt.event.ActionEvent;

import pipe.gui.graphicElements.PlaceTransitionObject;


public class ShowHideInfoAction extends javax.swing.AbstractAction {

	private final PlaceTransitionObject pto;

	public ShowHideInfoAction(PlaceTransitionObject component) {
		pto = component;
	}

	public void actionPerformed(ActionEvent e) {
		pto.toggleAttributesVisible();
	}

}
