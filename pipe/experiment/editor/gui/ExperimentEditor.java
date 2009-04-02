/*
 * ExperimentEditor.java
 *
 * Created on 26 / setembre / 2007, 09:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment.editor.gui;

import pipe.gui.CreateGui;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import java.awt.Container;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.GridLayout;
import pipe.gui.action.GuiAction;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.JTextField;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;

import java.util.Vector;


/**
 *
 * @author marc
 */
public class ExperimentEditor extends javax.swing.JPanel implements ActionListener {
    private String variableTypes[] = 
       {"LocalVariable", "GlobalVariable", "OutputVariable"};
    private Vector<String> places;
    private Vector<String> transitions;
    private Vector<VariableEditorPanel> variables;
    private JDialog experimentDialog;
    private JPanel experimentPanel;
    private JPanel variablePanel;
    private JPanel solutionSpecPanel;
    private JScrollPane sPane;
    private JMenuBar menuBar;
    private JMenu newMenu;
    private GridLayout experimentLayout;
    private GridLayout variableLayout;
    private GridLayout solutionSpecLayout;
    private int postion=10;
    /** Creates a new instance of ExperimentEditor */
    public ExperimentEditor(DataLayer sourceDataLayer) {
        menuBar=new JMenuBar();
        newMenu = new JMenu("New");
        places = new Vector<String>();
        transitions = new Vector<String>();
        variables = new Vector<VariableEditorPanel>();
        for(int i=0; i<sourceDataLayer.getPlacesCount(); i++){
            places.add(sourceDataLayer.getPlace(i).getName());
        }
        for(int i=0; i<sourceDataLayer.getTransitionsCount(); i++){
            transitions.add(sourceDataLayer.getTransition(i).getName());
        }
        start();
    }
    
    public Vector<String> getVariables(){
        Vector<String> result = new Vector<String>();
        for(int i =0; i < variables.size(); i++){
            String text = ((JTextField) variables.get(i).getComponent(0)).getText();
            if (text.length()>0){
                result.add(text);
                System.out.println(result.get(i));
            }
        }
        return result;
    }
    
    public void start(){
        experimentDialog = new JDialog(CreateGui.getApp(),
                "Experiment Editor", true);
        JMenuItem newVariable = new JMenuItem("Variable");
        JMenuItem newSolutionSpec = new JMenuItem("SolutionSpec");
        newVariable.addActionListener(this);
        newSolutionSpec.addActionListener(this);
        newMenu.add(newVariable);
        newMenu.add(newSolutionSpec);
        menuBar.add(newMenu);
        experimentDialog.setJMenuBar(menuBar);
        experimentPanel = new JPanel();
        variablePanel = new JPanel();
        solutionSpecPanel = new JPanel();
        experimentPanel.add(variablePanel);
        experimentPanel.add(solutionSpecPanel);
        
        Container contentPane=experimentDialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
        experimentPanel.setLayout(new BoxLayout(experimentPanel,BoxLayout.PAGE_AXIS));
        variablePanel.setLayout(new BoxLayout(variablePanel,BoxLayout.PAGE_AXIS));
        solutionSpecPanel.setLayout(new BoxLayout(solutionSpecPanel,BoxLayout.PAGE_AXIS));
        
        /*experimentPanel.setLayout(experimentLayout);
        variablePanel.setLayout(variableLayout);
        solutionSpecPanel.setLayout(solutionSpecLayout);*/
        contentPane.setPreferredSize(new Dimension(850,500));
        //contentPane.setMaximumSize(new Dimension(800,500));
        contentPane.add(experimentPanel);
        sPane=new JScrollPane();

        //sPane.setPreferredSize(new Dimension(500,500));
        experimentDialog.getContentPane().add(sPane);
        sPane.setViewportView(experimentPanel);
        contentPane.add(sPane);
        // 1 Set layout

      experimentDialog.setResizable(true);
      
      // Make window fit contents' preferred size
      experimentDialog.pack();
      
      // Move window to the middle of the screen
      experimentDialog.setLocationRelativeTo(null);
      experimentDialog.pack();
      experimentDialog.setVisible(true);
    }
    
    private void createVariable(Container container){
        //variableLayout.setRows(variableLayout.getRows()+1);
        VariableEditorPanel vep = new VariableEditorPanel(places,transitions);
        variablePanel.add(vep);
        variables.add(vep);
        //sPane.setViewportView(experimentPanel);
    }
    
    private void createSolutionSpec(Container container){
        solutionSpecPanel.add(new SolutionSpecEditorPanel(this));
        //sPane.setViewportView(experimentPanel);
    }
    
     public void actionPerformed(ActionEvent ae) {
        if(ae.getActionCommand().equals("Variable")){
            createVariable(variablePanel);
            experimentDialog.pack();
        }else if(ae.getActionCommand().equals("SolutionSpec")){
            createSolutionSpec(solutionSpecPanel);
            experimentDialog.pack();
        }
    }
     
    public void removeSolutionSpec(SolutionSpecEditorPanel ss){
        solutionSpecPanel.remove(ss);
        experimentDialog.pack();
    }

    
    
}
