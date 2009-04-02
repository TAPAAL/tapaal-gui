package pipe.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;

import pipe.dataLayer.calculations.Marking;

/**
 * Implementation of the StateRecord class using java.nio.* classes for improved speed
 *
 * @author Oliver Haggary - 08/2007 (Ideas taken from Nadeem Akharware)
 *
 */
public class NewStateRecord {
   private int stateid;
   private int[] state = null;
   private char isTangible;
   
   public NewStateRecord(){
      ;
   }
   
   
   /**
    * Creates NewStateRecord with same contents as specified state
    * @param newstate
    */
   public NewStateRecord(Marking newstate){
      stateid = newstate.getIDNum();
      state = new int[newstate.getState().length];
      System.arraycopy(newstate.getState(), 0, state, 0, newstate.getState().length);
      
      //necessary as there is no boolean view of a bytebuffer
      if (newstate.getIsTangible()) {
         isTangible = 'T';
      } else {
         isTangible = 'V';
      }
   }
   
   
   /**
    * writes contents of NewStateRecord to specified output buffer
    * @param opBuf
    * @throws IOException
    */
   public void write(MappedByteBuffer opBuf) throws IOException{
      if (state == null) {
         return;
      }
      opBuf.putInt(stateid);
      for (int index = 0; index < state.length; index++) {
         opBuf.putInt(state[index]);
      }
      opBuf.putChar(isTangible);
   }
   
   
   /**
    * Reads contents of NewStates record from specified input buffer
    * @param statesize
    * @param ipfile
    * @throws IOException
    */
   public void read(int statesize, MappedByteBuffer ipfile) throws IOException{
      state = new int[statesize];
      stateid = ipfile.getInt();
      for (int index = 0; index < state.length; index++) {
         state[index] = ipfile.getInt();
      }
      isTangible=ipfile.getChar();
   }
   
   
   public int[] getState(){
      return state;
   }
   
   
   public int getID(){
      return stateid;
   }
   
   
   public boolean getTangible(){
      return isTangible == 'T';
   }
   
}
