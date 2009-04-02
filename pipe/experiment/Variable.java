/*
 * Variable.java
 *
 * Created on 24 / juliol / 2007, 11:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment;

/**
 * This class represents the LocalVariable element from the experiment schema. GlobalVariable
 * and OuputVariable extend this class.
 *
 * @author marc
 */
public class Variable {
    private String name;
    private double value;
    
    /** Creates a new instance of Variable
     * @param name name of the variable
     */
    
    public Variable(String name) {
        this.name = name;
        value = 0.0;
    }
    
    /** Sets a new value to this variable
     */
    public void setValue(double value){
        this.value = value;
    }
    
    /** Returns the current value of this variable
     */
    public double getValue(){
        return value;
    }
    
    /** Returns the name of the variable
     */
    public String getName(){
        return name;
    }
    
}
