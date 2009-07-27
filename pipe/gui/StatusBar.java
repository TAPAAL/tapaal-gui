package pipe.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/* Status Bar to let users know what to do*/
public class StatusBar 
        extends JPanel{
   
   /* Provides the appropriate text for the mode that the user is in */
   public String textforDrawing =
           "Drawing Mode: Click on a button to start adding components to the "
           + "Editor";
   public String textforPlace =
           "Place Mode: Right click on a Place to see menu options " +
           "[Mouse wheel -> marking; Shift + Mouse wheel -> capacity]";
   public String textforTAPNPlace =
       "Place Mode: Right click on a Place to see menu options " +
       "[Mouse wheel -> marking]";
   public String textforTrans = 
           "Transition Mode: Right click on a Transition to see menu " +
           "options [Mouse wheel -> rotate]";
   public String textforTimedTrans =
           "Timed Transition Mode: Right click on a Transition to see menu " +
           "options [Mouse wheel -> rotate]";
   public String textforAddtoken =
           "Add Token Mode: Click on a Place to add a Token";
   public String textforDeltoken = 
           "Delete Token Mode: Click on a Place to delete a Token ";
   public String textforAnimation = 
           "Animation Mode: Red transitions are enabled, click a transition to "
           + "fire it";
   public String textforArc =
           "Arc Mode: Right-Click on an Arc to see menu options " +
           "[Mouse wheel -> rotate]";
   public String textforTransportArc =
       "Transport Arc Mode: Right-Click on an Arc to see menu options " +
       "[Mouse wheel -> rotate]";
   public String textforInhibArc =
           "Inhibitor Mode: Right-Click on an Arc to see menu options " +
           "[Mouse wheel -> rotate]";            
   public String textforMove =
           "Select Mode: Click/drag to select objects; drag to move them";
   public String textforAnnotation =
           "Annotation Mode: Right-Click on an Annotation to see menu options; " +
            "Double click to edit";   
   
   public String textforDrag = "Drag Mode";
   
   public String textforMarking = "Add a marking parameter";
   
   public String textforRate = "Add a rate parameter";
   
   private JLabel label;
   
   
   public StatusBar(){
      super();
      label = new JLabel(textforDrawing); // got to put something in there
      this.setLayout(new BorderLayout(0,0));
      this.add(label);
   }
   
   
   public void changeText(String newText){
      label.setText(newText);
   }
   
   
   public void changeText(int type ){
      switch(type){
         case Pipe.PLACE:
            changeText(textforPlace);
            break;
         case Pipe.TAPNPLACE:
        	 changeText(textforTAPNPlace);
             break;  
         case Pipe.IMMTRANS:
            changeText(textforTrans);
            break;
            
         case Pipe.TIMEDTRANS:
            changeText(textforTimedTrans);
            break;
         case Pipe.TAPNTRANS:
             changeText(textforTrans);
             break;
         case Pipe.ARC:
            changeText(textforArc);
            break;
         case Pipe.TAPNARC:
             changeText(textforArc);
             break;
         case Pipe.TRANSPORTARC:
             changeText(textforTransportArc);
             break;   
         case Pipe.INHIBARC:
            changeText(textforInhibArc);
            break;            
            
         case Pipe.ADDTOKEN:
            changeText(textforAddtoken);
            break;
            
         case Pipe.DELTOKEN:
            changeText(textforDeltoken);
            break;
            
         case Pipe.SELECT:
            changeText(textforMove);
            break;
            
         case Pipe.DRAW:
            changeText(textforDrawing);
            break;
            
         case Pipe.ANNOTATION:
            changeText(textforAnnotation);
            break;
            
         case Pipe.DRAG:
            changeText(textforDrag);
            break;            
            
         case Pipe.MARKING:
            changeText(textforMarking);
            break;

         case Pipe.RATE:
            changeText(textforRate);
            break;  
         default:
            changeText("To-do (textfor" + type);
            break;
      }
   }

}