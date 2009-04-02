/*
 * Created on 28-Jun-2005
 */
package pipe.modules.gspn;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.Transition;
import pipe.dataLayer.calculations.StateList;
import pipe.dataLayer.calculations.StateSpaceGenerator;
import pipe.dataLayer.calculations.StateSpaceTooBigException;
import pipe.dataLayer.calculations.SteadyStateSolver;
import pipe.dataLayer.calculations.TimelessTrapException;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.io.ImmediateAbortException;
import pipe.modules.Module;


/**
 * @author Nadeem
 *
 * This class enables steady state analysis of GSPN's created
 * using PIPE2
 *
 */
public class GSPNNew 
        extends GSPN 
        implements Module {
   
   private static final String MODULE_NAME = "GSPN Analysis";
   
   public void run(DataLayer pnmlData) {
      // Build interface
      EscapableDialog guiDialog = 
              new EscapableDialog(CreateGui.getApp(),MODULE_NAME,true);
      
      // 1 Set layout
      Container contentPane = guiDialog.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
      
      // 2 Add file browser
      sourceFilePanel = new PetriNetChooserPanel("Source net", pnmlData);
      contentPane.add(sourceFilePanel);
      
      // 3 Add results pane
      results = new ResultsHTMLPane(pnmlData.getURI());
      contentPane.add(results);
      
      // 4 Add button's
      contentPane.add(new ButtonBar("Analyse GSPN", runAnalysis,
              guiDialog.getRootPane()));
      
      // 5 Make window fit contents' preferred size
      guiDialog.pack();
      
      // 6 Move window to the middle of the screen
      guiDialog.setLocationRelativeTo(null);
      
      guiDialog.setVisible(true);
   }
   
   
   /**
    * Analyse button click handler
    */
   ActionListener runAnalysis = new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         long start = new Date().getTime();
         long efinished;
         long ssdfinished;
         long allfinished;
         double explorationtime;
         double steadystatetime;
         double totaltime;
         
         DataLayer sourceDataLayer = sourceFilePanel.getDataLayer();
         
         // This will be used to store the reachability graph data
         File reachabilityGraph = new File("results.rg");
         
         // This will be used to store the steady state distribution
         double[] pi = null;
         
         String s = "<h2>GSPN Steady State Analysis Results</h2>";
         
         if (sourceDataLayer == null) {
            JOptionPane.showMessageDialog( null, "Please, choose a source net", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
         }
         
         if (!sourceDataLayer.hasTimedTransitions()) {
            s += "This Petri net has no timed transitions, "
                    + "so GSPN analysis cannot be performed.";
            results.setText(s);
         } else {
            try {
               StateSpaceGenerator.generate(sourceDataLayer, reachabilityGraph,
                       results);
               efinished = new Date().getTime();
               System.gc();
               pi = SteadyStateSolver.solve(reachabilityGraph);
               ssdfinished = new Date().getTime();
               System.gc();
            } catch (OutOfMemoryError e) {
               s += "Memory error: " + e.getMessage();
               results.setText(s);
               return;
            } catch (ImmediateAbortException e) {
               s += "<br>Error: " + e.getMessage();
               results.setText(s);
               return;
            } catch (TimelessTrapException e) {
               s += "<br>" + e.getMessage();
               results.setText(s);
               return;
            } catch (IOException e) {
               s += "<br>" + e.getMessage();
               results.setText(s);
               return;
            }
            // Now format and display the results nicely
             s += displayResults(sourceDataLayer, reachabilityGraph, pi);
            allfinished = new Date().getTime();
            explorationtime = (efinished - start)/1000.0;
            steadystatetime = (ssdfinished - efinished)/1000.0;
            totaltime = (allfinished - start)/1000.0;
            DecimalFormat f = new DecimalFormat();
            f.setMaximumFractionDigits(5);
            s += "<br>State space exploration took " + f.format(explorationtime)
                     + "s";
            s += "<br>Solving the steady state distribution took " +
                     f.format(steadystatetime) + "s";
            s += "<br>Total time was " + f.format(totaltime) + "s";

            results.setEnabled(true);
            results.setText(s);
            
            if (reachabilityGraph.exists()) {
               if (!reachabilityGraph.delete()) {
                  System.err.println("Could not delete intermediate file.");
               }
            }
         }
      }
   };
   
   
   /**
    * displayResults()
    * Takes the reachability graph file and the steady state distribution
    * and produces nicely formatted output showing these results plus
    * more results based on them.
    *
    * @param sourceDataLayer		The GSPN model data
    * @param rgfile				The reachability graph
    * @param pi					The steady state distribution
    * @param results				The place to display the results
    */
   private String displayResults(DataLayer sourceDataLayer, File rgfile, 
           double[] pi) {
      StateList tangiblestates = null; // This will hold all the tangible states
      String s = new String();
      // If there are greater than MAXSTATESTODISPLAY tangible state,
      // the results will be summarised
      final boolean FULLMODE = false;	// Indicates that all results will be displayed
      final boolean SUMMARYMODE = true;	// Indicates that only a summary of the results should be displayed
      final int MAXSTATESTODISPLAY = 100;
      boolean mode = FULLMODE;
      
      // First load all the tangible states into memory
      try {
         tangiblestates = new StateList(rgfile, false);
      } catch (IOException e) {
         s += e.getMessage();
         return s;
      } catch (StateSpaceTooBigException e) {
         s += e.getMessage();
         return s;
      }
      // Now decide whether to summarise the results
      if (tangiblestates.size() > MAXSTATESTODISPLAY) {
         mode = SUMMARYMODE;
      }
      
      if (mode == SUMMARYMODE) {
         s += "<br>" + "There are " + tangiblestates.size() + " tangible states. ";
         s += "Only a summary of the results will be displayed.";
      } else {
         s += renderTangibleStates(sourceDataLayer, tangiblestates);
         s+= "<br>" + renderPi(pi, tangiblestates);
      }
      double[] averages = averageTokens(pi, tangiblestates);
      if (averages != null) {
         s += "<br>" + renderAverages(sourceDataLayer, averages);
      }
      
      double[][] tokendist = tokenDistribution(pi, tangiblestates);
      if (tokendist != null) {
         s += "<br>" + renderTokenDistribution(sourceDataLayer, tokendist);
      }
      
      double[] throughput = getFastTransitionThroughput(sourceDataLayer,
              tangiblestates, pi);
      if (throughput != null) {
         s += "<br>" + renderTimedTransitionThroughput(sourceDataLayer, throughput);
      }
      
      double[] sojournTimes = calcSojournTime(sourceDataLayer, tangiblestates);
      if (sojournTimes != null) {
         s += "<br>" + renderSojournTimes(sojournTimes, tangiblestates);
      }
  
      return s;
   }
   
   
   /**
    * renderTangibleStates()
    * Turns all the tangible states into a table in the form
    * of a long string with embedded HTML ready for display.
    *
    * @param sourceDataLayer		The GSPN model data
    * @param tangiblestates		The list of tangible states
    * @return
    */
   private String renderTangibleStates(DataLayer sourceDataLayer, 
           StateList tangiblestates){
      return ResultsHTMLPane.makeTable(new String[]{
              "Set of Tangible States",
              renderStateSpaceLinked(sourceDataLayer, tangiblestates)
              }, 1, false, false, true, false);
   }
   
   
   /**
    * renderPi()
    * Turns the steady state distribution vector into a table
    * in the form of a long string with embedded HTML ready
    * for display.
    * @param pi
    * @param states
    * @return
    */
   private String renderPi(double[] pi, StateList states){
      return ResultsHTMLPane.makeTable(new String[]{
              "Steady State Distribution of Tangible States",
              renderLists(pi, states)
              },1 , false, false, true, false);
   }
   
   
   /**
    * averageTokens()
    * Determines the average number of tokens on each place at steady state.
    *
    * @param states    The list of tangible states
    * @return          An array containing the average number of tokens on each 
    *                  place.
    */
   public double[] averageTokens(double[] pi, StateList states){
      int numstates = states.size();
      if (numstates == 0) {
         return null;
      }
      int numplaces = states.get(0).length;
      double[] averages = new double[numplaces];
      DecimalFormat f = new DecimalFormat();
      f.setMaximumFractionDigits(1);
      
      // Set all the averages to zero to begin with
      for (int place = 0; place < numplaces; place++) {
         averages[place] = 0.0;
      }
      
      // Now work out the average number of tokens on a place
      System.out.println("Calculating average number of tokens on a place...");
      for (int marking = 0; marking < numstates; marking++) {
         for (int place = 0; place < numplaces; place++) {
            averages[place] += (states.get(marking))[place] * pi[marking];
         }
         System.out.print(f.format(((double)marking/numstates)*100) + 
                 "% done.\r");
      }
      System.out.println("100.0% done.");
      return averages;
   }
   

   //<Marc>
   
   public class NoTimedTransitionsException extends Exception{};
   
   /* It returns an array with two Objects. The first Object is a double[] which contains
    * Pi, and the second one is an StateList which contains the tangible states.
    * @param sourceDataLayer The GSPN model data
    * @return Pi and the tangible states list*/
   public Object[] getPiAndTangibleStates(DataLayer sourceDataLayer) {
      Object result [] = new Object[2];
      File reachabilityGraph = new File("results.rg");
      try{
         StateSpaceGenerator.generate(sourceDataLayer, reachabilityGraph, null);
         result[0] = SteadyStateSolver.solve(reachabilityGraph);
         result[1] = new StateList(reachabilityGraph, false);
      } catch (StateSpaceTooBigException e) {
         System.out.println(e);
         return null;
      } catch(TimelessTrapException e) {
         System.out.println(e);
         return null;
      } catch(ImmediateAbortException e) {
         System.out.println(e);
         return null;
      } catch(IOException e) {
         System.out.println(e);
         return null;
      }
      
      return result;
   }
   
   
   /* It returns a double array containg the average number of tokens for each place.
    * The order is the same as in the array returned by getPlaces() method in
    * DataLayer class, so you can match each place with its average number of tokens.
    *
    * @param sourceDataLayer The GSPN model data
    * @return an array with the average number of tokens for each place. 
    * @throws NoTimedTransitionException if there is no timed transition in the net.
    */      
   public double[] getAverageTokens(DataLayer sourceDataLayer, double pi[], StateList tangiblestates) throws NoTimedTransitionsException{
      File reachabilityGraph = new File("results.rg");
      
      if(!sourceDataLayer.hasTimedTransitions()){
         throw new NoTimedTransitionsException();
      }
      try{
         if (pi == null || tangiblestates == null){
            StateSpaceGenerator.generate(sourceDataLayer, reachabilityGraph,
                    null);
            if (pi==null){
               pi = SteadyStateSolver.solve(reachabilityGraph);
            }
            if (tangiblestates==null){
               // First load all the tangible states into memory
               tangiblestates = new StateList(reachabilityGraph, false);
            }
         }
         return averageTokens(pi,tangiblestates);
         
      } catch (StateSpaceTooBigException e) {
         System.out.println(e);
         return null;
      } catch(TimelessTrapException e) {
         System.out.println(e);
         return null; 
      } catch(ImmediateAbortException e) {
         System.out.println(e);
         return null;
      } catch(IOException e) {
         System.out.println(e);
         return null;
      }
   }
   
   
   /* It returns a two-dimensional array containing the token distribution for each place.
    * The order is the same as in the array returned by getPlaces() method in
    * DataLayer class, so result[i] would be the token distribution for place i,
    * and result[i][j] would be the probability of having j tokens in place i.
    *
    * @param sourceDataLayer The GSPN model data
    * @return an array with the token distribution for each place. 
    * @throws NoTimedTransitionException if there is no timed transition in the net.
    */
   public double[][] getTokenDistribution(DataLayer sourceDataLayer, double pi[], StateList tangiblestates) throws NoTimedTransitionsException{
      File reachabilityGraph = new File("results.rg");
      if(!sourceDataLayer.hasTimedTransitions()){
         throw new NoTimedTransitionsException();
      }
      try{
         if (pi == null || tangiblestates == null) {
            StateSpaceGenerator.generate(sourceDataLayer, reachabilityGraph, null);
            if (pi==null) {
               pi = SteadyStateSolver.solve(reachabilityGraph);
            }
            if (tangiblestates==null) {
               // First load all the tangible states into memory
               tangiblestates = new StateList(reachabilityGraph, false);
            }
         }
         return tokenDistribution(pi, tangiblestates);
         
      } catch (StateSpaceTooBigException e) {
         System.out.println(e);
         return null;
      } catch(TimelessTrapException e) {
         System.out.println(e);
         return null;
      } catch(ImmediateAbortException e) {
         System.out.println(e);
         return null;
      } catch(IOException e) {
         System.out.println(e);
         return null;
      }
   }
   
   
   /* It returns a two-dimensional array containing the token distribution for each place.
    * The order is the same as in the array returned by getPlaces() method in
    * DataLayer class, so result[i] would be the token distribution for place i,
    * and result[i][j] would be the probability of having j tokens in place i.
    *
    * @param sourceDataLayer The GSPN model data
    * @return an array with the token distribution for each place. 
    * @throws NoTimedTransitionException if there is no timed transition in the net.
    */
   public double[] getThroughput(DataLayer sourceDataLayer, double pi[], StateList tangiblestates) throws NoTimedTransitionsException{
      File reachabilityGraph = new File("results.rg");
      if(!sourceDataLayer.hasTimedTransitions()){
         throw new NoTimedTransitionsException();
      }
      try{
         if (pi == null || tangiblestates == null){
            StateSpaceGenerator.generate(sourceDataLayer, reachabilityGraph, null);
            if (pi==null) {
               pi = SteadyStateSolver.solve(reachabilityGraph);
            }
            if (tangiblestates==null) {
               // First load all the tangible states into memory
               tangiblestates = new StateList(reachabilityGraph, false);
            }
         }
         return getFastTransitionThroughput(sourceDataLayer, tangiblestates, pi); 
         
      } catch (StateSpaceTooBigException e) {
         System.out.println(e);
         return null;
      } catch(TimelessTrapException e) {
         System.out.println(e);
         return null;
      } catch(ImmediateAbortException e) {
         System.out.println(e);
         return null;
      } catch(IOException e) {
         System.out.println(e);
         return null;
      }
   }
   
   //</Marc>
   
   
   /**
    * renderAverages()
    * Turns the array containing the average number of tokens on a place into a 
    * table in the form of a long string with embedded HTML ready for display.
    * @param pnmldata
    * @param data
    * @return
    */
   private String renderAverages(DataLayer pnmldata, double[] data) {
      return ResultsHTMLPane.makeTable(new String[]{
              "Average Number of Tokens on a Place",
              renderLists(data, pnmldata.getPlaces(), 
                       new String[]{"Place", "Number of Tokens"})
              }, 1, false, false, true, false);
   }
   
   
   /**
    * tokenDistribution()
    * Calculates the steady state probability of there being
    * n tokens at place p
    * @param pi
    * @param states
    * @return
    */
   protected double[][] tokenDistribution(double[] pi, StateList states){
      int numstates = states.size();
      if (numstates == 0) {
         return null;
      }
      int numplaces = states.get(0).length;
      int highestnumberoftokens = 1;
      
      DecimalFormat f=new DecimalFormat();
      f.setMaximumFractionDigits(1);
      
      // The following ArrayList tallies up the probabilities.
      // Each element in the ArrayList is an array of doubles.
      // Each row in the array of doubles represents a particular number of 
      // tokens on that place and the value at that row is the probability of 
      // having that many tokens.
      ArrayList counter = new ArrayList(numplaces);
      
      double[] copy;       // A temporary array for storing intermediate results
      double[][] result;
      
      // Each place will have at least 0 or 1 tokens on it at some point so
      // initialise each ArrayList with at a double array of at least two 
      // elements
      for (int p = 0; p < numplaces; p++) {
         counter.add(p, new double[]{0, 0});
      }
      
      System.out.println("Calculating token distribution...");
      // Now go through each tangible state and each place in that state. For 
      // each place, determine the number of tokens on it and add that states 
      // steady state distribution to the appropriate tally for that place and 
      // number of tokens in the ArrayList. Also keep track of the highest 
      // number of tokens for any place as we will need to know it for the final
      // results array.
      for (int marking = 0; marking < numstates; marking++) {
         int[] current = states.get(marking);
         for (int p = 0; p < numplaces; p++) {
            if (current[p] > highestnumberoftokens) {
               highestnumberoftokens = current[p];
            }
            if (current[p] > (((double[])counter.get(p)).length-1)) {
               copy = new double[current[p]+1];
               System.arraycopy((double[])counter.get(p), 0, copy, 0, 
                       ((double[])counter.get(p)).length);
               // Zero all the remainder of the array
               for (int i = ((double[])counter.get(p)).length; i < copy.length; i++){
                  copy[i] = 0.0;
               }
               // Start off the tally for this number of tokens on place p
               copy[current[p]] = pi[marking];
               // Get rid of the old smaller array
               counter.remove(p);
               // Put the new one in instead
               counter.add(p, copy);
            } else {
               copy = (double[])counter.get(p);
               copy[current[p]] += pi[marking];
            }
         }
         System.out.print(f.format(((double)marking/numstates) * 100) +
                 "% done.  \r");
      }
      System.out.println("100.0% done.  ");
      
      // Now put together our results array
      result = new double[numplaces][highestnumberoftokens+1];
      for (int p = 0; p < numplaces; p++) {
         System.arraycopy((double[])counter.get(p), 0, (double[])result[p], 0,
                 ((double[])counter.get(p)).length);
         if (((double[])counter.get(p)).length < highestnumberoftokens) {
            for (int i = ((double[])counter.get(p)).length; i < highestnumberoftokens; i++) {
               result[p][i] = 0.0;
            }
         }
      }
      return result;
   }
   
   
   /**
    * renderTokenDistribution()
    * Turns the array containing the token distributionns into a table in the 
    * form of a long string with embedded HTML ready for display.
    * @param pnmlData
    * @param tokendist
    * @return
    */
   private String renderTokenDistribution(DataLayer pnmlData,
           double[][] tokendist){
      return ResultsHTMLPane.makeTable(new String[]{
              "Token Probability Density",
              renderProbabilityDensity(pnmlData.getPlaces(), tokendist)
              }, 1, false, false, true, false);
   }
   
   
   private String renderProbabilityDensity(Place[] places,
           double[][] probabilities) {
      if(probabilities.length == 0) return "n/a";
      int rows = probabilities.length;
      int cols = probabilities[0].length;
      ArrayList result=new ArrayList();
      // add headers to table
      result.add("");
      for (int i = 0; i < cols; i++) {
         result.add("&micro;=" + i);
      }
      
      DecimalFormat f = new DecimalFormat();
      f.setMaximumFractionDigits(5);
      for (int i = 0; i < rows; i++) {
         result.add(places[i].getName());
         for (int j = 0; j < cols; j++) {
            result.add(f.format(probabilities[i][j]));
         }
      }
      
      return ResultsHTMLPane.makeTable(result.toArray(), cols + 1, false, true,
              true, true);
   }
   
   
   protected double[] getFastTransitionThroughput(DataLayer pnmldata, 
           StateList list, double[] pi){
      int length = list.size();
      
      Transition[] transitions = pnmldata.getTransitions();
      double[] result = new double[transitions.length];
      
      DecimalFormat f = new DecimalFormat();
      f.setMaximumFractionDigits(1);
      System.out.println("Calculating transition throughput...");
      
      for (int marking = 0; marking < length; marking++){
         double specifiedTransitionRate = 0;
         boolean[] transStatus = 
                 pnmldata.getTransitionEnabledStatusArray(list.get(marking));
         for (int tran = 0; tran < transitions.length; tran++) {
            if (transStatus[tran] == true) {
               specifiedTransitionRate = transitions[tran].getRate();
               result[tran]+= (specifiedTransitionRate*pi[marking]);
            }
            System.out.print(f.format(((double)marking/length) * 100) + 
                    "% done.  \r");
         }
      }
      System.out.println("100.0% done.  ");
      return result;
   }
   
   
   //	Format throughput data nicely.
   private String renderTTThroughput(DataLayer pnmldata, double[] data) {
      if (data.length == 0 ) {
         return "n/a";
      }
      int transCount = data.length;
      ArrayList result = new ArrayList();
      // add headers to table
      result.add("Transition");
      result.add("Throughput");
      DecimalFormat f = new DecimalFormat();
      f.setMaximumFractionDigits(5);
      for (int i = 0; i < transCount; i++) {
         if (pnmldata.getTransitions()[i].isTimed() == true) {
            result.add(pnmldata.getTransitions()[i].getName());
            result.add(f.format(data[i]));
         }
      }
      
      return ResultsHTMLPane.makeTable(result.toArray(), 2, false, true, true,
              true);
   }
   
   
   private String renderTimedTransitionThroughput(DataLayer pnmldata,
           double[] throughput){
      return ResultsHTMLPane.makeTable(new String[]{
              "Throughput of Timed Transitions",
              renderTTThroughput(pnmldata, throughput)
              }, 1, false, false, true, false);
   }
   
   
   private String renderSojournTimes(double[] data, StateList list) {
      return ResultsHTMLPane.makeTable(new String[]{
              "Sojourn times for tangible states",
              renderLists(data, list)
              }, 1, false, false, true, false);
   }
   
   
   public String getName() {
      return MODULE_NAME;
   }
   
   
   /**This function determines the sojourn time for each state in a specified set of states.
    * @param DataLater - the net to be analysed
    * @param StateList - the list of tangible markings
    * @return double[] - the array of sojourn times for each specific state
    */
   private double[] calcSojournTime(DataLayer pnmldata, StateList tangibleStates) {
      int numStates = tangibleStates.size();
      int numTrans = pnmldata.getTransitions().length;
      Transition[] trans = pnmldata.getTransitions();
      double[] sojournTime = new double[numStates];
      
      for (int i = 0; i < numStates; i++) {
         boolean[] transStatus = 
                 pnmldata.getTransitionEnabledStatusArray(tangibleStates.get(i));
         double weights = 0;
         for (int j = 0; j <numTrans; j++) {
            if (transStatus[j] == true){
               weights += trans[j].getRate();
            }
         }
         sojournTime[i] = 1/weights;
      }
      return sojournTime;
   }
   
}
