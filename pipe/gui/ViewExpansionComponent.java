package pipe.gui;

import pipe.dataLayer.PetriNetObject;

/**
 * @author unknown
 */
public class ViewExpansionComponent 
        extends PetriNetObject {

   private int originalX = 0;
   private int originalY = 0;
   
   
   public ViewExpansionComponent() {
      super();
   }
   
  
   public ViewExpansionComponent(int x, int y){
      this();
      originalX = x;
      originalY = y;
      setLocation(x,y);
   }
   

   public void zoomUpdate(int zoom) {
      double scaleFactor = Zoomer.getScaleFactor(zoom);
      setLocation((int)(originalX * scaleFactor),(int)(originalY * scaleFactor));
   }   

   
   public void addedToGui() {
      ;
   }

   
   public PetriNetObject copy() {
      return null;
   }

   
   public PetriNetObject paste(double despX, double despY, boolean inAnotherView) {
      return null;
   }

   
   public int getLayerOffset() {
      return 0;
   }

   
   public void translate(int x, int y) {
      ;
   }

}
