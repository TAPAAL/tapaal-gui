/*
 * Created on 19-Jul-2005
 */
package pipe.dataLayer.calculations;


/**
 * @author Nadeem
 * Thrown when a timeless trap is detected during state space exploration.
 */
public class TimelessTrapException 
        extends Exception {
   
   
   public TimelessTrapException(){
      super("Timeless Trap Detected. Analysis is impossible.");
   }
   
}
