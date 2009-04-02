/*
 * SplitArcPointAction.java
 *
 * Created on 21-Jun-2005
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;

import pipe.dataLayer.ArcPathPoint;
import pipe.gui.CreateGui;

/**
 * @author Nadeem
 *
 * This class is used to split a point on an arc into two to  allow the arc to 
 * be manipulated further.
 */
public class SplitArcPointAction 
        extends javax.swing.AbstractAction {
   
   private ArcPathPoint arcPathPoint;
   
   
   public SplitArcPointAction(ArcPathPoint _arcPathPoint) {
      arcPathPoint = _arcPathPoint;
   }
   
   
   public void actionPerformed(ActionEvent e) {
      CreateGui.getView().getUndoManager().addNewEdit(
               arcPathPoint.splitPoint());
   }
   
}
