package pipe.gui.action;

import java.awt.event.ActionEvent;
import java.awt.Container;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import pipe.dataLayer.NormalArc;
import pipe.gui.CreateGui;

/**
 * Action object that can be used to alternate an Arc between
 * tagged and non-tagged.
 *
 * @author Nick Dingle
 */
public class EditTaggedAction 
        extends AbstractAction {
   
   private static final long serialVersionUID = 2001;
   private Container contentPane;
   private NormalArc selected;
   
   
   public EditTaggedAction(Container cP, NormalArc component) {
      contentPane = cP;
      selected = component;
   }
   
   
   /** Action for toggling tagging on/off */
   public void actionPerformed(ActionEvent e) {
      boolean currentTagged = selected.isTagged();
      int currentWeight = selected.getWeight();
      
      if (currentWeight > 1) {
         JOptionPane.showMessageDialog(contentPane,
                 "Arc weight is greater than 1 and so it cannot be tagged.");
      } else {
         // if currentTagged it true, set it false, if false, set it true
         CreateGui.getView().getUndoManager().addNewEdit(
                 selected.setTagged( ! currentTagged ));
      }
   }
   
}		// end of class
