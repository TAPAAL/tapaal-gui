package pipe.gui.action;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import pipe.dataLayer.Arc;
import pipe.gui.CreateGui;

/**
 * This class allows the user to change the weight on an arc.
 * 
 * @author unknown 
 * 
 * @author Dave Patterson May 4, 2007: Handle cancel choice without an 
 * exception. Change error messages to ask for a positive integer.
 */
public class EditWeightAction 
        extends AbstractAction {

   private static final long serialVersionUID = 2003;
   private Container contentPane;
   private Arc myArc;
   
   
   public EditWeightAction(Container contentPane, Arc a){
      this.contentPane = contentPane;
      myArc = a;
   }
   
   
   public void actionPerformed(ActionEvent e){
      int currentWeighting = myArc.getWeight();
      
      String input = JOptionPane.showInputDialog(
               "Weighting:", String.valueOf(currentWeighting));
      
      if ( input == null ) {
         return;		// do nothing if the user clicks "Cancel"
      }
      
      try {
         int newWeighting = Integer.parseInt(input);
         if (newWeighting < 1) {
            JOptionPane.showMessageDialog(
                    contentPane, "Weighting cannot be less than 1. Please re-enter");
         } else if (newWeighting != currentWeighting){
            CreateGui.getView().getUndoManager().addNewEdit(
                    myArc.setWeight(newWeighting));
         }      
      } catch (NumberFormatException nfe) {
         JOptionPane.showMessageDialog(contentPane, 
                 "Please enter a positive integer greater than 0.", 
                 "Invalid entry", JOptionPane.ERROR_MESSAGE);
      } catch (Exception exc) {
         exc.printStackTrace();
         JOptionPane.showMessageDialog(contentPane, 
                 "Please enter a positive integer greater than 0.", 
                 "Invalid entry", JOptionPane.ERROR_MESSAGE);
      }
   }
   
}
