/*
 * Created on 23-Jul-2005
 */
package pipe.dataLayer.calculations;


/**
 * @author Nadeem
 * Used by StateSpaceGenerator class to represent an explored state during 
 * state space generation. Uses less memory than storing the entire state on the
 * explored states hashtable.
 */
public class CompressedState {
   
   private int hashCode2;
   private int idnum;
   
   
   public CompressedState(int hc, int id){
      hashCode2 = hc;
      idnum = id;
   }
   
   
   public int getHashCode2(){
      return hashCode2;
   }
   
   
   public int getID(){
      return idnum;
   }
   
}
