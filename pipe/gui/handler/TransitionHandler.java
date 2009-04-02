package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.action.ShowHideInfoAction;

/**
 * Class used to implement methods corresponding to mouse events on transitions.
 */
public class TransitionHandler 
        extends PlaceTransitionObjectHandler
        implements java.awt.event.MouseWheelListener {  
  
   
   public TransitionHandler(Container contentpane, Transition obj) {
      super(contentpane, obj);
   }

   
   public void mouseWheelMoved (MouseWheelEvent e) {

      if (CreateGui.getApp().isEditionAllowed() == false || 
              e.isControlDown()) {
         return;
      }
      
      if (e.isShiftDown()) {
         CreateGui.getView().getUndoManager().addNewEdit(
                 ((Transition)myObject).setTimed(
                 !((Transition)myObject).isTimed()));
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
   public JPopupMenu getPopup(MouseEvent e) {
      int index = 0;
      JPopupMenu popup = super.getPopup(e);
      
      JMenuItem menuItem = new JMenuItem("Edit Transition");      
      menuItem.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            ((Transition)myObject).showEditor();
         }
      });       
      popup.insert(menuItem, index++);             
      
      menuItem = new JMenuItem(new ShowHideInfoAction((Transition)myObject));
      if (((Transition)myObject).getAttributesVisible() == true){
         menuItem.setText("Hide Attributes");
      } else {
         menuItem.setText("Show Attributes");
      }
      popup.insert(menuItem, index++);      
      popup.insert(new JPopupMenu.Separator(), index);

      return popup;
   }
   
   
   public void mouseClicked(MouseEvent e) {   
      if (SwingUtilities.isLeftMouseButton(e)){    
         if (e.getClickCount() == 2 &&
                 CreateGui.getApp().isEditionAllowed() && 
                 (CreateGui.getApp().getMode() == Pipe.TIMEDTRANS || 
                 CreateGui.getApp().getMode() == Pipe.IMMTRANS ||
                 CreateGui.getApp().getMode() == Pipe.SELECT)) {
            ((Transition)myObject).showEditor();
         } 
      }  else if (SwingUtilities.isRightMouseButton(e)){
         if (CreateGui.getApp().isEditionAllowed() && enablePopup) { 
            JPopupMenu m = getPopup(e);
            if (m != null) {           
               int x = Zoomer.getZoomedValue(
                       ((Transition)myObject).getNameOffsetXObject().intValue(),
                       myObject.getZoom());
               int y = Zoomer.getZoomedValue(
                       ((Transition)myObject).getNameOffsetYObject().intValue(),
                       myObject.getZoom());
               m.show(myObject, x, y);
            }
         }
      }
   }
   
}
