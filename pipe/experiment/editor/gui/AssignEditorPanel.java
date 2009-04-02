/*
 * AssignEditorPanel.java
 *
 * Created on 10 / octubre / 2007, 11:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment.editor.gui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.util.Vector;

/**
 *
 * @author marc
 */
public class AssignEditorPanel extends javax.swing.JPanel implements ActionListener {
    private JComboBox variables;
    private JTextField expression;
    private Vector<String> variablesList;
    private JButton delete;
    private SolutionSpecDialog parent;
    /** Creates a new instance of AssignEditorPanel */
    public AssignEditorPanel(Vector<String> variables, SolutionSpecDialog ssd) {
        if (variables.size() > 0){
            parent=ssd;
            variablesList=variables;
            initComponents();
            paint();
        }else{
            JOptionPane.showMessageDialog(this, "There are no declared variables.","Experiment Editor Error", 
                        JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initComponents() {
        delete = new JButton("Delete");
        delete.addActionListener(this);
        expression = new javax.swing.JTextField(15);
        variables = new javax.swing.JComboBox();
        variables.setModel(new javax.swing.DefaultComboBoxModel(variablesList));
        

    }
    
    private void paint(){
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.removeAll();
                this.setLayout(layout);
                layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addGap(24, 24, 24)
                    .addComponent(variables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(24, 24, 24)
                    .addComponent(expression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(24, 24, 24)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(111, Short.MAX_VALUE))
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addGap(24, 24, 24)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(variables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent ae){
        if (ae.getSource() instanceof JButton){
            if (((JButton) ae.getSource()).getText().equals("Delete")){
                parent.removeAssign(this);
            }
        }
        
    }
}
