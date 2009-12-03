package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPopupMenu;

import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;

public class TAPNTransitionHandler extends TransitionHandler {

	public TAPNTransitionHandler(Container contentpane, Transition obj) {
		super(contentpane, obj);
		// TODO Auto-generated constructor stub
	}
	
	//overwrite to remove shift behaviour
	@Override
	public void mouseWheelMoved (MouseWheelEvent e) {

      if (CreateGui.getApp().isEditionAllowed() == false || 
              e.isControlDown()) {
         return;
      }
      
      if (e.isShiftDown()) {
  /*       CreateGui.getView().getUndoManager().addNewEdit(
                 ((Transition)myObject).setTimed(
                 !((Transition)myObject).isTimed()));*/
      } else {
         int rotation = 0;
         if (e.getWheelRotation() < 0) {
            rotation = -e.getWheelRotation() * 135;
         } else {
            rotation = e.getWheelRotation() * 45;
         }
         CreateGui.getView().getUndoManager().addNewEdit(
                 ((Transition)myObject).rotate(rotation));
      }
   }   
   
	/** 
	    * Creates the popup menu that the user will see when they right click on a 
	    * component 
	    */
	   @Override
	public JPopupMenu getPopup(MouseEvent e) {
	      JPopupMenu popup = super.getPopup(e);
	      popup.remove(1); // the show attributes menu point
	      
	      return popup;
	   }

}
