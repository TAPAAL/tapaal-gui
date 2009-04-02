package pipe.modules.dnamaca;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GraphPanelPane;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.modules.Module;

public class Dnamaca 
        implements Module {
   
   private static final String MODULE_NAME = "DNAmaca";
   private Place places[];
   private Transition transitions[];
   private DataLayer pnmldata;
   private String modString = "";
   private final static String urtaStrings[] = {"Parsing input file", 
           "Generating state-space", "Peforming functional analysis", 
           "Computing steady-state probabilities", "Identifying target states",
           "Compiling response-time analyser", "Initialising Uniformiser",
           "Running uniformise", "calculating answers for each t_point", 
           "powered down"};
   private File modFile;
   private EscapableDialog guiDialog;
   private JButton resultsButton;
   private ArrayList resultsX = new ArrayList();
   private ArrayList resultsY = new ArrayList();
   private ResultsHTMLPane resultText;
   private GraphPanelPane resultsGraph;
   private String urtaExecutablePath;
   
   private JTextField jtfTargetCondition;
   private JTextField jtfSourceCondition;
   private JTextField jtfTStart;
   private JTextField jtfTStop;
   private JTextField jtfTStep;
   
   private JCheckBox jcbCumulative;
   
   
        /* (non-Javadoc)
         * @see pipe.modules.Module#getName()
         */
   public String getName() {
      return MODULE_NAME;
   }
   
   
        /* (non-Javadoc)
         * @see pipe.modules.Module#run(pipe.dataLayer.DataLayer)
         */
   public void run(DataLayer _pnmldata) {
      pnmldata = _pnmldata;
      places = pnmldata.getPlaces();
      transitions = pnmldata.getTransitions();
      
      // Build interface
      guiDialog = new EscapableDialog(CreateGui.getApp(), MODULE_NAME, true);
      
      Container contentPane=guiDialog.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
      
      resultText = new ResultsHTMLPane(pnmldata.getURI());
      contentPane.add(resultText);
      resultsGraph = new GraphPanelPane();
      guiDialog.getContentPane().add(resultsGraph,0);
      resultsGraph.setVisible(false);
      
      JPanel optionsPanel=new JPanel();
      optionsPanel.setLayout(new BoxLayout(optionsPanel,BoxLayout.PAGE_AXIS));
      optionsPanel.setBorder(new TitledBorder(new EtchedBorder(),"Conditions"));
      JPanel topPanel=new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.LINE_AXIS));
      topPanel.add(new JLabel("Source condition:"));
      topPanel.add(Box.createHorizontalStrut(5));
      topPanel.add(jtfSourceCondition=new JTextField(5));
      topPanel.add(Box.createHorizontalStrut(10));
      topPanel.add(new JLabel("Target condition:"));
      topPanel.add(Box.createHorizontalStrut(5));
      topPanel.add(jtfTargetCondition=new JTextField(5));
      JPanel bottomPanel=new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.LINE_AXIS));
      bottomPanel.add(new JLabel("T Start:"));
      bottomPanel.add(Box.createHorizontalStrut(5));
      bottomPanel.add(jtfTStart=new JTextField(5));
      bottomPanel.add(Box.createHorizontalStrut(10));
      bottomPanel.add(new JLabel("T Stop:"));
      bottomPanel.add(Box.createHorizontalStrut(5));
      bottomPanel.add(jtfTStop=new JTextField(5));
      bottomPanel.add(Box.createHorizontalStrut(10));
      bottomPanel.add(new JLabel("T Step:"));
      bottomPanel.add(Box.createHorizontalStrut(5));
      bottomPanel.add(jtfTStep=new JTextField(5));
      bottomPanel.add(Box.createHorizontalStrut(5));
      bottomPanel.add(jcbCumulative=new JCheckBox("Cumulative",false));
      
      optionsPanel.add(topPanel);
      optionsPanel.add(Box.createVerticalStrut(5));
      optionsPanel.add(bottomPanel);
      
      // TODO: remove these since they're not generally applicable
      jtfSourceCondition.setText("P4 == 1");
      jtfTargetCondition.setText("P4 == 3");
      jtfTStart.setText("0.1");
      jtfTStop.setText("30");
      jtfTStep.setText("0.1");
      
      optionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,optionsPanel.getPreferredSize().height));
      contentPane.add(optionsPanel);
      
      String buttonLabels[] = {"Run DNAmaca", "Toggle graph"};
      ActionListener buttonHandlers[] = {runDnamacaAction, runResultsAction};
      ButtonBar buttons;
      contentPane.add(buttons = new ButtonBar(buttonLabels, buttonHandlers));
      resultsButton = ((JButton)buttons.getComponent(1));
      resultsButton.setEnabled(false);
      
      guiDialog.pack();
      guiDialog.setLocationRelativeTo(null);
      guiDialog.setVisible(true);
   }
   
   ActionListener runDnamacaAction=new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         if (!getUrtaPath()) {
            resultText.setText(
                    "The command 'urta' was not found on your system path.");
         } else if (pnmldata.getPlaces().length==0) {
            resultText.setText(
                    "You cannot run DNAmaca on a net without places.");
         } else if (!hasTimed()) {
            resultText.setText(
                    "You cannot run DNAmaca on a net without a timed transition.");
         } else {
            try {
               modFile = File.createTempFile("dnamaca", ".mod");
               FileWriter out = new FileWriter(modFile);
               generateMod();
               out.write(modString);
               out.close();
               DnamacaRun dnamaca = new DnamacaRun();
            } catch (IOException e3) {
               System.out.println("Could not write to temporary file!");
               e3.printStackTrace();
            }
         }
      }
   };
   
   
   ActionListener runResultsAction=new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
         if (!resultText.isVisible()) {
            resultsGraph.setVisible(false);
            resultText.setVisible(true);
//           resultsButton.setText("View Graph");
         } else {
            resultText.setVisible(false);
            resultsGraph.setVisible(true);
//            resultsButton.setText("View Results");
         }
         resultText.setEnabled(true);
         guiDialog.validate();
      }
   };
   
   
   public void displayResults() {
      ArrayList resultList = new ArrayList();
      resultList.add("x"); 
      resultList.add("y");
      
      for(int i=0; i<resultsX.size(); i++) {
         resultList.add(resultsX.get(i));
         resultList.add(resultsY.get(i));
      }
      resultText.setText("Passage Time Analysis<br>" + 
              ResultsHTMLPane.makeTable(resultList.toArray(),
                                        2, false,true, true, false));
   }
   
   
   private void generateMod() {
      modString = "";
      model();
//		performance();
      passageTime();
   }
   
   
   private void model() {
      modString += "\\model{\n";
      stateVector();
      initial();
      transitions();
      modString += "}\n\n";
   }
   
   
   private void stateVector() {
      modString += "\t\\statevector{\n";
      modString += "\t\t\\type{short}{";
      
      modString += places[0].getId();
      for(int i=1; i<places.length; i++) {
         modString += ", "+places[i].getId();
      }
      
      modString += "}\n";
      modString += "\t}\n\n";
   }
   
   
   private void initial() {
      modString += "\t\\initial{\n";
      
      modString += "\t\t";
      for(int i=0; i<places.length; i++) {
         modString += places[i].getId() + " = " + places[i].getCurrentMarking() + 
                 "; ";
      }
      modString += "\n\t}\n";
   }
   
   
   private void transitions() {
      for(int i=0; i<transitions.length; i++) {
         modString += "\t\\transition{"+transitions[i].getId()+"}{\n";
         modString += "\t\t\\condition{" + getTransitionConditions(i) + "}\n";
         modString += "\t\t\\action{\n";
         
         Iterator arcsTo = transitions[i]. getConnectToIterator();
         while (arcsTo.hasNext()) {
            String currentId = ((Arc)arcsTo.next()).getSource().getId();
            modString += "\t\t\tnext->"+currentId;
            modString += " = "+currentId+" - 1;\n";
         }

         Iterator arcsFrom = transitions[i]. getConnectFromIterator();
         while (arcsFrom.hasNext()) {
            String currentId = ((Arc)arcsFrom.next()).getTarget().getId();
            modString += "\t\t\tnext->"+currentId;
            modString += " = "+currentId+" + 1;\n";
         }
         
         modString += "\t\t}\n";
         
         if (transitions[i].isTimed()) {
            modString += "\t\t\\rate{" + transitions[i].getRate() + "}\n";
         } else {
            modString += "\t\t\\weight{" + transitions[i].getRate() + "}\n";
         }
         modString += "\t}\n";
      }
   }
   
   
   private void performance() {
      modString += "\\performance{\n";
      tokenDistribution();
      transitionMeasures();
      modString += "}\n";
   }

   
   private void tokenDistribution() {
      for(int i=0; i<places.length; i++) {
         modString += "\t\\statemeasure{Mean tokens on place " + 
                 places[i].getId() + "}{\n";
         modString += "\t\t\\estimator{mean variance distribution}\n";
         modString += "\t\t\\expression{" + places[i].getId() + "}\n";
         modString += "\t}\n";
      }
   }

   
   private void transitionMeasures() {
      for(int i=0; i<transitions.length; i++) {
         modString += "\t\\statemeasure{Enabled probability for transition " + 
                 transitions[i].getId() + "}{\n";
         modString += "\t\t\\estimator{mean}\n";
         modString += "\t\t\\expression{(" + getTransitionConditions(i) +
                 ") ? 1 : 0}\n";
         modString += "\t}\n";
         
         modString += "\t\\countmeasure{Throughput for transition " + 
                 transitions[i].getId() + "}{\n";
         modString += "\t\t\\estimator{mean}\n";
         modString += "\t\t\\precondition{1}\n";
         modString += "\t\t\\postcondition{1}\n";
         modString += "\t\t\\transition{" + transitions[i].getId() + "}\n";
         modString += "\t}\n";
      }
   }
   
   
   private void passageTime() {
      modString += "\\passage{\n";
      modString += "\t\\targetcondition{" + jtfTargetCondition.getText() + "}\n";
      modString += "\t\\sourcecondition{" + jtfSourceCondition.getText() + "}\n";
      modString += "\t\\t_start{" + jtfTStart.getText() + "}\n";
      modString += "\t\\t_stop{" + jtfTStop.getText() + "}\n";
      modString += "\t\\t_step{" + jtfTStep.getText() + "}\n";
      modString += "}\n";
   }
   
   
   private String getTransitionConditions(int transitionNum) {
      String condition = new String();
      Iterator arcsTo = transitions[transitionNum]. getConnectToIterator();
      if (arcsTo.hasNext()){
         condition += ((Arc)arcsTo.next()).getSource().getId()+" > 0";
      }
      while (arcsTo.hasNext()){
         condition += " && "+((Arc)arcsTo.next()).getSource().getId()+" > 0";
      }
      return condition;
   }
   
   
   private boolean hasTimed() {
      for(int i=0; i<transitions.length; i++) {
         if (transitions[i].isTimed()){
            return true;
         }
      }
      return false;
   }
   
   
   private boolean getUrtaPath() {
      Runtime r = Runtime.getRuntime();
      try {
         Process p = r.exec("urta");
         p.waitFor();
      } catch (Exception e) {
         return false;
      }
      try {
         Process p = r.exec("which urta");
         p.waitFor();
         InputStreamReader stdout = new InputStreamReader(p.getInputStream());
         int ch=0;
         String currentLine="";
         while (((ch = stdout.read()) != -1) && (ch != 10)) {
            currentLine += (char)ch;
         }
         
         urtaExecutablePath = currentLine.substring(0, currentLine.indexOf("urta"));
      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return true;
   }
   
   
   class DnamacaRun {
      private ProgressMonitor progressMonitor;
      private Timer timer;
      private ExternalTask task;
      
      public DnamacaRun() {
         //Create a timer.
         timer = new Timer(100, new TimerListener());
         progressMonitor = new ProgressMonitor(guiDialog, "Running DNAmaca",
                                               "", 0, urtaStrings.length);
         progressMonitor.setProgress(0);
         progressMonitor.setMillisToDecideToPopup(0);
         progressMonitor.setMillisToPopup(0);
         
         task = new ExternalTask();
         task.go();
         timer.start();
      }
   
      
      class TimerListener implements ActionListener {
         
         public void actionPerformed(ActionEvent evt) {
            if (progressMonitor.isCanceled() || task.done()) {
               progressMonitor.close();
               task.stop();
               timer.stop();
               resultsButton.setEnabled(true);
               
               SwingUtilities.invokeLater(new Runnable() {
                  
                  public void run() {
                     displayResults();
                     resultsGraph.getGraph().setValues(resultsX, resultsY);
                  }
               });
               
            } else {
               progressMonitor.setNote(task.getMessage());
               progressMonitor.setProgress(task.getCurrent());
            }
         }
      }
      
      
      class ExternalTask {
         private int lengthOfTask;
         private int current = 0;
         private String statMessage;
         
         ExternalTask() {
            lengthOfTask = urtaStrings.length-1;
         }
 
         
         void go() {
            current = 0;
            final SwingWorker worker = new SwingWorker() {
               public Object construct() {
                  return new ActualTask();
               }
            };
            worker.start();
         }
         
         
         int getCurrent() {
            return current;
         }
         
         
         void stop() {
            current = lengthOfTask;
         }
         
         
         boolean done() {
            if (current >= lengthOfTask){
               return true;
            } else {
               return false;
            }
         }
         
         
         String getMessage() {
            return statMessage;
         }
         
         
         class ActualTask {
            
            ActualTask() {
               Runtime r = Runtime.getRuntime();
               try {
                  Process p = r.exec(urtaExecutablePath + "urta " + 
                          modFile.getAbsolutePath());
                  InputStreamReader stdout = 
                          new InputStreamReader(p.getInputStream());
                  int ch=0;
                  String currentLine="";
                  while (ch != -1) {
                     while (((ch = stdout.read()) != -1) && (ch != 10)) {
                        currentLine += (char)ch;
                     }
                     for(int i=0; i<urtaStrings.length; i++) {
                        if (currentLine.indexOf(urtaStrings[i])>=0){
                           current = i;
                           statMessage = currentLine;
                        }
                     }
//                     System.out.println(currentLine);
                     currentLine = "";
                  }
                  p = r.exec(urtaExecutablePath + "uniform" + 
                          (jcbCumulative.isSelected()?" -cdf":""));
                  stdout = new InputStreamReader(p.getInputStream());
                  resultsX.clear(); resultsY.clear();
                  ch=0;
                  while (ch != -1) {
                     while (((ch = stdout.read()) != -1) && (ch != 10)) {
                        currentLine += (char)ch;
                     }
                     for(int i=0; i<urtaStrings.length; i++) {
                        if (currentLine.indexOf(urtaStrings[i])>=0){
                           current = i;
                           statMessage = currentLine;
                        }
                     }
                     if ((currentLine.indexOf("DATA0") >= 0) && 
                             (currentLine.indexOf("elapsed") == -1)) {
                        String results[] = currentLine.split("\\s+");
                        resultsX.add(new Double(results[0]));
                        resultsY.add(new Double(results[1]));
                     }
//                     System.out.println(currentLine);
                     currentLine = "";
                  }
               } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         }
      }
   }
   
}
