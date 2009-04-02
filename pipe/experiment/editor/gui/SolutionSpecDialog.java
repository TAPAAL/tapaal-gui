/*
 * SolutionSpecDialog.java
 *
 * Created on 9 / octubre / 2007, 16:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment.editor.gui;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.Container;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import pipe.gui.CreateGui;

/**
 *
 * @author marc
 */
public class SolutionSpecDialog extends JDialog implements ActionListener {
    
    private ExperimentEditor editor;
    private JPanel pan;
    
    /** Creates a new instance of SolutionSpecDialog */
    public SolutionSpecDialog(ExperimentEditor ee) {
        super(CreateGui.getApp(),
                "Solution Specification Editor", true);
        editor=ee;
        JMenuBar menuBar = new JMenuBar();
        JMenu newMenu = new JMenu("New");
        JMenuItem newIteration = new JMenuItem("Iteration");
        JMenuItem newAlt = new JMenuItem("Alternation");
        JMenuItem newAssign = new JMenuItem("Assign");
        JMenuItem newSolve = new JMenuItem("Solve");
        newIteration.addActionListener(this);
        newAlt.addActionListener(this);
        newAssign.addActionListener(this);
        newSolve.addActionListener(this);
        newMenu.add(newIteration);
        newMenu.add(newAlt);
        newMenu.add(newAssign);
        newMenu.add(newSolve);
        menuBar.add(newMenu);
        this.setJMenuBar(menuBar);
        pan = new JPanel();
        pan.setLayout(new BoxLayout(pan,BoxLayout.Y_AXIS));
        //pan.setPreferredSize(new Dimension(500,500));
        Container contentPane=this.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
        this.getContentPane().setPreferredSize(new Dimension(500,500));
        this.setLocationRelativeTo(null);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int)dim.getWidth()/2-250,(int)dim.getHeight()/2-250);
        
        JScrollPane sPane=new JScrollPane();

        //sPane.setPreferredSize(new Dimension(500,500));
        this.getContentPane().add(sPane);
        sPane.setViewportView(pan);
        this.add(sPane);
        
        
        this.pack();
        this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent ae){
        if (((JMenuItem) ae.getSource()).getText().equals("Iteration")){
            System.out.println("Iteration");
        }
        else if (((JMenuItem) ae.getSource()).getText().equals("Alternation")){
            System.out.println("Alt");
        }
        else if (((JMenuItem) ae.getSource()).getText().equals("Assign")){
            System.out.println("Assign");
            AssignEditorPanel aep = new AssignEditorPanel(editor.getVariables(),this);
            pan.add(aep);
        }
        else if (((JMenuItem) ae.getSource()).getText().equals("Solve")){
            System.out.println("Solve");
        }
        pack();
    }
    
    protected void removeAssign(AssignEditorPanel aep){
       System.out.println("Esborram");
       pan.remove(aep);
       pan.repaint();
       this.pack();
    }
    
    
}
