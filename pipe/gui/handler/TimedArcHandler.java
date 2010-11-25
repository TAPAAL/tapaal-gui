package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.dataLayer.Arc;
import pipe.dataLayer.NetType;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TransportArcComponent;
import pipe.gui.CreateGui;
import pipe.gui.action.SplitArcAction;

public class TimedArcHandler extends ArcHandler{

	public TimedArcHandler(Container contentpane, Arc obj) {
		super(contentpane, obj);
		enablePopup = true;
	}
	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int popupIndex = 0;
		JMenuItem menuItem;
		JPopupMenu popup = super.getPopup(e);

		if (myObject instanceof TimedInputArcComponent && ! (myObject instanceof TransportArcComponent) ){
			if(!CreateGui.getModel().netType().equals(NetType.UNTIMED)){
				menuItem = new JMenuItem("Properties");      
				menuItem.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						((TimedInputArcComponent)myObject).showTimeIntervalEditor();
					}
				}); 
				popup.insert(menuItem, popupIndex++);
			} 
			/*
			menuItem = new JMenuItem(new EditTimeIntervalAction(contentPane, (Arc)myObject));
			menuItem.setText("Edit Time Interval");
			popup.insert(menuItem, popupIndex++);
			 */
			menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, e.getPoint()));            
			menuItem.setText("Insert Point");
			popup.insert(menuItem, popupIndex++);
			
			popup.insert(new JPopupMenu.Separator(), popupIndex);
		}
		return popup;
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if (CreateGui.getApp().isEditionAllowed()){
			if (e.getClickCount()==2){
				Arc arc = (Arc)myObject;
				if (e.isControlDown()){
					CreateGui.getView().getUndoManager().addNewEdit(
							arc.getArcPath().insertPoint(
									new Point2D.Float(arc.getX() + e.getX(), 
											arc.getY() + e.getY()), e.isAltDown()));
				}else if(!CreateGui.getModel().netType().equals(NetType.UNTIMED)){
					((TimedInputArcComponent)myObject).showTimeIntervalEditor();
				}
			}else{
				getPopup(e);
				super.mousePressed(e);	
			}
		}
	}
}
