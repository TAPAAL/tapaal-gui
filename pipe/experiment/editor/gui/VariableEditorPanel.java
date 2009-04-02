/*
 * ExperimentEditorPanel2.java
 *
 * Created on 26 / setembre / 2007, 10:12
 */

package pipe.experiment.editor.gui;

import java.util.Vector;
import java.awt.event.ItemEvent;

/**
 *
 * @author  marc
 */
public class VariableEditorPanel extends javax.swing.JPanel implements java.awt.event.ItemListener {
    
    /**
     * Creates new form VariableEditorPanel
     */
    public VariableEditorPanel(Vector<String> places, Vector<String> transitions) {
        this.places=places;
        this.transitions=transitions;
        initComponents();
    }
                         
    private void initComponents() {
        jtext = new javax.swing.JTextField(8);
        variableType = new javax.swing.JComboBox();
        nodesCB = new javax.swing.JComboBox();
        placeTransition = new javax.swing.JComboBox();
        placeTransition.addItemListener(this);
        attributeToChange = new javax.swing.JComboBox();
        resultToUseCB=new javax.swing.JComboBox();
        jComboBox4 = new javax.swing.JComboBox();
        jComboBox5 = new javax.swing.JComboBox();
        variableType.addItemListener(this);
        initialValue = new javax.swing.JTextField(8);

        variableType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "LocalVariable", "GlobalVariable", "OutputVariable"}));
        placeTransition.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Place", "Transition"}));
        nodesCB.setModel(new javax.swing.DefaultComboBoxModel(places));
        attributeToChange.setModel(new javax.swing.DefaultComboBoxModel(placeAttributes));
        resultToUseCB.setModel(new javax.swing.DefaultComboBoxModel(placeResultToUse));

        localVariableIGU();
    }
    
    public void itemStateChanged(ItemEvent ie){
        if(ie.getStateChange() == ItemEvent.SELECTED){       
            System.out.println(((javax.swing.JComboBox)ie.getSource()).getSelectedItem());
            String selected = ((String)((javax.swing.JComboBox)ie.getSource()).getSelectedItem());
            if (selected.equals("LocalVariable")){
                localVariableIGU();
            }else if (selected.equals("GlobalVariable")){
                globalVariableIGU(PLACE);
            }else if (selected.equals("OutputVariable")){
                outputVariableIGU(PLACE);
            }else if(selected.equals("Place")){
                if (((String)variableType.getSelectedItem()).equals("GlobalVariable")){
                    globalVariableIGU(PLACE);
                }else{
                    outputVariableIGU(PLACE);
                }
            }else if(selected.equals("Transition")){
                if (((String)variableType.getSelectedItem()).equals("GlobalVariable")){
                    globalVariableIGU(TRANSITION);
                }else{
                    outputVariableIGU(TRANSITION);
                }
            }
        }
    }
    
    private void localVariableIGU(){
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.removeAll();
                this.setLayout(layout);
                layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addGap(24, 24, 24)
                    .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(24, 24, 24)
                    .addComponent(variableType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(24, 24, 24)
                    .addComponent(initialValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(111, Short.MAX_VALUE))
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addGap(24, 24, 24)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(variableType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(initialValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
    }
    
    private void globalVariableIGU(int type){
        if (type==PLACE){
            placeTransition.setSelectedIndex(0);
            attributeToChange.setModel(new javax.swing.DefaultComboBoxModel(placeAttributes));
            nodesCB.setModel(new javax.swing.DefaultComboBoxModel(places));
        }else if (type==TRANSITION){
            placeTransition.setSelectedIndex(1);
            attributeToChange.setModel(new javax.swing.DefaultComboBoxModel(transitionAttributes));
            nodesCB.setModel(new javax.swing.DefaultComboBoxModel(transitions));
        }
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.removeAll();
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(variableType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(placeTransition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(nodesCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(attributeToChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(111, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(variableType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(placeTransition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodesCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attributeToChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }
    
    private void outputVariableIGU(int type){
        if(type==PLACE){
            placeTransition.setSelectedIndex(0);
            resultToUseCB.setModel(new javax.swing.DefaultComboBoxModel(placeResultToUse));      
            nodesCB.setModel(new javax.swing.DefaultComboBoxModel(places));
        }else{
            placeTransition.setSelectedIndex(1);
            resultToUseCB.setModel(new javax.swing.DefaultComboBoxModel(transitionResultToUse));
            nodesCB.setModel(new javax.swing.DefaultComboBoxModel(transitions));
        }
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.removeAll();
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(variableType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(27, 27, 27)
                .addComponent(placeTransition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(nodesCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(resultToUseCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(111, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(variableType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(placeTransition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodesCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resultToUseCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }
    
    
    // Variables declaration - do not modify                     
    private javax.swing.JComboBox variableType;
    private javax.swing.JComboBox nodesCB;
    private javax.swing.JComboBox placeTransition;
    private javax.swing.JComboBox attributeToChange;
    private javax.swing.JComboBox resultToUseCB;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JTextField jtext;
    private javax.swing.JTextField initialValue;
    
    private String[] placeAttributes = new String[] { "Initial Marking", "Capacity" };
    private String[] transitionAttributes = new String[] { "Priority", "Rate", "Weight" };
    private String[] placeResultToUse = new String[] { "Utilization", "Average Tokens"};
    private String[] transitionResultToUse = new String[] { "Throughput"};
    
    private static final int PLACE = 0;
    private static final int TRANSITION = 1;
    
    private Vector<String> places;
    private Vector<String> transitions;
    // End of variables declaration                   
    
}
