package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.undo.ArcWeightEdit;
import pipe.gui.undo.ClearInverseArcEdit;
import pipe.gui.undo.JoinInverseArcEdit;
import pipe.gui.undo.SetInverseArcEdit;
import pipe.gui.undo.SplitInverseArcEdit;
import pipe.gui.undo.TagArcEdit;
import pipe.gui.undo.UndoableEdit;


/**
 * <b>Arc</b> - Petri-Net Normal Arc Class
 *
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema (stNet.xsd)</a>
 * @see </p><p><a href="..\..\..\UML\dataLayer.html">UML  -  PNML Package </a></p>
 * @version 1.0
 */
public class NormalArc
        extends Arc {
/**
	 * 
	 */
	private static final long serialVersionUID = 5588142404135607382L;

/*CB Joakim Byg - probably not needed   
   public final static String type = "normal";
EOC*/   
   protected Polygon head = 
           new Polygon(new int[]{0, 5, 0, -5}, new int[]{0, -10, -7, -10}, 4);
   
   // bidirectional arc?
   private boolean joined = false;
   
   // Whether or not exists an inverse arc
   private NormalArc inverse = null;
   
      
   /** Whether or not the Arc is capable of carrying tagged tokens
    *  By default it is not
    */
   private Boolean tagged = false;   

   
   /**
    * Create Petri-Net Arc object
    * @param startPositionXInput Start X-axis Position
    * @param startPositionYInput Start Y-axis Position
    * @param endPositionXInput End X-axis Position
    * @param endPositionYInput End Y-axis Position
    * @param sourceInput Arc source
    * @param targetInput Arc target
    * @param idInput Arc id
    */
   public NormalArc(double startPositionXInput, double startPositionYInput, 
                    double endPositionXInput, double endPositionYInput, 
                    PlaceTransitionObject sourceInput, 
                    PlaceTransitionObject targetInput,
                    int weightInput,
                    String idInput,
                    boolean taggedInput){
      super(startPositionXInput, startPositionYInput,
            endPositionXInput, endPositionYInput,
            sourceInput, 
            targetInput, 
            weightInput, 
            idInput);
      setTagged(taggedInput);
   }
   
   
   /**
    * Create Petri-Net Arc object
    */
   public NormalArc(PlaceTransitionObject newSource) {
      super(newSource);
   }
   
   
   public NormalArc(NormalArc arc) {
      weightLabel = new NameLabel(zoom);
      
      for (int i = 0; i <= arc.myPath.getEndIndex(); i++){
         this.myPath.addPoint(arc.myPath.getPoint(i).getX(),
                              arc.myPath.getPoint(i).getY(),
                              arc.myPath.getPointType(i));         
      }      
      this.myPath.createPath();
      this.updateBounds();  
      this.id = arc.id;
      this.setSource(arc.getSource());
      this.setTarget(arc.getTarget());
      this.setWeight(arc.getWeight());
      this.inView = arc.inView;
      this.joined = arc.joined;
   }

   
   public NormalArc paste(double despX, double despY, boolean toAnotherView){
      PlaceTransitionObject source = this.getSource().getLastCopy();
      PlaceTransitionObject target = this.getTarget().getLastCopy();
      
      if (source == null && target == null) {
         // don't paste an arc with neither source nor target
         return null;
      }
      
      if (source == null){
         if (toAnotherView) {
            // if the source belongs to another Petri Net, the arc can't be 
            // pasted
            return null;
         } else {
            source = this.getSource();
         }
      }
      
      if (target == null){
         if (toAnotherView) {
            // if the target belongs to another Petri Net, the arc can't be 
            // pasted            
            return null;
         } else {
            target = this.getTarget();
         }
      }
      
      NormalArc copy =  new NormalArc(0, 0, //startPoint
                                      0, 0, //endPoint
                                      source,
                                      target,
                                      this.getWeight(),
                                      source.getId() + " to " + target.getId(),
                                      false);      
      
      copy.myPath.delete();
      for (int i = 0; i <= this.myPath.getEndIndex(); i++){
         copy.myPath.addPoint(this.myPath.getPoint(i).getX() + despX,
                              this.myPath.getPoint(i).getY() + despY,
                              this.myPath.getPointType(i));         
         copy.myPath.selectPoint(i); 
      }
            
      source.addConnectFrom(copy);
      target.addConnectTo(copy);
      
      copy.inView = this.inView;
      copy.joined = this.joined;
      
      return copy;
   }
   
   
   public NormalArc copy(){
      return new NormalArc(this);
   }   
   
/*CB Joakim Byg - This is sortof ugly, since it is only used one place 
  and we would like to make an other type of arc that inherits from NormalArc 
   public String getType(){
      return this.type;
   }
 EOC*/  
      
   @Override
public UndoableEdit setWeight(int weightInput){
      int oldWeight = weight;

      weight = weightInput;
      updateWeightLabel();
      return new ArcWeightEdit(this, oldWeight, weight);      
   }
   
   
   
   /** Accessor function to set whether or not the Arc is tagged */             
   public UndoableEdit setTagged(boolean flag){
      /**Set the timed transition attribute (for GSPNs)*/

      tagged = flag;
      
      // If it becomes tagged we must remove any existing weight....
      // ...and thus we can reuse the weightLabel to display that it's tagged!!!
      // Because remember that a tagged arc must have a weight of 1...
      if (tagged) {
         //weight = 1;
         weightLabel.setText("TAG");
         setWeightLabelPosition();
         weightLabel.updateSize();         
      } else {
         weightLabel.setText((weight > 1)?Integer.toString(weight) : "");
      }
      repaint();
      return new TagArcEdit(this);      
   }

   
   /** Accessor function to check whether or not the Arc is tagged */
   public boolean isTagged(){
      return tagged;
   }      
   
   
   public void updateWeightLabel(){   
      if (hasInverse() && !inView) {
         inverse.updateWeightLabel();
      } else {
         if (!hasInvisibleInverse()){
            if (weight == 1){
               weightLabel.setText("");
            } else {
               weightLabel.setText(Integer.toString(weight));            
            }
         } else {
            if (weight == 1 && inverse.weight == 1){         
               weightLabel.setText("");
            } else {
               if (getSource() instanceof Place){
                  weightLabel.setText(weight + "(PT) / " + inverse.getWeight() + "(TP)");
               } else {
                  weightLabel.setText(inverse.getWeight() + "(PT) / " + weight + "(TP)");
               }   
            }
         }
         setWeightLabelPosition();
      }
   }

   
   public void setInView(boolean flag) {
      inView = flag;
   }

   
   public void setJoined(boolean flag) {
      joined = flag;
   }

   
   public UndoableEdit clearInverse() {
      NormalArc oldInverse = inverse;

      inverse.inView = true;
      inView = true;
      
      inverse.joined = false;            
      joined = false;

      inverse.updateWeightLabel();                
      updateWeightLabel();             
  
      inverse.inverse = null;
      inverse = null;   
      
      return new ClearInverseArcEdit(this, oldInverse, false);
   }

   
   public boolean hasInverse() {
      return inverse != null;
   }
   

   public NormalArc getInverse() {
      return inverse;
   }
   
   
   public UndoableEdit setInverse(NormalArc _inverse, boolean joined) {
      inverse = _inverse;
      inverse.inverse = this;
      updateArc(joined);
      return new SetInverseArcEdit(this, inverse, joined); 
   }

   
   private void updateArc(boolean isJoined){
      inView = true;
      inverse.inView = !isJoined;
      
      if (isJoined) {
         inverse.removeFromView();
         Transition transition = this.getTransition();
         transition.removeFromArc(inverse);
         transition.removeArcCompareObject(inverse);
         transition.updateConnected();
         joined = isJoined;
      }
      updateWeightLabel();
   }
   
   
   public boolean isJoined() {
      return joined;
   }

   
   public UndoableEdit split() {
      //
      if (!this.inverse.inView) { 
         CreateGui.getView().add(inverse);
         inverse.getSource().addConnectFrom(inverse);
         inverse.getTarget().addConnectTo(inverse);
      }
      if (!this.inView) {
         CreateGui.getView().add(this);
         this.getSource().addConnectFrom(this);
         this.getTarget().addConnectTo(this);
      }

      //
      inverse.inView = true;
      this.inView = true;
      this.joined = false;
      inverse.joined = false;

      this.updateWeightLabel();
      inverse.updateWeightLabel();    

      this.updateArcPosition();
      inverse.updateArcPosition();
      
      return new SplitInverseArcEdit(this);
   }

   
   public UndoableEdit join() {
      this.updateArc(true);
      //((NormalArc)arc.getInverse()).setInView(false);
      //arc.getParent().remove(arc.getInverse());
      inverse.removeFromView();
      this.setJoined(true);
      if (this.getParent() != null) {
         this.getParent().repaint();
      }
      
      return new JoinInverseArcEdit(this);
   }

   
   public boolean hasInvisibleInverse() {
      return ((this.inverse!=null) && !(this.inverse.inView()));
   }
  

   @Override
public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;   

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);
      
      g2.translate(COMPONENT_DRAW_OFFSET + zoomGrow - myPath.getBounds().getX(),
               COMPONENT_DRAW_OFFSET + zoomGrow - myPath.getBounds().getY());
      
      AffineTransform reset = g2.getTransform();      

      if (selected && !ignoreSelection){
         g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
      } else{
         g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
      }      
      
      if (joined) {
         g2.translate(myPath.getPoint(0).getX(), myPath.getPoint(0).getY());
         g2.rotate(myPath.getStartAngle() + Math.PI);
         g2.transform(Zoomer.getTransform(zoom)); 
         g2.fillPolygon(head);
         g2.setTransform(reset);         
      }
      
      g2.setStroke(new BasicStroke(0.01f * zoom));
      g2.draw(myPath);

      g2.translate(myPath.getPoint(myPath.getEndIndex()).getX(),
               myPath.getPoint(myPath.getEndIndex()).getY());
        
      g2.rotate(myPath.getEndAngle()+Math.PI);
      g2.setColor(java.awt.Color.WHITE);
            
      g2.transform(Zoomer.getTransform(zoom));   
      g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);    

      if (selected && !ignoreSelection){
         g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
      } else{
         g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
      }
      
      g2.setStroke(new BasicStroke(0.8f));
      g2.fillPolygon(head);
      
      g2.transform(reset);   
   }   

   @Override
public NormalArc clone(){

	   NormalArc toReturn = (NormalArc)super.clone();
	   toReturn.setTagged(tagged);
	   return toReturn;

   }

}
