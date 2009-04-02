/*
 * VariableNotInitializedException.java
 *
 * Created on 2 / agost / 2007, 11:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package expressions;

/**
 *
 * @author marc
 */
public class VariableNotInitializedException extends Exception {
    
    /**
     * Name of the variable which caused this exception to be thrown.
     */
    public String varName;
    
    /**
     * Creates a new instance of VariableNotInitializedException
     * @param varName name of the variable which has not been initialized.
     */
    public VariableNotInitializedException(String varName){
        super();
        this.varName = varName;
    }
    
}
