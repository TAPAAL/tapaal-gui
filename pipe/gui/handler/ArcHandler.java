package pipe.gui.handler;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.dataLayer.Arc;
import pipe.dataLayer.InhibitorArc;
import pipe.dataLayer.TimedInputArcComponent;
import pipe.dataLayer.TimedOutputArcComponent;
import pipe.dataLayer.TransportArcComponent;
import pipe.dataLayer.colors.ColoredOutputArc;
import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.action.EditWeightAction;
import pipe.gui.action.SplitArcAction;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.OutputValueEditorPanel;


/**
 * Class used to implement methods corresponding to mouse events on arcs.
 */
public class ArcHandler 
extends PetriNetObjectHandler {


	public ArcHandler(Container contentpane, Arc obj) {
		super(contentpane, obj);
		enablePopup = true;
	}


	/** 
	 * Creates the popup menu that the user will see when they right click on a 
	 * component 
	 */
	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int popupIndex = 0;
		JMenuItem menuItem;
		JPopupMenu popup = super.getPopup(e);

		if (myObject instanceof InhibitorArc) {
			menuItem = new JMenuItem(new EditWeightAction(contentPane,
					(Arc)myObject));
			menuItem.setText("Edit Weight");
			popup.insert(menuItem, popupIndex++);

			menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, 
					e.getPoint()));
			menuItem.setText("Split Arc Segment");
			popup.insert(menuItem, popupIndex++);

			popup.insert(new JPopupMenu.Separator(), popupIndex++);         
			/*CB Joakim Byg - timed arcs should not be handled here*/         
		} else if (myObject instanceof TimedOutputArcComponent && !(myObject instanceof TimedInputArcComponent) && !(myObject instanceof TransportArcComponent)) {
			/*EOC*/
			if(CreateGui.getModel().isUsingColors() && myObject instanceof ColoredOutputArc){
				menuItem = new JMenuItem();            
				menuItem.setText("Properties");
				menuItem.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showOutputValueEditor((ColoredOutputArc)myObject);
					}
				}); 
				popup.insert(menuItem, popupIndex++);
			}

			menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, 
					e.getPoint()));            
			menuItem.setText("Insert Point");
			popup.insert(menuItem, popupIndex++);

			popup.insert(new JPopupMenu.Separator(), popupIndex);
		}
		return popup;
	}


	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		if (CreateGui.getApp().isEditionAllowed() == false){
			return;
		}      
		if (e.getClickCount() == 2){
			Arc arc = (Arc)myObject;
			if (e.isControlDown()){
				CreateGui.getView().getUndoManager().addNewEdit(
						arc.getArcPath().insertPoint(
								new Point2D.Float(arc.getX() + e.getX(), 
										arc.getY() + e.getY()), e.isAltDown()));
			} else {
				if(CreateGui.getModel().isUsingColors()){
					if(arc instanceof ColoredOutputArc){
						showOutputValueEditor((ColoredOutputArc)arc);
					}
				}else{
					arc.getSource().select();
					arc.getTarget().select();
					justSelected = true;
				}
			}
		}
	}


	private void showOutputValueEditor(ColoredOutputArc arc) {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new OutputValueEditorPanel(guiDialog.getRootPane(), arc, CreateGui.getCurrentTab().network(), CreateGui.getView().getUndoManager()));

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		switch (CreateGui.getApp().getMode()) {
		case Pipe.SELECT:
			if (!isDragging){
				break;
			}
			Arc currentObject = (Arc)myObject;
			Point oldLocation = currentObject.getLocation();
			// Calculate translation in mouse
			int transX = (Grid.getModifiedX(e.getX() - dragInit.x));
			int transY = (Grid.getModifiedY(e.getY() - dragInit.y));
			((DrawingSurfaceImpl)contentPane).getSelectionObject().translateSelection(
					transX, transY);
			dragInit.translate(
					-(currentObject.getLocation().x - oldLocation.x - transX),
					-(currentObject.getLocation().y - oldLocation.y - transY));
		}
	}


	@Override
	public void mouseWheelMoved (MouseWheelEvent e) {

		if (CreateGui.getApp().isEditionAllowed() == false){
			return;
		}

		Arc arc = ((Arc)myObject);
		if (arc instanceof TimedOutputArcComponent){

			//         if (e.isControlDown()) {
			//            if (arc.getWeight() == 1) {
			//               if (((NormalArc)arc).hasInvisibleInverse()) {
			//                  if (arc.getSource() instanceof Place){
			//                     if (e.isShiftDown()) {
			//                        arc = ((NormalArc)arc).getInverse();
			//                     }
			//                  } else {
			//                     if (!e.isShiftDown()) {
			//                        arc = ((NormalArc)arc).getInverse();
			//                     }
			//                  }
			//               }
			//               CreateGui.getView().getUndoManager().addNewEdit(
			//                       ((NormalArc)arc).setTagged(!((NormalArc)arc).isTagged()));
			//            }
			//            return;
			//         }
			/*   
         if (((NormalArc)arc).hasInvisibleInverse()) {
            if (arc.getSource() instanceof Place){
               if (e.isShiftDown()) {
                  arc = ((NormalArc)arc).getInverse();                  
               }
            } else {
               if (!e.isShiftDown()) {
                  arc = ((NormalArc)arc).getInverse();                  
               }
            }
         }*/
		}      
		/*
      int oldWeight = arc.getWeight();
      int newWeight = oldWeight - e.getWheelRotation();
      if (newWeight < 1) {
         newWeight = 1;
      }
      if (newWeight != oldWeight) {
         CreateGui.getView().getUndoManager().addNewEdit(
               arc.setWeight(newWeight));
         arc.repaint();
      }*/
	}

}
