package pipe.modules.reachability;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Checkbox;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import jpowergraph.PIPEInitialState;
import jpowergraph.PIPEInitialTangibleState;
import jpowergraph.PIPEInitialVanishingState;
import jpowergraph.PIPELoopWithTextEdge;
import jpowergraph.PIPEState;
import jpowergraph.PIPEVanishingState;
import jpowergraph.PIPETangibleState;

import net.sourceforge.jpowergraph.defaults.DefaultGraph;
import net.sourceforge.jpowergraph.defaults.DefaultNode;
import net.sourceforge.jpowergraph.defaults.TextEdge;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.calculations.StateSpaceGenerator;
import pipe.dataLayer.calculations.TimelessTrapException;
import pipe.dataLayer.calculations.myTree;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GraphFrame;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.io.AbortDotFileGenerationException;
import pipe.io.ImmediateAbortException;
import pipe.io.IncorrectFileFormatException;
import pipe.io.RGFileHeader;
import pipe.io.StateRecord;
import pipe.io.TransitionRecord;
import pipe.modules.Module;

/**
 * @author Matthew Worthington / Edwin Chung / Will Master 
 * Created module to produce the reachability graph representation of a Petri 
 * net. This module makes use of modifications that were made to the state space
 * generator to produce a list of possible states (both tangible and non 
 * tangible) which are then transformed into a dot file. 
 * The file is then translated into its graphical layout using 
 * www.research.att.com hosting of Graphviz. It should therefore be noted that 
 * a live internet connection is required. (Feb/March,2007)
 */
