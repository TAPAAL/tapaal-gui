/*
 * MarkingParameter.java
 */
package pipe.dataLayer;

import java.util.HashSet;
import java.util.Iterator;

import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.undo.MarkingParameterValueEdit;
import pipe.gui.undo.UndoManager;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.ParameterPanel;


/**
 * This class defines a marking parameter (an integer value > 0)
 * @author Pere Bonet
 */
public class MarkingParameter 
        extends Parameter {
   
   // the value of the parameter
   private Integer value;   

   // the set of places that use this parameter
   private HashSet<Place> placesHashSet;

   
   public MarkingParameter(String _name, Integer _value, int x, int y){
      super (x, y);
      name = _name;
      value = _value;
      placesHashSet = new HashSet();
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
   
   
   public Integer getValue() {
      return value;
   }
   
   
   public UndoableEdit setValue(Integer _value) {
      int oldValue = value;
      value = _value;
      valueChanged = true;
      return new MarkingParameterValueEdit(this, oldValue, value);
   }   

   
   // updates each place in placeHashSet to current parameter value
   public void update() {
      if (valueChanged == true) {
         valueChanged = false;
         Iterator<Place> iterator = placesHashSet.iterator();
         while (iterator.hasNext()){
            Place p = iterator.next();
            p.setCurrentMarking(value);
            p.update();
         }
      }
      this.setText(name + "=" + value);
      this.setSize(this.getMinimumSize());
   }
   
   
   public void delete() {
      Object[] places = placesHashSet.toArray();
      if (places.length > 0) {
         UndoManager undoManager = CreateGui.getView().getUndoManager();
         for (int i = 0; i < places.length; i++) {
            undoManager.addEdit( ((Place)places[i]).clearMarkingParameter());
         }
      }
      super.delete();
   }
   
   
   /** 
    * Adds a place to this placesHashSet
    * @param place The place to be removed
    * @return true if placesHashSet did not already contain place   
    */
   public boolean add (Place place){
      return placesHashSet.add(place);
   }
   
   
   /**
    * Removes a place from placesHashSet
    * @param place The place to be removed
    * @return true if placesHashSet contained place
    */
   public boolean remove (Place place){
      return placesHashSet.remove(place);
   }

   
   public Parameter copy() {
      return new MarkingParameter(name, value, 
                                  (int)this.getX(), (int)this.getY());
   }

   
   public Parameter paste(double x, double y, boolean fromAnotherView) {
      return new MarkingParameter(name, value,
                                  (int)this.getX() + Grid.getModifiedX(x),
                                  (int)this.getY() + Grid.getModifiedY(y));
   }

   
   // returns the array of places that are using this parameter
   public Object[] getPlaces() {
      return placesHashSet.toArray();
   }

   
}
