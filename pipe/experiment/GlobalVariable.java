/*
 * GlobalVariable.java
 *
 * Created on 24 / juliol / 2007, 12:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment;

/**
 *
 * @author marc
 */
public class GlobalVariable extends Variable {
    
    String nodeName;
    String attributeToChange;
    
    /**
     * Creates a new instance of GlobalVariable
     * @param name name of the variable
     * @param nodeName node which this variable refers to
     * @param attributeToChange attribute which this variable refers to
     */
    public GlobalVariable(String name, String nodeName, String attributeToChange) {
        super(name);
        this.nodeName = nodeName;
        this.attributeToChange = attributeToChange;
    }
    
    /**
     * Returns the name of the target node.
     */
    public String getNodeName(){
        return nodeName;
    }
    
    /**
     * Returns the name of the attribute which this variable refers to.
     */
    public String getAttributeName(){
        return attributeToChange;
    }
    
}
