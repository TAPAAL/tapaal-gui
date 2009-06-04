package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.dataLayer.Place;
import pipe.dataLayer.TimedPlace;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.undo.UndoManager;


/**
 * Class used to implement methods corresponding to mouse events on places.
 */
public class PlaceHandler 
        extends PlaceTransitionObjectHandler {
   
   
   public PlaceHandler(Container contentpane, Place obj) {
      super(contentpane, obj);
   }
   
   
   /** 
    * Creates the popup menu that the user will see when they right click on a 
    * component 
    */
   public JPopupMenu getPopup(MouseEvent e) {
      int index = 0;
      JPopupMenu popup = super.getPopup(e);      
     
      JMenuItem menuItem = new JMenuItem("Edit Place");      
      menuItem.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            ((Place)myObject).showEditor();
         }
      }); 
      popup.insert(menuItem, index++);
 
      menuItem = new JMenuItem(new ShowHideInfoAction((Place)myObject));      
      if (((Place)myObject).getAttributesVisible() == true){
         menuItem.setText("Hide Attributes");
      } else {
         menuItem.setText("Show Attributes");
      }
      popup.insert(menuItem,index++);
      popup.insert(new JPopupMenu.Separator(),index);      

      return popup;
   }
   
   
   public void mouseClicked(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)){
         if (e.getClickCount() == 2 &&
                 CreateGui.getApp().isEditionAllowed() &&
                 (CreateGui.getApp().getMode() == Pipe.PLACE || 
                 CreateGui.getApp().getMode() == Pipe.SELECT)) {
            ((Place)myObject).showEditor(); 
         } else {
            int currentMarking = ((Place)myObject).getCurrentMarking();
            UndoManager undoManager = CreateGui.getView().getUndoManager();
            
            switch(CreateGui.getApp().getMode()) {
               case Pipe.ADDTOKEN:
                  undoManager.addNewEdit(
                          ((Place)myObject).setCurrentMarking(++currentMarking));
                  break;
               case Pipe.DELTOKEN:
                  if (currentMarking > 0) {
                     undoManager.addNewEdit(
                             ((Place)myObject).setCurrentMarking(--currentMarking));
                  }
                  break;
               default:
                  break;
            }
         }
      }else if (SwingUtilities.isRightMouseButton(e)){
         if (CreateGui.getApp().isEditionAllowed() && enablePopup) { 
            JPopupMenu m = getPopup(e);
            if (m != null) {           
               int x = Zoomer.getZoomedValue(
                       ((Place)myObject).getNameOffsetXObject().intValue(),
                       myObject.getZoom());
               int y = Zoomer.getZoomedValue(
                       ((Place)myObject).getNameOffsetYObject().intValue(),
                       myObject.getZoom());
               m.show(myObject, x, y);
            }
         }
      }/* else if (SwingUtilities.isMiddleMouseButton(e)){
         // TODO - middelclick draw a arrow 
      } */
   }


   public void mouseWheelMoved(MouseWheelEvent e) {
      // 
      if (CreateGui.getApp().isEditionAllowed() == false || 
              e.isControlDown()) {
         return;
      }
      
      UndoManager undoManager = CreateGui.getView().getUndoManager();
      if (e.isShiftDown()) {
    	 /* if ((myObject instanceof TimedPlace)==false){
    		  int oldCapacity = ((Place)myObject).getCapacity();
    		  int oldMarking = ((Place)myObject).getCurrentMarking();

    		  int newCapacity = oldCapacity - e.getWheelRotation();
    		  if (newCapacity < 0) {
    			  newCapacity = 0;
    		  }

    		  undoManager.newEdit(); // new "transaction""
    		  if ((newCapacity > 0) && (oldMarking > newCapacity)){
    			  if (((Place)myObject).getMarkingParameter() != null) {
    				  undoManager.addEdit(((Place)myObject).clearMarkingParameter());
    			  }
    			  undoManager.addEdit(((Place)myObject).setCurrentMarking(newCapacity));
    		  }
    		  undoManager.addEdit(((Place)myObject).setCapacity(newCapacity));
    	  }*/
      } else {
         int oldMarking = ((Place)myObject).getCurrentMarking();
         int newMarking = oldMarking - e.getWheelRotation();
         
         if (newMarking < 0) {
            newMarking = 0;
         }
         if (oldMarking != newMarking) {            
            undoManager.addNewEdit(((Place)myObject).setCurrentMarking(newMarking));
            if (((Place)myObject).getMarkingParameter() != null) {
               undoManager.addEdit(((Place)myObject).clearMarkingParameter());
            }            
         }         
      }
   }
   public void mouseEntered(MouseEvent e){
	   if ((myObject instanceof TimedPlace) && CreateGui.getView().isInAnimationMode()){		   
		  ((TimedPlace) myObject).showAgeOfTokens(true);
	   }else {
		   //do something else;
	   }
   }
   public void mouseExited(MouseEvent e){
	   if ((myObject instanceof TimedPlace) && CreateGui.getView().isInAnimationMode()){
		  ((TimedPlace) myObject).showAgeOfTokens(false);
	   }else {
		   //do something else;
	   }
   }
   
   //Override
   public void mousePressed(MouseEvent e) {
	   if (CreateGui.getApp().isEditionAllowed()){
		   super.mousePressed(e);
	   }else{
		   //do nothing except the things that one do in the simulator (handled somewhere else).
	   }
   }
}
