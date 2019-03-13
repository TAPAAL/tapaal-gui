package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.gui.CreateGui;
import pipe.gui.action.SplitArcAction;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

public class TransportArcHandler extends TimedArcHandler {

	public TransportArcHandler(Arc obj) {
		super(obj);
		enablePopup = true;
	}

	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int popupIndex = 0;
		JMenuItem menuItem;
		JPopupMenu popup = super.getPopup(e);

		if (myObject instanceof TimedTransportArcComponent) {
			final TimedTransportArcComponent tarc = (TimedTransportArcComponent) myObject;

			// Only show properties if its the first arc
			if (tarc.getSource() instanceof Place) {
				menuItem = new JMenuItem("Properties");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tarc.showTimeIntervalEditor();
					}
				});
				popup.insert(menuItem, popupIndex++);
			}
			

			menuItem = new JMenuItem(new SplitArcAction((Arc) myObject, e
					.getPoint()));
			menuItem.setText("Insert Point");
			popup.insert(menuItem, popupIndex++);

			popup.insert(new JPopupMenu.Separator(), popupIndex);
		}
		return popup;
	}
	@Override
	public void mousePressed(MouseEvent e) {

		if (((Arc) myObject).isPrototype()) {
			dispatchToParentWithMouseLocationUpdated(e);
			return;
		}

		if (CreateGui.getApp().isEditionAllowed()) {
			Arc arc = (Arc) myObject;
			if (e.getClickCount() == 2) {
				if (((TimedInputArcComponent)myObject).getSource() instanceof Place) {
					((TimedInputArcComponent) myObject).showTimeIntervalEditor();
				}else {
					arc.getSource().select();
					arc.getTarget().select();
					justSelected = true;
				}
			}
			}
	}
}