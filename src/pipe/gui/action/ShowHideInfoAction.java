package pipe.gui.action;

import java.awt.event.ActionEvent;

import pipe.gui.graphicElements.PlaceTransitionObject;


public class ShowHideInfoAction extends javax.swing.AbstractAction {


	private static final long serialVersionUID = 5942951930546351538L;
	private PlaceTransitionObject pto;

	public ShowHideInfoAction(PlaceTransitionObject component) {
		pto = component;
	}

	public void actionPerformed(ActionEvent e) {
		pto.toggleAttributesVisible();
	}

}
