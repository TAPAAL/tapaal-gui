/*
 * Created on Mar 11, 2004
 *
 */
package pipe.dataLayer.calculations;

import pipe.gui.Pipe;


/**
 * @author Matthew
 */
public class TreeTooBigException 
        extends Exception {
   
   
   TreeTooBigException() {
      super("The state-space tree for this net has more than " + Pipe.MAX_NODES +
              "nodes DNAMACA might be a more appropriate tool for this analysis");
   }
   
}
