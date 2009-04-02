/*
 * Created on 28-Feb-2004
 * Author is Michael Camacho
 *
 */
package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.dataLayer.ArcPathPoint;
import pipe.gui.CreateGui;
import pipe.gui.action.SplitArcPointAction;
import pipe.gui.action.ToggleArcPointAction;


public class ArcPathPointHandler 
        extends PetriNetObjectHandler {

   
   public ArcPathPointHandler(Container contentpane, ArcPathPoint obj) {
      super(contentpane, obj);
      enablePopup = true;
   }
   
   
   /** Creates the popup menu that the user will see when they right click on a component */
   public JPopupMenu getPopup(MouseEvent e) {
      JPopupMenu popup = super.getPopup(e);
      
      if (!((ArcPathPoint)myObject).isDeleteable()) {
         popup.getComponent(0).setEnabled(false);
      }
      
      popup.insert(new JPopupMenu.Separator(), 0);
      
      if (((ArcPathPoint)myObject).getIndex()==0) {
         return null;
      } else {
         JMenuItem menuItem = 
                 new JMenuItem(new ToggleArcPointAction((ArcPathPoint)myObject));
         if (((ArcPathPoint)myObject).getPointType() == ArcPathPoint.STRAIGHT) {
            menuItem.setText("Change to Curved");
         } else{
            menuItem.setText("Change to Straight");
         }
         popup.insert(menuItem,0);
         
         menuItem = new JMenuItem(new SplitArcPointAction((ArcPathPoint)myObject));
         menuItem.setText("Split Point");
         popup.add(menuItem,1);
         
         // The following commented out code can be used for
         // debugging arc issues - Nadeem 18/07/2005
         /*
         menuItem = new JMenuItem(new GetIndexAction((ArcPathPoint)myObject,
                                                     e.getPoint()));
         menuItem.setText("Point Index");
         menuItem.setEnabled(false);
         popup.add(menuItem);
          */
      }
      return popup;
   }
   
   
   public void mousePressed(MouseEvent e) {
      if (myObject.isEnabled()) {
         ((ArcPathPoint)e.getComponent()).setVisibilityLock(true);
         super.mousePressed(e);
      }
   }
   
   
   public void mouseDragged(MouseEvent e) {
      super.mouseDragged(e);
   }
   
   
   public void mouseReleased(MouseEvent e) {
      ((ArcPathPoint)e.getComponent()).setVisibilityLock(false);
      super.mouseReleased(e);
   }
   
   
   public void mouseWheelMoved (MouseWheelEvent e) { 
      if (e.isShiftDown()) {
         CreateGui.getView().getUndoManager().addNewEdit(
                 ((ArcPathPoint)myObject).togglePointType());
      }
   }  
   
}
