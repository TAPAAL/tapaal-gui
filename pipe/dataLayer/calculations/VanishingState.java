/*
 * Created on 01-Jul-2005
 */
package pipe.dataLayer.calculations;


/**
 * @author Nadeem
 * This class is used for recording vanishing states while generating the state 
 * space of a GSPN
 */
public class VanishingState 
        extends State {
   
   double rate;

   
   VanishingState(int[] newState, double initialRate){
      super(newState);
      setRate(initialRate);
   }
   
   
   VanishingState(State newState, double initialRate){
      super(newState);
      setRate(initialRate);
   }
   
   
   public void setRate(double newRate){
      rate = newRate;
   }
   
   
   public double getRate(){
      return rate;
   }
   
}
