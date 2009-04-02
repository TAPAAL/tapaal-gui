/*
 * IterationVector.java
 *
 * Created on 19 / setembre / 2007, 10:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment;

import java.util.Vector;

/**
 *
 * @author marc
 */
public class IterationVector extends Vector<Double> {
    private String variableName;
    private int position;
    
    /** Creates a new instance of IterationVector */
    public IterationVector(String variableName,String content) {
        this.variableName=variableName;
        String[] components = content.split(",");
        for(int i = 0; i < components.length; i++){
            this.add(Double.parseDouble(components[i]));
        }
    }
    
    public void setIndex(int idx){
        position=idx;
    }
    
    public int getIndex(){
        return position;
    }
    
    public String getVariableName(){
        return variableName;
    }
    
}
