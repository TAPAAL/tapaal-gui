package pipe.dataLayer;

import java.awt.Container;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GuardDialogue;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedInputArc;

public class TimedInputArcComponent extends TimedOutputArcComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8263782840119274756L;
	private TimedInputArc inputArc;
	protected String timeInterval; 
	
	public TimedInputArcComponent(PlaceTransitionObject source){
		super(source);
		init();
	}

	private void init() {
		updateWeightLabel();
	}
	
	
	public TimedInputArcComponent(TimedOutputArcComponent arc){
		super(arc);
		init();
	}

	
	public TimedInputArcComponent(TimedOutputArcComponent arc, String guard) {
		super(arc);
		updateWeightLabel();
	}
	
	@Override
	public void delete() {
		if(inputArc != null) inputArc.delete();
		super.delete();
	}
	
	
	public void SetTimedInputArc(TimedInputArc inputArc) {
		this.inputArc = inputArc;
	}

	public static boolean validateTimeInterval(String timeInterval) {
		if (Pattern.matches("((\\(\\d+)|(\\[\\d+)),((inf\\))|((\\d+\\))|(\\d+\\])))",timeInterval)){
			String[] range = timeInterval.split(",");
			String firstNumber = "";
			String secondNumber = "";
			for (int i=1; i<range[0].length(); i++){
				firstNumber = "" + firstNumber + range[0].charAt(i);
			}
			for (int i=0; i<range[1].length()-1; i++){
				secondNumber = "" + secondNumber + range[1].charAt(i);
			}
			if (secondNumber.equals("inf")) secondNumber = ""+Integer.MAX_VALUE; 
			return Integer.parseInt(firstNumber)<=Integer.parseInt(secondNumber);	
		}
		return false;
	}
	
	public String getGuardAsString() {
		
		return inputArc.interval().toString();
	}
	
	public TimeInterval getGuard() {
		return inputArc.interval();
	}
	
	
	public Command setGuard(TimeInterval guard) {
	
		TimeInterval oldTimeInterval = inputArc.interval();
		inputArc.setTimeInterval(guard);

		//hacks - I use the weight to display the TimeInterval
		updateWeightLabel();
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval, inputArc.interval());
	}
	//hacks - I use the weight to display the TimeInterval
	@Override
	public void updateWeightLabel(){   
		if(!CreateGui.getModel().netType().equals(NetType.UNTIMED)){
			if(inputArc == null)
				weightLabel.setText("");
			else
				weightLabel.setText(inputArc.interval().toString());

			this.setWeightLabelPosition();	
		}
	}
	
	
	@Override
	public TimedInputArcComponent copy(){
		TimedOutputArcComponent copy = new TimedOutputArcComponent(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedInputArcComponent timedCopy = new TimedInputArcComponent(copy.copy(), this.timeInterval);
		return timedCopy;
	}
	
	@Override
	public TimedInputArcComponent paste(double despX, double despY, boolean toAnotherView){
		TimedOutputArcComponent copy = new TimedOutputArcComponent(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedInputArcComponent timedCopy = new TimedInputArcComponent(copy.paste(despX, despY, toAnotherView), this.timeInterval);
		return timedCopy;
	}

	public void showTimeIntervalEditor() {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new GuardDialogue(guiDialog.getRootPane(), this) );

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

	}

	public boolean satisfiesGuard(BigDecimal token) {
//		boolean satisfies = true;
//		String[] partedTimeInteval = timeInterval.split(",");
//		if ((""+partedTimeInteval[0].charAt(0)).contains("[") ){
//			if (token.compareTo(BigDecimal.valueOf(Long.parseLong( partedTimeInteval[0].substring(1) ))) < 0){
//				return false;
//			}
//		}else {
//			if ( token.compareTo(BigDecimal.valueOf(Long.parseLong( partedTimeInteval[0].substring(1) )))  <= 0){
//				return false;
//			}
//		}
//		int guardMaxValue = 0;
//		
//		int lastIndexOfNumber = partedTimeInteval[1].length()-1;
//		if ( partedTimeInteval[1].substring(0, lastIndexOfNumber).contains("inf") ){
//			guardMaxValue = Integer.MAX_VALUE;
//		} else {
//			guardMaxValue = Integer.parseInt( partedTimeInteval[1].substring(0, lastIndexOfNumber) );
//		}
//		
//		if ((""+partedTimeInteval[1].charAt(lastIndexOfNumber)).contains("]") ){
//			if ( token.compareTo(BigDecimal.valueOf((Long.parseLong(""+guardMaxValue)))) > 0 ){
//				return false;
//			}
//		}else {
//			if ( token.compareTo(BigDecimal.valueOf((Long.parseLong(""+guardMaxValue)))) >= 0){
//				return false;
//			}
//		}
//		
//		return satisfies;
		return inputArc.isEnabledBy(token);
	}
	
	@Override
	public void setWeightLabelPosition() {
		weightLabel.setPosition(
				(int)(myPath.midPoint.x) + weightLabel.getWidth()/2 - 4, 
				(int)(myPath.midPoint.y) - ((zoom/55)*(zoom/55)) );
	}

	public dk.aau.cs.model.tapn.TimedInputArc underlyingTimedInputArc() {
		return inputArc;
	}
	
	public void setUnderlyingArc(dk.aau.cs.model.tapn.TimedInputArc ia) {
		this.inputArc = ia;
	}
}