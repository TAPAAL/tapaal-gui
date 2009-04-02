package pipe.gui;

import java.awt.geom.AffineTransform;


/**
 * @author Tim Kimber
 * @author Pere Bonet - Minor changes
 */
public class Zoomer {

   private int percent;

   private GuiFrame app;
   
   public Zoomer(GuiFrame _app){
      this(100, _app);
   }
   
   
   public Zoomer(int pct, GuiFrame _app){
      percent = pct;
      app = _app;
   }
   
   
   public boolean zoomOut() {
      percent -= Pipe.ZOOM_DELTA;
      if (percent < Pipe.ZOOM_MIN) {
         percent += Pipe.ZOOM_DELTA;
         return false;
      } else {
         return true;
      }
   }
   
   
   public boolean zoomIn(){
      percent += Pipe.ZOOM_DELTA;
      if (percent > Pipe.ZOOM_MAX) {
         percent -= Pipe.ZOOM_DELTA;
         return false;
      } else {
         return true;
      }
   }

   
   public int getPercent() {
      return percent;
   }

   
   private void setPercent(int newPercent) {
      if ((newPercent >= Pipe.ZOOM_MIN) && (newPercent <= Pipe.ZOOM_MAX)) {
         percent=newPercent;
      }
   }

   
   public void setZoom(int newPercent) {
      setPercent(newPercent);
   }


   public static int getZoomedValue(int x, int zoom) {
      return (int)(x * zoom * 0.01);
   }
   
   
   public static float getZoomedValue(float x, int zoom) {
      return (float)(x * zoom * 0.01);
   }
   
   
   public static double getZoomedValue(double x, int zoom) {
      return (x * zoom * 0.01);
   }   
   
   
   public static AffineTransform getTransform(int zoom){
      return AffineTransform.getScaleInstance(zoom * 0.01, zoom * 0.01);
   }
   
   
   public static double getScaleFactor(int zoom) {
      return zoom * 0.01;
   }
   
   
   public static int getUnzoomedValue (int x, int zoom) {
      return (int)(x / (zoom * 0.01));
   }

   
   public static double getUnzoomedValue(double x, int zoom) {
      return (x / (zoom * 0.01));
   }

}
