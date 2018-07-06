/*
 * Created on 04-Mar-2004
 * Author is Michael Camacho
 *
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.ArcPathPoint;

public class ToggleArcPointAction extends javax.swing.AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8250176608566816298L;
	private ArcPathPoint arcPathPoint;

	public ToggleArcPointAction(ArcPathPoint _arcPathPoint) {
		arcPathPoint = _arcPathPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		CreateGui.getDrawingSurface().getUndoManager().addNewEdit(
				arcPathPoint.togglePointType());
	}

}
