/*
 * OutputVariable.java
 *
 * Created on 20 / agost / 2007, 10:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment;

/**
 * This class represents the OutputVariable element from the experiment schema.
 * @author marc
 */

public class OutputVariable extends Variable {
    public static int PLACE = 0;
    public static int TRANSITION = 1;
    
    private String resultToUse;
    private String nodeName;
    private int nodeType;
    private double initialValue;
    
    /** Creates a new instance of OutputVariable.
     * @param name name of the variable.
     * @param resultToUse result linked to this variable.
     * @param nodeName node which resultToUse belongs to.
     * @param nodeType a node can be a place or a transition. Use the static integers
     * PLACE and TRANSITION.
     * @param initialValue variable initial value.
     */
    public OutputVariable(String name, String resultToUse, String nodeName, int nodeType, double initialValue) {
        super(name);
        this.setValue(initialValue);
        this.resultToUse = resultToUse;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
    }
    
    /** It returns the type of the node linked to this variable.
     * @return PLACE or TRANSITION
     */
    public int getNodeType(){
        return nodeType;
    }
    
    /** It returns the name of the node linked to this variable.
     */
    public String getNodeName(){
        return nodeName;
    }
    
    /** It returns the result which this variable refers to.
     */
    public String getResultToUse(){
        return resultToUse;
    }
    
}
