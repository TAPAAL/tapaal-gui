/*
 * RateParameter.java
 */
package pipe.dataLayer;

import java.util.HashSet;
import java.util.Iterator;

import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.undo.RateParameterValueEdit;
import pipe.gui.undo.UndoManager;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.ParameterPanel;


/**
 * This class defines a marking parameter (an double value > 0)
 * @author Pere Bonet
 */
public class RateParameter 
        extends Parameter {

   // the value of the parameter
   private Double value;    
   
   // the set of transitions that use this parameter
   private HashSet<Transition> transitionsHashSet;
   
      
   public RateParameter(String _name, Double _value, int x, int y){
      super (x, y);
      name = _name;
      value = _value;
      transitionsHashSet = new HashSet();
      update();
   }
   
   
   public void enableEditMode(){
      // Build interface
      EscapableDialog guiDialog = 
              new EscapableDialog(CreateGui.getApp(),"PIPE2",true);
      guiDialog.add(new ParameterPanel(guiDialog.getRootPane(), this));

      // Make window fit contents' preferred size
      guiDialog.pack();
      
      // Move window to the middle of the screen
      guiDialog.setLocationRelativeTo(null);
      
      guiDialog.setResizable(false);
      guiDialog.setVisible(true);
      
      guiDialog.dispose();      
   }   
   

   public Double getValue() {
      return value;
   }

   
   public UndoableEdit setValue(Double _value) {
      double oldValue = value;
      value = _value;
      valueChanged = true;
      return new RateParameterValueEdit(this, oldValue, value);
   }
   

   /** 
    * Adds a transition to this placesHashSet
    * @param transition The transition to be removed
    * @return true if transitionHashSet did not already contain transition 
    */
   public boolean add (Transition transition){
      return transitionsHashSet.add(transition);
   }
   
   
   /**
    * Removes a transition from transitionsHashSet
    * @param transition The transition to be removed
    * @return true if placesHashSet contained place
    */   
   public boolean remove (Transition transition){
      return transitionsHashSet.remove(transition);
   }   
   
   
   // updates each transition in transitionsHashSet to current parameter value
   public void update() {
      if (valueChanged){
         valueChanged = false;
         Iterator<Transition> iterator = transitionsHashSet.iterator();
         while (iterator.hasNext()){
            Transition t = iterator.next();
            t.setRate(value);
            t.update();
         }
      }      
      this.setText(/*"[R]" + */name + "=" + value);
      this.setSize(this.getMinimumSize());
   }   

   
   public Parameter copy() {
      return new RateParameter(name, value, (int)this.getX(), (int)this.getY());      
   }

   
   public Parameter paste(double x, double y, boolean fromAnotherView) {
      return new RateParameter(name, value,
                               (int)this.getX() + Grid.getModifiedX(x),
                               (int)this.getY() + Grid.getModifiedY(y));            
   }

   
   public void delete(){
      Object[] transitions = transitionsHashSet.toArray();
      if (transitions.length > 0) {
         UndoManager undoManager = CreateGui.getView().getUndoManager();
         for (int i = 0; i < transitions.length; i++) {
            undoManager.addEdit(
                    ((Transition)transitions[i]).clearRateParameter());
         }
      }
      super.delete();      
   }

   
   // returns the array of transitions that are using this parameter
   public Object[] getTransitions() {
      return transitionsHashSet.toArray();
   }
   
}
