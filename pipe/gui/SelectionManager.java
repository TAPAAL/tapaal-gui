/*
 * Created on 08-Feb-2004
 */
package pipe.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import pipe.dataLayer.Arc;
import pipe.dataLayer.ArcPath;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.PlaceTransitionObject;


/**
 * @author Peter Kyme, Michael Camacho
 * Class to handle selection rectangle functionality
 */
public class SelectionManager 
        extends javax.swing.JComponent
        implements java.awt.event.MouseListener, 
                   java.awt.event.MouseWheelListener,
                   java.awt.event.MouseMotionListener {

   private Point startPoint;
   private Rectangle selectionRectangle = new Rectangle(-1,-1);
   private boolean isSelecting;
   private static final Color selectionColor = new Color(000, 000, 255, 030);
   private static final Color selectionColorOutline = new Color(000, 000, 100);
   private GuiView view;
   private boolean enabled = true;

   
   public SelectionManager(GuiView _view) {
      addMouseListener(this);
      addMouseMotionListener(this);
      addMouseWheelListener(this);
      this.view = _view;
   }
   
   
   public void updateBounds() {
      if (enabled == true) {
         setBounds(0,0,view.getWidth(),view.getHeight());
      }
   }

   
   public void enableSelection() {
      if (enabled == false) {
         view.add(this);
         enabled = true;
         updateBounds();
      }
   }

   
   public void disableSelection() {
       if (enabled == true) {
         view.remove(this);
         enabled = false;
      }
   }

   
   private void processSelection(MouseEvent e) {
      if (!e.isShiftDown()){
         clearSelection();
      }
      
      // Get all the objects in the current window
      ArrayList <PetriNetObject> pnObjects = view.getPNObjects();
      for (PetriNetObject pnObject : pnObjects) {
         pnObject.select(selectionRectangle);
      }      
   }

   
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(selectionColor);
      g2d.fill(selectionRectangle);
      g2d.setPaint(selectionColorOutline);
      g2d.draw(selectionRectangle);
   }

   
   public void deleteSelection() {
      // Get all the objects in the current window
      ArrayList <PetriNetObject> pnObjects = view.getPNObjects();
      for (int i = 0; i < pnObjects.size(); i++) {
         if (pnObjects.get(i).isSelected()) {
            pnObjects.get(i).delete();
         }         
      }
      view.updatePreferredSize();
   }

   
   public void clearSelection() {
      // Get all the objects in the current window
      ArrayList <PetriNetObject> pnObjects = view.getPNObjects();
      for (PetriNetObject pnObject : pnObjects) {    
         if (pnObject.isSelectable()) {
            pnObject.deselect();
         }
      }
   }

   
   public void translateSelection(int transX, int transY) {
      
      if (transX == 0 && transY == 0) {
         return;
      }

      // First see if translation will put anything at a negative location
      Point point = null;
      Point topleft = null;

      // Get all the objects in the current window
      ArrayList <PetriNetObject> pnObjects = view.getPNObjects();
      for (PetriNetObject pnObject : pnObjects) {
         if (pnObject.isSelected()){
            point = pnObject.getLocation();
            if (topleft == null) {
               topleft = point;
            } else {
               if (point.x < topleft.x) {
                  topleft.x = point.x;
               }
               if (point.y < topleft.y) {
                  topleft.y = point.y;
               }
            }
         }
      }
      
      if (topleft != null) {
         topleft.translate(transX, transY);
         if (topleft.x < 0){
            transX -= topleft.x;
         }
         if (topleft.y < 0){
            transY -= topleft.y;
         }
         if (transX == 0 && transY == 0){
            return;
         }
      }
      
      for (PetriNetObject pnObject : pnObjects) {  
         if (pnObject.isSelected()) {
            pnObject.translate(transX, transY);
         }
      }
      view.updatePreferredSize();
   }

   
   public ArrayList getSelection() {
      ArrayList selection = new ArrayList();

      // Get all the objects in the current window
      ArrayList <PetriNetObject> pnObjects = view.getPNObjects();
      for (PetriNetObject pnObject : pnObjects) {      
         if (pnObject.isSelected()){
            selection.add(pnObject);
         }
      }
      return selection;
   }

   
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
    */
   public void mousePressed(MouseEvent e) {
      if (e.getButton() == e.BUTTON1 && !(e.isControlDown())) {
         isSelecting = true;
         view.setLayer(this, Pipe.SELECTION_LAYER_OFFSET);
         startPoint = e.getPoint();
         selectionRectangle.setRect(startPoint.getX(), startPoint.getY(), 0, 0);
         // Select anything that intersects with the rectangle.
         processSelection(e);
         repaint();
      } else {
         startPoint = e.getPoint();
      }
   }

   
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
    */
   public void mouseReleased(MouseEvent e) {
      if (isSelecting == true) {
         // Select anything that intersects with the rectangle.
         processSelection(e);
         isSelecting = false;
         view.setLayer(this, Pipe.LOWEST_LAYER_OFFSET);
         selectionRectangle.setRect(-1, -1, 0, 0);
         repaint();
      }
   }

   
   /* (non-Javadoc)
    * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
    */
   public void mouseDragged(MouseEvent e) {
      if (isSelecting) {
         selectionRectangle.setSize(
                  (int)Math.abs(e.getX() - startPoint.getX()),
                  (int)Math.abs(e.getY() - startPoint.getY()));
         selectionRectangle.setLocation(
                  (int)Math.min(startPoint.getX(), e.getX()),
                  (int)Math.min(startPoint.getY(), e.getY()));
         // Select anything that intersects with the rectangle.
         processSelection(e);
         repaint();
      } else {   
         view.drag(startPoint, e.getPoint());
      }
   }


   public void mouseWheelMoved(MouseWheelEvent e) {
      if (e.isControlDown()) {
         if (e.getWheelRotation()> 0) {
            view.zoomIn();
         } else {
            view.zoomOut();
         }
      }
   }   
   
   public void mouseClicked(MouseEvent e) {
      ;// Not needed
   }

   
   public void mouseEntered(MouseEvent e) {
      ; // Not needed
   }

   
   public void mouseExited(MouseEvent e) {
      ; // Not needed
   }

   
   /* (non-Javadoc)
    * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
    */
   public void mouseMoved(MouseEvent e) {
      ; // Not needed
   }   
   
   
   public int getSelectionCount() {
      Component netObj[] = view.getComponents();
      int selectionCount = 0;
      // Get all the objects in the current window
      for (int i=0; i< netObj.length; i++) {
         // Handle Arcs and Arc Points
         if ((netObj[i] instanceof Arc) && ((PetriNetObject)netObj[i]).isSelectable())	{
            Arc thisArc = (Arc)netObj[i];
            ArcPath thisArcPath = thisArc.getArcPath();
            for (int j=1; j < thisArcPath.getEndIndex(); j++){
               if (thisArcPath.isPointSelected(j)) {
                  selectionCount++;
               }
            }
         }
         
         // Handle PlaceTransition Objects
         if ((netObj[i] instanceof PlaceTransitionObject) && 
                 ((PetriNetObject)netObj[i]).isSelectable()) {
            if (((PlaceTransitionObject)netObj[i]).isSelected()) {
               selectionCount++;
            }
         }
      }
      return selectionCount;
   }

}
