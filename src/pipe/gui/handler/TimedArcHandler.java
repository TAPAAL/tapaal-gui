package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.dataLayer.NetType;
import pipe.gui.CreateGui;
import pipe.gui.action.SplitArcAction;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

public class TimedArcHandler extends ArcHandler {

	public TimedArcHandler(Arc obj) {
		super(obj);
		enablePopup = true;
	}

	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int popupIndex = 0;
		JMenuItem menuItem;
		JPopupMenu popup = super.getPopup(e);

		if (myObject instanceof TimedInputArcComponent
				&& !(myObject instanceof TimedTransportArcComponent)) {
			if (!CreateGui.getModel().netType().equals(NetType.UNTIMED) && !(myObject instanceof TimedInhibitorArcComponent)) {
				menuItem = new JMenuItem("Properties");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						((TimedInputArcComponent) myObject).showTimeIntervalEditor();
					}
				});
				popup.insert(menuItem, popupIndex++);
			}
			/*
			 * menuItem = new JMenuItem(new EditTimeIntervalAction(contentPane,
			 * (Arc)myObject)); menuItem.setText("Edit Time Interval");
			 * popup.insert(menuItem, popupIndex++);
			 */
			menuItem = new JMenuItem(new SplitArcAction((Arc) myObject, e
					.getPoint()));
			menuItem.setText("Insert Point");
			popup.insert(menuItem, popupIndex++);

			popup.insert(new JPopupMenu.Separator(), popupIndex);
		}
		return popup;
	}
}
