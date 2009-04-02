/**
 * Module interface which all modules should implement.
 * @author Camilla Clifford
 * @version 2003/03/22
 */
package pipe.modules;

import pipe.dataLayer.DataLayer;


public interface Module {
   
   /* Display name for the module */
   public String getName();
   
   
   public void run(DataLayer pnmldata);
   
}
