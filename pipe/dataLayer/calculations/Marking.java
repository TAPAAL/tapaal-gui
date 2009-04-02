/*
 * Created on Mar 2, 2004
 */
package pipe.dataLayer.calculations;


/**
 * @author unespecified
 *
 * @author Nadeem
 * This class modified to make use of the new State class (01/07/2005)
 *
 * @author Matthew Worthington/Edwin Chung added a new attribute to include the 
 * information whether the state is tangible or vanishing. Appropriate 
 * constructors and methods are added/modified to check the equivalance of 
 * two markings.
 */
public class Marking 
        extends State {
   
   private int idnum;
   public static boolean isTangible;
   
   
   public Marking(State markingInput, int idInput) {
      super(markingInput);
      idnum = idInput;
   }
   
   public Marking(State markingInput, int idInput, boolean Tangible){
      super(markingInput);
      idnum = idInput;
      isTangible = Tangible;
   }
   
   
   public Marking(int[] markingInput, int idInput){
      super(markingInput);
      idnum = idInput;
   }
   
   
   public Marking(int[] markingInput, String idInput) {
      super(markingInput);
   }
   
   
   public int[] getMarking(){
      return getState();
   }
   
   
   public String getID(){
      return "M" + idnum;
   }
   
   
   public int getIDNum(){
      return idnum;
   }
   

   public boolean getIsTangible(){
      return isTangible;
   }
   
   
   public boolean equals (Marking m1){
      return (this.equals((State)m1) && (this.isTangible == m1.isTangible));
   }   
   
}
