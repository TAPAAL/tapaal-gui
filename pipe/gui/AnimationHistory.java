package pipe.gui;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;


/** Class to represent the history of the net animation
 *
 * @author Pere Bonet changed and added a number of new functions
 * to fix the unexpected behaviour observed during animation playback.
 * Previously, under certain circumstances, it is possible to step back to state(s)
 * before the initial state and step forward to state(s) after the last recorded
 * transitions in the animation history. 
 * These actions are no longer allowed after the fix.
 * */
public class AnimationHistory
        extends JTextPane {
   
   /**
	 * 
	 */
	private static final long serialVersionUID = -4284885450021683552L;
/** Holds all the transitions in the sequence */
   public Vector<String> fSeq;
   private String initText;
   private Document doc;
   private Style emph;
   private Style bold;
   private Style regular;
   private int currentItem;
   
   
   public AnimationHistory(String text) throws 
           javax.swing.text.BadLocationException {
      super();
      this.setBorder(new EmptyBorder(0,0,0,0));
      //initText = text;
      initStyles();
      doc = getDocument();
      //doc.insertString(doc.getLength(),"",bold);
      fSeq = new Vector<String>();
      fSeq.add("Initial Marking");
      currentItem = 0;
      updateText();
   }
   
   
   private void initStyles() {
      Style def = StyleContext.getDefaultStyleContext().getStyle(
              StyleContext.DEFAULT_STYLE);
      regular = addStyle("regular", def);
      StyleConstants.setFontFamily(def, "SansSerif");
      
      emph = addStyle("currentTransition",regular);
      StyleConstants.setBackground(emph,Color.LIGHT_GRAY);
      
      bold = addStyle("title",regular);
      StyleConstants.setBold(bold,true);
   }
   
   
   public void addHistoryItem(String transitionName) {
      fSeq.add(transitionName);
      currentItem = fSeq.size();
      updateText();
   }
   
   public void addHistoryItemDontChange(String transitionName) {
	      fSeq.add(transitionName);
	      //currentItem = fSeq.size();
	      updateText();
	   }
   
   
   public void clearStepsForward() {
      fSeq.setSize(currentItem);
   }
   
   
   /** Method reinserts the text highlighting the currentItem */
   private void updateText() {
      String newS;
      int count=0;
      Enumeration<String> e = fSeq.elements();
      try {
         doc.remove(0,doc.getLength());
         
         while (e.hasMoreElements()) {
            newS = e.nextElement();
            doc.insertString(doc.getLength(), newS+"\n",
                    (count ==currentItem) ?emph :regular);
            count++;
         }
      } catch (BadLocationException b) {
         System.err.println(b.toString());
      }
   }
   
   
   public void stepForward() {
      if (isStepForwardAllowed()) {
         currentItem++;
      }
      updateText();
   }
   
   
   public void stepBackwards() {
      if (isStepBackAllowed()){
         currentItem--;
      }
      updateText();
   }
   
   
   public boolean isStepForwardAllowed(){
      return currentItem < fSeq.size();
   }
   
   
   public boolean isStepBackAllowed(){
      return currentItem > 1;
   }
   
   public int getCurrentItem(){
	   return currentItem;
   }
   
   public String getElement(int i){
	   if (i >= fSeq.size()){
		   return "";
	   }
	   return fSeq.get(i);    
   }
}
