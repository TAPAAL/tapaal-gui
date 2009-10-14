/*
 * Created on 04-Mar-2004
 * Author is Michael Camacho
 *
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;

import dk.aau.cs.petrinet.PlaceTransitionObject;

import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.TransportArc;
import pipe.gui.CreateGui;


public class DeletePetriNetObjectAction 
        extends AbstractAction {

   private PetriNetObject selected;

   
   public DeletePetriNetObjectAction(PetriNetObject component) {
      selected = component;
   }

   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e) 
   {
      CreateGui.getView().getUndoManager().newEdit(); // new "transaction""
      if(CreateGui.getView().getSelectionObject().getSelectionCount() <= 1)
      {
    	  CreateGui.getView().getUndoManager().deleteSelection(selected);
    	  selected.delete();
      }
      else
      {
    	  CreateGui.getView().getUndoManager().deleteSelection(CreateGui.getView().getSelectionObject().getSelection());
    	  CreateGui.getView().getSelectionObject().deleteSelection();
      }
   }

}
