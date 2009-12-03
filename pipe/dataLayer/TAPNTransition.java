package pipe.dataLayer;

import java.awt.Container;

import javax.swing.BoxLayout;

import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.TAPNTransitionEditor;

public class TAPNTransition extends Transition {

	private static final long serialVersionUID = -2280012053262288174L;
	
	public TAPNTransition(Transition t) {
		super(t);
		// TODO Auto-generated constructor stub
	}

	public TAPNTransition(double positionXInput, double positionYInput) {
		super(positionXInput, positionYInput);
		// TODO Auto-generated constructor stub
	}

	public TAPNTransition(double positionXInput, double positionYInput, String idInput, String nameInput, double nameOffsetXInput, double nameOffsetYInput, double rateInput, boolean timedTransition, boolean infServer, int angleInput, int priority) {
		super(positionXInput, positionYInput, idInput, nameInput, nameOffsetXInput,
				nameOffsetYInput, rateInput, timedTransition, infServer, angleInput,
				priority);
		// TODO Auto-generated constructor stub
	}
	

	public TAPNTransition(String idInput, String nameInput) {
		super(idInput, nameInput);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void showEditor(){
	      // Build interface
	      EscapableDialog guiDialog = 
	              new EscapableDialog(CreateGui.getApp(),Pipe.getProgramName(),true);
	      
	      Container contentPane = guiDialog.getContentPane();

			// 1 Set layout
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

			// 2 Add Place editor
			contentPane.add( new TAPNTransitionEditor(guiDialog.getRootPane(),
		              this, CreateGui.getModel(), CreateGui.getView()), this);

			guiDialog.setResizable(true);     

			// Make window fit contents' preferred size
			guiDialog.pack();

			// Move window to the middle of the screen
			guiDialog.setLocationRelativeTo(null);
			guiDialog.setVisible(true);
	   }      
	 
	   @Override
	public TAPNTransition copy(){

		   TAPNTransition copy = new TAPNTransition(Zoomer.getUnzoomedValue(this.getX(), zoom),
				   									Zoomer.getUnzoomedValue(this.getY(), zoom));      
		      copy.setName(this.getName());
		      copy.nameOffsetX = this.nameOffsetX;
		      copy.nameOffsetY = this.nameOffsetY;
		      copy.setOriginal(this);
		      return copy;
		   
//		   TAPNTransition copy = new TAPNTransition(super.copy());      
//		   return copy;
	   }
	   
	   @Override
	public TAPNTransition paste(double x, double y, boolean fromAnotherView){
		   this.incrementCopyNumber();
	      TAPNTransition copy = new TAPNTransition (
	              Grid.getModifiedX(x + this.getX() + Pipe.PLACE_TRANSITION_HEIGHT/2),
	              Grid.getModifiedY(y + this.getY() + Pipe.PLACE_TRANSITION_HEIGHT/2));
	      copy.pnName.setName(this.pnName.getName()  + "(" + this.getCopyNumber() +")");
	      
	      this.newCopy(copy);
	      copy.nameOffsetX = this.nameOffsetX;
	      copy.nameOffsetY = this.nameOffsetY;
	           
//	      copy.angle = this.angle;

	      copy.attributesVisible = this.attributesVisible;
//	      copy.priority = this.priority;
//	      copy.transition.transform(
//	              AffineTransform.getRotateInstance(Math.toRadians(copy.angle), 
//	                                                Transition.TRANSITION_HEIGHT/2,
//	                                                Transition.TRANSITION_HEIGHT/2));
//	      copy.rateParameter = null;//this.rateParameter;
	      return copy;
	   }

	   //Override by jokke
	   @Override
	public boolean isEnabled(boolean animationStatus){
		   if (animationStatus) {
			   if (enabled) {
				   if (isEnabledByDelay()){					   
					   highlighted = false;
				   }else {
					   highlighted = true;
				   }
				   return true;
			   } else {
				   highlighted = false;
			   }
		   } 
		   return false;
	   }

	private boolean isEnabledByDelay() {
		// TODO Auto-generated method stub
		return false;
	}
}
