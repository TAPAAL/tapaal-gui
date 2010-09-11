/*
 * CopyPasteManager.java
 */
package pipe.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import pipe.dataLayer.Arc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.dataLayer.TAPNTransition;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.Transition;
import pipe.gui.undo.UndoManager;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.undo.AddPetriNetObjectEdit;


/**
 * Class to handle paste & paste functionality
 * @author Pere Bonet
 */
public class CopyPasteManager 
        extends javax.swing.JComponent 
        implements pipe.gui.Zoomable, java.awt.event.MouseListener, 
                   java.awt.event.MouseMotionListener, 
                   java.awt.event.KeyListener {

   private static final Color PASTE_COLOR = new Color(155,155,155,100);
   private static final Color PASTE_COLOR_OUTLINE = new Color(155,0,0,0);
   
   private Rectangle pasteRectangle = new Rectangle(-1,-1);   
   
   // pasteInProgres is true when pasteRectangle is visible (user is doing a 
   // paste but still hasn't chosen the position where elements will be pasted).
   private boolean pasteInProgress = false;
   
   private ArrayList <ArrayList> objectsToPaste = new ArrayList();
   
   private Point origin = new Point();
   
   private GuiView sourceView;
   
   private int zoom;


   public CopyPasteManager() {
      addMouseListener(this);
      addMouseMotionListener(this);
      addKeyListener(this);
   }


   private void updateBounds() {
      if (pasteInProgress) {
         setBounds(0, 0, 
                   CreateGui.getView().getWidth(), 
                   CreateGui.getView().getHeight());
      }
   }

   
   public void setUpPaste(ArrayList<PetriNetObject> toCopy, GuiView _sourceView){
      
      sourceView = _sourceView;
      zoom = sourceView.getZoom();
      
      int bottom = 0;
      int right = 0;
      int top = Integer.MAX_VALUE;
      int left = Integer.MAX_VALUE;
      
      ArrayList<Arc> arcsToPaste = new ArrayList();
      ArrayList ptaToPaste = new ArrayList();
      
      for (PetriNetObject pnObject : toCopy) {
         if (pnObject.isCopyPasteable()){
            if (pnObject instanceof Arc) {
               arcsToPaste.add((Arc)pnObject.copy());
               if (pnObject instanceof NormalArc) {
                  if (((NormalArc)pnObject).hasInvisibleInverse()) {
                     arcsToPaste.add(((NormalArc)pnObject).getInverse().copy());
                  }
               }
            } else {
               if (pnObject.getX() < left) {
                  left = pnObject.getX();
               }
               if (pnObject.getX() + pnObject.getWidth() > right) {
                  right = pnObject.getX() + pnObject.getWidth();
               }
               if (pnObject.getY() < top) {
                  top = pnObject.getY();
               } 
               if (pnObject.getY() + pnObject.getHeight() > bottom) {
                  bottom = pnObject.getY() + pnObject.getHeight(); 
               }
               ptaToPaste.add(pnObject.copy());
            }
         }
      }

      if (ptaToPaste.isEmpty() == false) {
         objectsToPaste.clear(); 
         pasteRectangle.setRect(left, top, right - left, bottom - top);
         origin.setLocation(Zoomer.getUnzoomedValue(left, zoom), 
                            Zoomer.getUnzoomedValue(top, zoom));
         objectsToPaste.add(ptaToPaste);
         objectsToPaste.add(arcsToPaste);
      }
   }

   
   public void startPaste(GuiView view) {
      if (!pasteInProgress) {
         view.add(this);
         requestFocusInWindow();
         try {
            if (zoom != view.getZoom()){
               updateSize(pasteRectangle, zoom, view.getZoom());
               zoom = view.getZoom();
            }
            pasteRectangle.setLocation(view.getPointer());
         } catch (java.lang.NullPointerException npe){
            System.out.println(npe);
         }
         view.setLayer(this, Pipe.SELECTION_LAYER_OFFSET);
         repaint();
         pasteInProgress = true;
         updateBounds();
      }
   }

   
   private void clearPaste(GuiView view) {
       if (pasteInProgress) {
         view.remove(this);
         pasteInProgress = false;
         sourceView = null;
         //updateBounds();
      }
   }

   
   private void endPaste(GuiView view){
      ArrayList <UndoableEdit> undo = new ArrayList();
      
      pasteInProgress = false;
      view.remove(this);

      double despX = Grid.getModifiedX(
              Zoomer.getUnzoomedValue(pasteRectangle.getX(), zoom) - origin.getX());
      double despY = Grid.getModifiedY(
              Zoomer.getUnzoomedValue(pasteRectangle.getY(), zoom) - origin.getY());
      
      if (objectsToPaste.isEmpty()) {
         return;
      }
      
      UndoManager undoManager = view.getUndoManager();
      DataLayer model = CreateGui.getModel();
      
      //First, we deal with Places, Transitions & Annotations
      ArrayList <PetriNetObject>ptaToPaste = objectsToPaste.get(0);
      for (int i = 0; i < ptaToPaste.size(); i++) {
         PetriNetObject pnObject = ptaToPaste.get(i).paste(despX, despY, sourceView != view);
         
         if (pnObject != null) {         
            model.addPetriNetObject(pnObject);
            view.addNewPetriNetObject(pnObject);
            view.updatePreferredSize();
            pnObject.select();      
            undo.add(new AddPetriNetObjectEdit(pnObject, view, model));
         }
      }
      
      //Now, we deal with Arcs
      ArrayList <Arc> arcsToPaste = objectsToPaste.get(1);      
      for (int i = 0; i < arcsToPaste.size(); i++) {
         if (!(arcsToPaste.get(i) instanceof Arc)) {
            break;
         }
         Arc arc = (Arc)(arcsToPaste.get(i)).paste(
                 despX, despY, sourceView != view);
         if (arc != null) {
            model.addPetriNetObject(arc);
            view.addNewPetriNetObject(arc);
            view.updatePreferredSize();
            arc.select();
            arc.updateArcPosition();
            undo.add(new AddPetriNetObjectEdit(arc, view, model));
         }
      }
      
      // Now, we find inverse arcs
      ptaToPaste = objectsToPaste.get(0);
      for (PetriNetObject pno : ptaToPaste) {
         if ((pno instanceof PlaceTransitionObject)){
            PlaceTransitionObject pt = 
                    ((PlaceTransitionObject)pno).getOriginal().getLastCopy();

            Iterator <Arc> pnoConnectedFromIterator = 
                    pt.getConnectFromIterator();
            while (pnoConnectedFromIterator.hasNext()) {
               Arc arc1;
               try {
                  arc1 = pnoConnectedFromIterator.next();
               } catch (java.util.ConcurrentModificationException cme) {
                  System.out.println("cme:" + cme);
                  break;
               }
               Iterator <Arc> pnoConnectedToIterator = 
                        pt.getConnectToIterator();
               while (pnoConnectedToIterator.hasNext()) {
                  Arc arc2 = pnoConnectedToIterator.next();
                  
                  if (arc2 instanceof NormalArc) {
                     if (((NormalArc)arc2).hasInverse()){
                        break;
                     }
                  }                  
                  if (arc1.getSource().equals(arc2.getTarget()) && 
                           arc1.getTarget().equals(arc2.getSource())){
                     if (((NormalArc)arc1).isJoined()){
                        ((NormalArc)arc1).setInverse((NormalArc)arc2, true);

                     } else if (((NormalArc)arc2).isJoined()){
                        ((NormalArc)arc2).setInverse((NormalArc)arc1, true);

                     } else {
                        ((NormalArc)arc1).setInverse((NormalArc)arc2, false);
                     }
                  }
               }
            }
         }
      }

      // Clear copies
      ptaToPaste = objectsToPaste.get(0);
      for (PetriNetObject pno : ptaToPaste) {
         if (pno instanceof PlaceTransitionObject) {
            if (((PlaceTransitionObject)pno).getOriginal() != null){
               //the Place/Transition is a copy of another Object, so we have to
               // nullify the reference to the original Object
               ((PlaceTransitionObject)pno).getOriginal().resetLastCopy();
            } else {
               ((PlaceTransitionObject)pno).resetLastCopy();
            }               
         }
      }
      
      // Add undo edits
      undoManager.newEdit(); // new "transaction""
      
      Iterator <UndoableEdit> undoIterator = undo.iterator();
      while (undoIterator.hasNext()){
         undoManager.addEdit(undoIterator.next());
      }
      
      view.zoom(); //
   }

  
   public void cancelPaste() {
      cancelPaste(CreateGui.getView());
   }
  
      
   public void cancelPaste(GuiView view) {         
      pasteInProgress = false;
      view.repaint();
      view.remove(this);   
   }
   
   
   public boolean pasteInProgress() {
      return pasteInProgress;
   }
   
   
   public boolean pasteEnabled() {
      return !objectsToPaste.isEmpty();
   }
   
   
   @Override
public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(PASTE_COLOR);
      //g2d.setXORMode(pasteColor);//Xor doesn't work on windows?
      g2d.fill(pasteRectangle);
      g2d.setXORMode(PASTE_COLOR_OUTLINE);
      g2d.draw(pasteRectangle);
   }
   

   public void zoomUpdate(int newZoom) {
      updateSize(pasteRectangle, zoom, newZoom);
      zoom = newZoom;
   }

   
   private void updateSize(Rectangle pasteRectangle, int zoom, int newZoom) {
      int realWidth = Zoomer.getUnzoomedValue(pasteRectangle.width, zoom);
      int realHeight = Zoomer.getUnzoomedValue(pasteRectangle.height, zoom);
      
      pasteRectangle.setSize((int)(realWidth* Zoomer.getScaleFactor(newZoom)),
                             (int)(realHeight* Zoomer.getScaleFactor(newZoom)));
   }
      
   
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
    */
   public void mousePressed(MouseEvent e) {
      ; // Not needed
   }

   
   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
    */
   public void mouseReleased(MouseEvent e) {
      GuiView view = CreateGui.getView();
      
      view.updatePreferredSize();
      view.setLayer(this, Pipe.LOWEST_LAYER_OFFSET);
      repaint();
      //now, we have the position of the pasted objects so we can show them.
      endPaste(view); 
   }

   
   /* (non-Javadoc)
    * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
    */
   public void mouseDragged(MouseEvent e) {
      if (pasteInProgress){
         pasteRectangle.setLocation(e.getPoint());
         repaint();
         updateBounds();
         //view.updatePreferredSize();
      }
   }

   
   /* (non-Javadoc)
    * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
    */
   public void mouseMoved(MouseEvent e) {
      if (pasteInProgress){
         pasteRectangle.setLocation(e.getPoint());
         //view.updatePreferredSize();
         repaint();
         updateBounds();
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
   
   
   public void keyTyped(KeyEvent e) {
      ; // Not needed
   }

   
   public void keyPressed(KeyEvent e) {
      ; // Not needed
   }

   
   public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
         cancelPaste();
      }
   }

}
