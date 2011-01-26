package pipe.gui.handler;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import pipe.dataLayer.NameLabel;
import pipe.dataLayer.PlaceTransitionObject;


public class LabelHandler 
        extends javax.swing.event.MouseInputAdapter 
        implements java.awt.event.MouseWheelListener {
   
   private PlaceTransitionObject obj;
   
   private NameLabel nl;
   
   protected Point dragInit = new Point();
   
   
   public LabelHandler(NameLabel _nl, PlaceTransitionObject _obj) {
      obj = _obj;
      nl = _nl;
   }
   
   
   @Override
public void mouseClicked(MouseEvent e) {
      obj.dispatchEvent(e);
   }
   
   
   @Override
public void mousePressed(MouseEvent e) {
      dragInit = e.getPoint(); //
      //dragInit = e.getLocationOnScreen();  //causes exception in Windows!
      dragInit = javax.swing.SwingUtilities.convertPoint(nl, dragInit, obj);
   }
  

   @Override
public void mouseDragged(MouseEvent e){
      // 
      if (!SwingUtilities.isLeftMouseButton(e)){
         return;
      }
      
      Point p = javax.swing.SwingUtilities.convertPoint(nl, e.getPoint(),obj);
      //obj.setNameOffsetX((e.getXOnScreen() - dragInit.x)); //causes exception in Windows!
      //obj.setNameOffsetY((e.getYOnScreen() - dragInit.y)); //causes exception in Windows!
      //dragInit = e.getLocationOnScreen(); //causes exception in Windows!
      obj.setNameOffsetX((p.x - dragInit.x));
      obj.setNameOffsetY((p.y - dragInit.y));
      dragInit = p;
      obj.update();
   }   
   
   @Override
public void mouseWheelMoved(MouseWheelEvent e) {
      obj.dispatchEvent(e);
   }
   
}
