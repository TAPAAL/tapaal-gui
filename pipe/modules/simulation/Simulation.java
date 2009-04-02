/**
 * Simulation Module
 * @author James D Bloom (UI)
 * @author Clare Clark (Maths)
 * @author Maxim (replacement UI and cleanup)
 * 
 * @author Davd Patterson (handle null return from fireRandomTransition)
 *
 */
package pipe.modules.simulation;

/**
 * This class does simulation of a net. 
 * 
 * @author unknown
 * 
 * @author Dave Patterson 2 May 2007: Change the code so it checks for 
 * a null being returned from the fireRandomTransition method and 
 * cuts short a cycle. This avoids an endless loop that could occur
 * before this fix. It is related to problem 1699546. The code also
 * puts an error message in the StatusBar at the bottom of the frame. 
 */

// TODO: wjk 04/10/2007 confidence intervals are not working
//                      code could be made a lot more memory efficient
//                      should really use a decent random number generator
//                         e.g. mersenne twister
//                      confidence intervals should use t-table values
//                      for low numbers of replications

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.modules.Module;


public class Simulation
        implements Module {
   
   private static final String MODULE_NAME = "(Broken) Simulation";
   
   private PetriNetChooserPanel sourceFilePanel;
   private ResultsHTMLPane results;
   
   private JTextField jtfFirings, jtfCycles;

   
   public void run(DataLayer pnmlData) {
      EscapableDialog guiDialog = 
              new EscapableDialog(CreateGui.getApp(), MODULE_NAME, true);
      
      // 1 Set layout
      Container contentPane = guiDialog.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
      
      // 2 Add file browser
      sourceFilePanel = new PetriNetChooserPanel("Source net",pnmlData);
      contentPane.add(sourceFilePanel);
      
      // 2.5 Add edit boxes
      JPanel settings=new JPanel();
      settings.setLayout(new BoxLayout(settings,BoxLayout.LINE_AXIS));
      settings.add(new JLabel("Firings:"));
      settings.add(Box.createHorizontalStrut(5));
      settings.add(jtfFirings=new JTextField("100",5));
      settings.add(Box.createHorizontalStrut(10));
      settings.add(new JLabel("Replications:"));
      settings.add(Box.createHorizontalStrut(5));
      settings.add(jtfCycles=new JTextField("5",5));
      settings.setBorder( new TitledBorder(new EtchedBorder(),
                                           "Simulation parameters"));
      settings.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                            settings.getPreferredSize().height));
      contentPane.add(settings);
      
      // 3 Add results pane
      results = new ResultsHTMLPane(pnmlData.getURI());
      contentPane.add(results);
      
      // 4 Add button
      contentPane.add(new ButtonBar("Simulate", simulateButtonClick,
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
            //if (!sourceDataLayer.getPetriNetObjects().hasNext()) {
   
   /**
    * Simulate button click handler
    */
   ActionListener simulateButtonClick = new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         DataLayer sourceDataLayer = sourceFilePanel.getDataLayer();
         String s = "<h2>Petri net simulation results</h2>";
         if (sourceDataLayer == null) {
            JOptionPane.showMessageDialog( null, "Please, choose a source net", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
         }
         if (!sourceDataLayer.hasPlaceTransitionObjects()) {
            s += "No Petri net objects defined!";
         } else {
            try {
               int firings = Integer.parseInt(jtfFirings.getText());
               int cycles = Integer.parseInt(jtfCycles .getText());
               s += simulate(sourceDataLayer, cycles, firings);
               results.setEnabled(true);
            } catch (NumberFormatException e) {
               s += "Invalid parameter!";
            }
         }
         results.setText(s);
      }
   };
   
   
   String simulate(DataLayer data,int cycles,int firings) {
      data.storeState();
      
      int[] marking = data.getInitialMarkingVector();
      double averageTokens[] = new double[marking.length];
      int    totalTokens[]   = new int[marking.length];
      double avgResult[]     = new double[marking.length];
      double errorResult[]   = new double[marking.length];
      
      double overallAverages[][] = new double[cycles][marking.length];
      
      int i,j;
      
      // Initialise arrays
      for (i = 0; i < marking.length; i++) {
         averageTokens[i] = 0;
         totalTokens[i] = 0;
         avgResult[i] = 0;
         errorResult[i] = 0;
      }
      
      //Initialise matrices
      for (i = 0; i < cycles; i++){
         for (j = 0; j < marking.length; j++){
            overallAverages[i][j] = 0;
         }
      }
      
      for (i = 0; i < cycles; i++) {
         //Need to initialise the transition count again
         int transCount = 0;
         
         //Get initial marking
         marking = data.getInitialMarkingVector();
         data.restoreState();
         
         //Initialise matrices for each new cycle
         for (j = 0; j < marking.length; j++) {
            averageTokens[j] = 0;
            totalTokens[j] = 0;
            avgResult[j] = 0;
         }
         
         //Add initial marking to the total
         addTotal(marking, totalTokens);
         
         // Fire as many transitions as required and evaluate averages
         // Changed by Davd Patterson April 24, 2007
         // Handle a null return from fireRandomTransition if no transition
         // can be found.
         for (j = 0; j < firings; j++) {
            System.out.println("Firing " + j + " now");
            //Fire a random transition
            Transition fired = data.fireRandomTransition();
            if ( fired  == null ) {
               CreateGui.getApp().getStatusBar().changeText(
                       "ERROR: No transitions to fire after " + j + " firings" );
               break;		// no point to keep trying to find a transition
            } else { 
               //data.createCurrentMarkingVector();
               //Get the new marking from the dataLayer object
               marking = data.getCurrentMarkingVector();
               
               /*     for (int k=0; k<marking.length; k++)
                System.out.print("" + marking[k] + ",");
                System.out.println("");*/
               
               //Add to the totalTokens array
               addTotal(marking, totalTokens);
               //Increment the transition count
               transCount++;
            }
         }
         
         //Evaluate averages
         for (j = 0; j < marking.length; j++) {
            //Divide by transCount + 1 as total number of markings
            //considered includes the original marking which is outside
            //the loop which counts the number of randomly fired transitions.
            averageTokens[j] = (totalTokens[j] / (transCount + 1.0));
            
            //add appropriate to appropriate row of overall averages for each cycle
            overallAverages[i][j] = averageTokens[j];
         }
      }
      
      //Add up averages for each cycle and divide by number of cycles
      //Perform evaluation on the overallAverages matrix.
      //for each column
      for (i = 0; i < marking.length; i++) {
         //for each row
         for (j = 0; j < cycles; j++){
            avgResult[i] = avgResult[i] + overallAverages[j][i] ;
         }
         avgResult[i] = (avgResult[i]/cycles);
      }
      
      
      //Generate the 95% confidence interval for the table of results
      
      //Find standard deviation and mulitply by 1.95996 assuming approx
      //to gaussian distribution
      
      //For each column in result array
      for (i = 0; i < marking.length; i++) {
         //Find variance
         for (j = 0; j < cycles ; j++) {
            //Sum of squares
            errorResult[i] = errorResult[i] + 
                    ((overallAverages[j][i] - avgResult[i]) * 
                    (overallAverages[j][i] - avgResult[i]));
         }
         
         //Divide by number of cycles
         //Find standard deviation by taking square root
         //Multiply by 1.95996 to give 95% confidence interval
         errorResult[i] = 1.95996 * Math.sqrt(errorResult[i] / cycles);
      }
      
      ArrayList results = new ArrayList();
      DecimalFormat f = new DecimalFormat();
      f.setMaximumFractionDigits(5);
      
      if(averageTokens != null && errorResult != null 
               && averageTokens.length > 0 && errorResult.length > 0) {  
         // Write table of results
         results.add("Place");
         results.add("Average number of tokens");
         results.add("95% confidence interval (+/-)");
         for (i = 0; i < averageTokens.length; i++) {
            results.add(data.getPlace(i).getName());
            results.add(f.format(averageTokens[i]));
            results.add(f.format(errorResult[i]));
         }
      }
      data.restoreState();
      return ResultsHTMLPane.makeTable(results.toArray(),3,false,true,true,true);
   }
   
   private void addTotal(int array[], int dest[]) {
      if (array.length == dest.length) {
         for (int i = 0; i < dest.length; i++) {
            dest[i]+=array[i];
         }
      }
   }
   
}
