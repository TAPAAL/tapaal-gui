/*
 * InvalidExpressionException.java
 *
 * Created on 17 / agost / 2007, 12:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment;

/**
 *
 * @author marc
 */
public class InvalidExpressionException extends Exception {
    private String message;
    /** Creates a new instance of InvalidExpressionException */
    public InvalidExpressionException(String message) {
        this.message = message;
    }
    
    public String getMessage(){
        return message;
    }
    
}
