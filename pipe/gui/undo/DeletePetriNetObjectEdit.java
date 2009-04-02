/*
 * DeletePetriNetObjectEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.Parameter;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.RateParameter;
import pipe.dataLayer.Transition;
import pipe.gui.GuiView;


/**
 *
 * @author Pere Bonet
 */
public class DeletePetriNetObjectEdit 
        extends UndoableEdit {
   
   PetriNetObject pnObject;
   DataLayer model;
   GuiView view;
   Object[] objects;
   Parameter param;
   
   
   /** Creates a new instance of placeWeightEdit */
   public DeletePetriNetObjectEdit(PetriNetObject _pnObject,
            GuiView _view, DataLayer _model) {
      pnObject = _pnObject;
      view = _view;
      model = _model;

      if (pnObject instanceof RateParameter) {
         objects = ((RateParameter)pnObject).getTransitions();
      } else if (pnObject instanceof MarkingParameter) {
         objects = ((MarkingParameter)pnObject).getPlaces();
      } else if (pnObject instanceof Place) {
         MarkingParameter mParam = ((Place)pnObject).getMarkingParameter();
         if (mParam != null) {
            param = mParam;
         }
      } else if (pnObject instanceof Transition) {
         RateParameter rParam = ((Transition)pnObject).getRateParameter();
         if (rParam != null) {
            param = rParam;
         }
      }
      pnObject.markAsDeleted();      
   }

     
   /** */
   public void redo() {
      pnObject.delete();
   }

   
   /** */
   public void undo() {
      pnObject.undelete(model,view);
   }
   
   
   public String toString(){
      return super.toString() + " " + pnObject.getClass().getSimpleName() 
             + " [" +  pnObject.getId() + "]";
   }   
   
}
