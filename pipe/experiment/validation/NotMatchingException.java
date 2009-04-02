/*
 * NotMatchingException.java
 *
 * Created on 10 / agost / 2007, 12:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pipe.experiment.validation;

/**
 *
 * @author marc
 */
public class NotMatchingException extends Exception {
    private String message;
    /**
     * Creates a new instance of NotMatchingException
     */
    public NotMatchingException(String message) {
        this.message = message;
    }
    
    public String getMessage(){
        return message;
    }
    
}
