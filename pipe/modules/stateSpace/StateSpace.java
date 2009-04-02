/**
 * State Space Module
 * @author James D Bloom (UI) & Clare Clark (Maths)
 * @author Maxim (better UI)
 */
package pipe.modules.stateSpace;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.calculations.TreeTooBigException;
import pipe.dataLayer.calculations.myTree;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.modules.EmptyNetException;
import pipe.modules.Module;


public class StateSpace
        implements Module {
   
   // Main Frame
   private static final String MODULE_NAME = "State Space Analysis";
   
   private PetriNetChooserPanel sourceFilePanel;
   private ResultsHTMLPane results;
   
   public void run(DataLayer pnmlData) {
      // Build interface
      EscapableDialog guiDialog = 
              new EscapableDialog(CreateGui.getApp(), MODULE_NAME, true);
      
      // 1 Set layout
      Container contentPane = guiDialog.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
      
      // 2 Add file browser
      sourceFilePanel = new PetriNetChooserPanel("Source net",pnmlData);
      contentPane.add(sourceFilePanel);
      
      // 3 Add results pane
      contentPane.add(results = new ResultsHTMLPane(pnmlData.getURI()));
      
      // 4 Add button
      contentPane.add(new ButtonBar("Analyse", analyseButtonClick,
              guiDialog.getRootPane()));
      
      // 5 Make window fit contents' preferred size
      guiDialog.pack();
      
      // 6 Move window to the middle of the screen
      guiDialog.setLocationRelativeTo(null);
      
      guiDialog.setVisible(true);
   }
   
   
   public String getName() {
      return MODULE_NAME;
   }
   
   
   /**
    * Analyse button click handler
    */
   ActionListener analyseButtonClick = new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         DataLayer sourceDataLayer = sourceFilePanel.getDataLayer();
         
         int[] markup = sourceDataLayer.getCurrentMarkingVector();
         myTree tree = null;
         
         String s = "<h2>Petri net state space analysis results</h2>";
         if (sourceDataLayer == null) {
            JOptionPane.showMessageDialog( null, "Please, choose a source net", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
         }
         if (!sourceDataLayer.hasPlaceTransitionObjects()) {
            s += "No Petri net objects defined!";
         } else if (markup != null){
            try {
               tree = new myTree(sourceDataLayer, markup);
               boolean bounded = !tree.foundAnOmega;
               boolean safe = !tree.moreThanOneToken;
               boolean deadlock = tree.noEnabledTransitions;
               if (tree.tooBig) {
                  s += "<div class=warning> State space tree expansion aborted " +
                          "because it grew too large. Results will be " +
                          "incomplete.</div>";
               }
               
               s += ResultsHTMLPane.makeTable(
                       new String[]{ "Bounded" , "" + bounded, 
                                     "Safe"    , "" + safe,
                                     "Deadlock", "" + deadlock},
                       2, false, true, false, true);
               
               if (deadlock) {
                  s += "<b>Shortest path to deadlock:</b> ";
                  if (tree.pathToDeadlock.length == 0) {
                     s += "Initial state is deadlocked";
                  } else {
                     for (int i = 0; i < tree.pathToDeadlock.length; i++) {
                        int j = tree.pathToDeadlock[i];
                        if (sourceDataLayer.getTransition(j) != null &&
                                sourceDataLayer.getTransition(j).getName() != null) {
                           s += sourceDataLayer.getTransition(j).getName()+" ";
                        }
                     }
                  }
               }
               results.setEnabled(true);
            } catch (TreeTooBigException e){
               s += e.getMessage();
            }
         } else {
            s += "Error performing analysis";
         }
         results.setText(s);
      }
   };

   
   //<Marc>
   public boolean [] getStateSpace(DataLayer sourceDataLayer) throws EmptyNetException, TreeTooBigException{
        boolean [] result = new boolean [3];
       
        int[] markup = sourceDataLayer.getCurrentMarkingVector();
        myTree tree = null;
        
        if (!sourceDataLayer.hasPlaceTransitionObjects()) {
            throw new EmptyNetException();
         } else if (markup != null){
               tree = new myTree(sourceDataLayer, markup);
               result[0] = !tree.foundAnOmega;
               result[1] = !tree.moreThanOneToken;
               result[2] = tree.noEnabledTransitions;
               if (tree.tooBig) {
                  
               }
         }
       
        return result;
   }
   //</Marc>
   
   
   public boolean [] getStateSpace(){
        boolean [] result = new boolean[3];
        
        return result;
   }
   
}