public class ReachabilityGraphGenerator 
        implements Module {

   private static final String MODULE_NAME = "Reachability Graph";
   private PetriNetChooserPanel sourceFilePanel;
   private static ResultsHTMLPane results;
   private EscapableDialog guiDialog = 
           new EscapableDialog(CreateGui.getApp(), MODULE_NAME, true);
   
   private static Checkbox checkBox1 =  
           new Checkbox("Show the intial state(S0) in a different color", false);
   
   private static String dataLayerName;
   
   private static DataLayer pnmlData;
   
   
   public void run(DataLayer pnmlData) {
      // Build interface

      // 1 Set layout
      Container contentPane = guiDialog.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

      // 2 Add file browser
      sourceFilePanel = new PetriNetChooserPanel("Source net", pnmlData);
      contentPane.add(sourceFilePanel);

      // 3 Add results pane
      results = new ResultsHTMLPane(pnmlData.getURI());
      contentPane.add(results);

      // 4 Add button's
      contentPane.add(new ButtonBar("Generate Reachability Graph", generateGraph,
              guiDialog.getRootPane()));
      contentPane.add(checkBox1);      

      // 5 Make window fit contents' preferred size
      guiDialog.pack();

      // 6 Move window to the middle of the screen
      guiDialog.setLocationRelativeTo(null);
      
      checkBox1.setState(false);
      guiDialog.setModal(false);
      guiDialog.setVisible(false);
      guiDialog.setVisible(true);
   }

   
   private ActionListener generateGraph = new ActionListener() {
       
      public void actionPerformed(ActionEvent arg0) {
         long start = new Date().getTime();
         long gfinished;
         long allfinished;
         double graphtime;
         double constructiontime;
         double totaltime;
         
         DataLayer sourceDataLayer = sourceFilePanel.getDataLayer();
         dataLayerName = sourceDataLayer.pnmlName;
         
         // This will be used to store the reachability graph data
         File reachabilityGraph = new File("results.rg");
         
         // This will be used to store the steady state distribution
         String s = "<h2>Reachability Graph Results</h2>";
         
         if (sourceDataLayer == null){
            JOptionPane.showMessageDialog( null, "Please, choose a source net", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
         }
         
         if (!sourceDataLayer.hasPlaceTransitionObjects()) {
            s += "No Petri net objects defined!";
         } else {
            try {
               
               String graph = "Reachability graph";
               
               boolean generateCoverability = false;
               try {
                  StateSpaceGenerator.generate(sourceDataLayer, reachabilityGraph);               
               } catch (OutOfMemoryError e) {
                  // net seems to be unbounded, let's try to generate the
                  // coverability graph
                  generateCoverability = true;
               }  

               // TODO: reachability graph and coverability graph are the same 
               // when the net is bounded so we could just generate the 
               // coverability graph
               if (generateCoverability) {
                  myTree tree = new myTree(sourceDataLayer, 
                                        sourceDataLayer.getCurrentMarkingVector(),
                                        reachabilityGraph);        
                  s += "<br>Reachability graph couldn't be generated ";
                  s += "<br>Coverability graph is shown instead";
                  graph = "Coverability graph";                  
               }
               
               gfinished = new Date().getTime();
               System.gc();
               generateGraph(reachabilityGraph, sourceDataLayer, 
                       generateCoverability);
               allfinished = new Date().getTime();
               graphtime = (gfinished - start)/1000.0;
               constructiontime = (allfinished - gfinished)/1000.0;
               totaltime = (allfinished - start)/1000.0;
               DecimalFormat f=new DecimalFormat();
               f.setMaximumFractionDigits(5);
               s += "<br>Generating " + graph + " took " + 
                       f.format(graphtime) + "s";
               s += "<br>Converting to dot format and constructing took " + 
                       f.format(constructiontime) + "s";
               s += "<br>Total time was " + f.format(totaltime) + "s";	
               results.setEnabled(true);
            } catch (OutOfMemoryError e) {
               s += "Memory error: " + e.getMessage();
               results.setText(s);
               return;
            } catch (StackOverflowError e) {
               s += "StackOverflow Error";
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
            } catch (Exception e) {
               e.printStackTrace();
               s += "<br>Error";
               results.setText(s);
               return;
            } finally {
               if (reachabilityGraph.exists()) {
                  reachabilityGraph.delete();
               }
            }
         }
         results.setText(s);
      }
   };

   
   public String getName() {
      return MODULE_NAME;
   }

   
   private void generateGraph(File rgFile, DataLayer dataLayer, 
           boolean coverabilityGraph) throws AbortDotFileGenerationException, 
           IOException, Exception{
      DefaultGraph graph = createGraph(rgFile, dataLayer, coverabilityGraph);
      GraphFrame frame = new GraphFrame();
      Place[] place = dataLayer.getPlaces();
      String legend ="";
      if (place.length > 0) {
         legend = "{" + place[0].getName();
      }
      for (int i = 1; i < place.length; i++) {
         legend += ", " + place[i].getName();
      }
      legend += "}";
      frame.constructGraphFrame(graph, legend);
      frame.toFront();
      frame.setIconImage((
              new ImageIcon(Thread.currentThread().getContextClassLoader().
              getResource(CreateGui.imgPath + "icon.png")).getImage()));
      frame.setTitle(dataLayerName);
   }
   
 
   private static DefaultGraph createGraph(File rgFile, DataLayer dataLayer, 
           boolean coverabilityGraph) throws IOException{
      DefaultGraph graph = new DefaultGraph();
      
      RGFileHeader header = new RGFileHeader();
      RandomAccessFile reachabilityFile;
      try {
         reachabilityFile = new RandomAccessFile(rgFile, "r");
         header.read(reachabilityFile);
      } catch (IncorrectFileFormatException e1) {
         System.err.println("createGraph: " +
                 "incorrect file format on state space file");
         return graph;
      } catch (IOException e1) {
         System.err.println("createGraph: unable to read header file");
         return graph;
      }
      
      if ((header.getNumStates() + header.getNumTransitions()) > 400) {
         results.setText("The Petri Net contains in excess of 400 elements " +
                 "(state and transitions)");
         throw new IOException("Reachability graph too big for displaying");
      }
      
      ArrayList nodes = new ArrayList();
      ArrayList edges = new ArrayList();
      ArrayList loopEdges = new ArrayList();
      ArrayList loopEdgesTransitions = new ArrayList();
      String label;
      String marking;
      
      int stateArraySize = header.getStateArraySize();
      StateRecord record = new StateRecord();      
      record.read1(stateArraySize, reachabilityFile);         
      label = "S0";
      marking = record.getMarkingString();  
      if (record.getTangible()) {
         if (checkBox1.getState()) {
            nodes.add(coverabilityGraph
                    ? new PIPEInitialState(label,marking)                    
                    : new PIPEInitialTangibleState(label, marking));
         } else {
            nodes.add(coverabilityGraph
                    ? new PIPEState(label,marking)
                    : new PIPETangibleState(label, marking));
         }
      } else {
         if (checkBox1.getState()) {
            nodes.add(coverabilityGraph 
                    ? new PIPEInitialState(label,marking)
                    : new PIPEInitialVanishingState(label, marking));
         } else {
            nodes.add(coverabilityGraph
                    ? new PIPEState(label,marking)
                    : new PIPEVanishingState(label, marking));
         }
      }
      
      for (int count = 1; count < header.getNumStates(); count++) {
         record.read1(stateArraySize, reachabilityFile);         
         label = "S" + count;
         marking = record.getMarkingString();
         if (record.getTangible()) {
            nodes.add(coverabilityGraph 
                    ? new PIPEState(label,marking)
                    : new PIPETangibleState(label, marking));
         } else {
            nodes.add(coverabilityGraph
                    ? new PIPEState(label,marking)
                    : new PIPEVanishingState(label, marking));
         }
      }
      
      reachabilityFile.seek(header.getOffsetToTransitions());
      int numberTransitions = header.getNumTransitions();
      for (int transitionCounter = 0; transitionCounter < numberTransitions; 
              transitionCounter++) {
         TransitionRecord transitions = new TransitionRecord();
         transitions.read1(reachabilityFile);

         int from = transitions.getFromState();
         int to  = transitions.getToState();
         if (from != to) {
            edges.add(new TextEdge(
                    (DefaultNode)(nodes.get(from)),
                    (DefaultNode)(nodes.get(to)),
                    dataLayer.getTransitionName(transitions.getTransitionNo())));
         } else {
            if (loopEdges.contains((DefaultNode)(nodes.get(from)))) {
               int i = loopEdges.indexOf((DefaultNode)(nodes.get(from)));
               
               loopEdgesTransitions.set(i, 
                       (String)loopEdgesTransitions.get(i) + ", " +
                       dataLayer.getTransitionName(transitions.getTransitionNo()));
            } else {
               loopEdges.add((DefaultNode)(nodes.get(from)));
               loopEdgesTransitions.add(
                       dataLayer.getTransitionName(transitions.getTransitionNo()));
            }
         }
      }
      
      for (int i = 0; i < loopEdges.size(); i++) {
         edges.add(new PIPELoopWithTextEdge((DefaultNode)(loopEdges.get(i)),
                 (String)(loopEdgesTransitions.get(i))));
      }
      
      graph.addElements(nodes, edges);
      
      return graph;
    }      
   
}
