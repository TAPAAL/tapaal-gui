package pipe.dataLayer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.EventListener;

import javax.swing.JComponent;

import pipe.gui.CopyPasteable;
import pipe.gui.CreateGui;
import pipe.gui.GuiView;
import pipe.gui.Pipe;
import pipe.gui.Translatable;
import pipe.gui.Zoomer;
import pipe.gui.Zoomable;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.undo.PetriNetObjectNameEdit;


/**
 * <b>PetriNetObject</b> - Petri-Net Object Class<b> - <i>Abstract</i></b>
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema (stNet.xsd)</a>
 * @see </p><p><a href="..\..\..\UML\dataLayer.html">UML  -  PNML Package </a></p>
 * @version 1.0
 * @author James D Bloom
 */
public abstract class PetriNetObject 
        extends JComponent 
        implements Zoomable, CopyPasteable, Cloneable, Translatable {

   protected final static int COMPONENT_DRAW_OFFSET = 5;
   
   /** Id */
   protected String id = null;
   
   /** Name Label for displaying name*/
   protected NameLabel pnName;
   protected Color objectColour = Pipe.ELEMENT_LINE_COLOUR;
   protected Color selectionBorderColour = Pipe.SELECTION_LINE_COLOUR;
   protected boolean selected = false;	// True if part of the current selection.
   protected boolean selectable = true;	// True if object can be selected.
   protected boolean draggable = true;	// True if object can be dragged.
   protected boolean copyPasteable  = true;	// True if object can be cloned.
   protected static boolean ignoreSelection = false;
   protected Rectangle bounds = new Rectangle();
   
   protected boolean deleted = false;
   protected boolean markedAsDeleted = false;
   
   // The ZoomController of the GuiView this component is part of.
   private Zoomer zoomControl;   
   
   // Integer value which represents a zoom percentage
   protected int zoom = 100;   
   
   
   /**
    * Create PetriNetObject
    */
   public PetriNetObject(){
      ;    
   }

   
   /**
    * Set id
    * @param idInput String value for id;
    */
   public void setId(String idInput) {
      id = idInput;
   }

   
   /**
    * Get id returns null if value not yet entered
    * @return String value for id;
    */
   public String getId() {
      return id;
   }

   
   /**
    * Returns Name Label - is used by GuiView
    * @return PetriNetObject's Name Label (Model View Controller Design Pattern)
    */
   public NameLabel getNameLabel(){
      return pnName;
   }

   
   public void addLabelToContainer() {
      if (getParent() != null && pnName.getParent() == null) {
         getParent().add(pnName);
      }
   }

   
   public boolean isSelected() {
      return selected;
   }

   
   public void select() {
      if (selectable && !selected) {
         selected = true;
         repaint();
      }
   }

   
   public void deselect() {
      if (selected) {
         selected = false;
         repaint();
      }
   }

   
   public boolean isSelectable() {
      return selectable;
   }

   
   public void setSelectable(boolean allow) {
      selectable = allow;
   }

   
   public static void ignoreSelection(boolean ignore) {
      ignoreSelection = ignore;
   }

   
   public boolean isDraggable() {
      return draggable;
   }

   
   public void setDraggable(boolean allow) {
      draggable = allow;
   }

   
   public void setObjectColour(Color c) {
      objectColour = c;
   }

   
   public void setSelectionBorderColour(Color c) {
      selectionBorderColour = c;
   }

   
   public abstract void addedToGui();
   
   
   public void delete() {
      deleted = true;
      CreateGui.getModel().removePetriNetObject(this);
      removeFromContainer();
      removeAll();
   }
   
   
   public void undelete(DataLayer model, GuiView view) {
      model.addPetriNetObject(this);
      view.add(this);
   }
   
   
   protected void removeFromContainer() {
      Container c = getParent();
      
      if (c != null){
         c.remove(this);
      }
   }
   
   
   public UndoableEdit setPNObjectName(String name){
      String oldName = this.getName();
      this.setName(name);
      return new PetriNetObjectNameEdit(this, oldName, name);               
   }
   
   
   public boolean isDeleted() {
      return deleted || markedAsDeleted;
   }

   
   public void markAsDeleted() {
      markedAsDeleted = true;
   }

   
   public void select(Rectangle selectionRectangle) {
      if (selectionRectangle.intersects(this.getBounds())) {
         select();
      }
   }


   public void paintComponent(Graphics g) {
      super.paintComponent(g);
   }
   
   
   public boolean isCopyPasteable() {
      return copyPasteable;
   }   
 
   

   public abstract int getLayerOffset();

   
   public int getZoom() {
      return zoom;
   }

   
   public PetriNetObject clone() {
      try {
         PetriNetObject pnObjectCopy = (PetriNetObject) super.clone();

         // Remove all mouse listeners on the new object
         EventListener[] mouseListeners = pnObjectCopy.getListeners(MouseListener.class);
         for (int i = 0; i < mouseListeners.length; i++){
            pnObjectCopy.removeMouseListener((MouseListener) mouseListeners[i]);
         }
         
         mouseListeners = pnObjectCopy.getListeners(MouseMotionListener.class);
         
         for (int i = 0; i < mouseListeners.length; i++) {
            pnObjectCopy.removeMouseMotionListener((MouseMotionListener) mouseListeners[i]);
         }
         
         mouseListeners = pnObjectCopy.getListeners(MouseWheelListener.class);
         
         for (int i = 0; i < mouseListeners.length; i++) {
            pnObjectCopy.removeMouseWheelListener((MouseWheelListener) mouseListeners[i]);
         }
         
         return pnObjectCopy;
      } catch (CloneNotSupportedException e) {
         throw new Error(e);
      }
   }

}
