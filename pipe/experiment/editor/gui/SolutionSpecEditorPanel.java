/*
 * SolutionSpecEditorPanel.java
 *
 * Created on 3 / octubre / 2007, 10:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment.editor.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JButton;
/**
 *
 * @author marc
 */
public class SolutionSpecEditorPanel extends javax.swing.JPanel implements ActionListener {
    private ExperimentEditor ee;
    
    /**
     * Creates new form SolutionSpecEditorPanel
     */
    public SolutionSpecEditorPanel(ExperimentEditor ee) {
        this.ee=ee;
        initComponents();
    }
                         
    private void initComponents() {
        JLabel jtext = new JLabel("Solution Specification");
        JButton edit = new JButton ("Edit");
        JButton delete = new JButton ("Delete");
        edit.addActionListener(this);
        delete.addActionListener(this);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.removeAll();
                this.setLayout(layout);
                layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(10, 10, 10)
                    .addComponent(edit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(10, 10, 10)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(111, Short.MAX_VALUE))
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(edit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
    }
    
    public void itemStateChanged(ItemEvent ie){
        
    }
    
    public void actionPerformed(ActionEvent ae){
        System.out.println(((JButton) ae.getSource()).getText());
        
        if ( ((JButton) ae.getSource()).getText().equals("Edit") ){
            new SolutionSpecDialog(ee);
        }
        else if ( ((JButton) ae.getSource()).getText().equals("Delete") ){
            ee.removeSolutionSpec(this);
        }
    }
    
    
}
