/*
 * ParameterHandler.java
 */
package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.dataLayer.Parameter;
import pipe.gui.action.EditNoteAction;


public class ParameterHandler 
        extends NoteHandler {
   
   
   public ParameterHandler(Container contentpane, Parameter parameter) {
      super(contentpane, parameter);
   }
   
   
   /** Creates the popup menu that the user will see when they right click on a
    * component */
   public JPopupMenu getPopup(MouseEvent e) {
      int index = 0;
      JPopupMenu popup = super.getPopup(e);
      JMenuItem menuItem = 
               new JMenuItem(new EditNoteAction((Parameter)myObject));
      menuItem.setText("Edit parameter");
      popup.insert(menuItem, index++);
      
      popup.insert(new JPopupMenu.Separator(),index);

      return popup;
   }
   
   
   public void mouseClicked(MouseEvent e) {
      if ((e.getComponent() == myObject) || !e.getComponent().isEnabled()){
         if ((SwingUtilities.isLeftMouseButton(e)) && (e.getClickCount() == 2)){
            ((Parameter)myObject).enableEditMode();
         }
      }
   }

}
