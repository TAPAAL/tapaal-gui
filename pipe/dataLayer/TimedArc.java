package pipe.dataLayer;

import java.awt.Color;
import java.awt.Container;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;

import pipe.gui.CreateGui;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.undo.ArcWeightEdit;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GuardDialogue;
import pipe.gui.widgets.PlaceEditorPanel;

public class TimedArc extends NormalArc{

	protected String timeInterval; 
	
	public TimedArc(NormalArc arc){
		super(arc);
		timeInterval="[0,inf)";
		
		if (arc.getSource() instanceof pipe.dataLayer.Transition){
			
		}
		updateWeightLabel();
	}

	public TimedArc(NormalArc arc, String guard) {
		super(arc);
		timeInterval = guard;
		updateWeightLabel();
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
	public String getGuard() {
		// TODO Auto-generated method stub
		return timeInterval;
	}
	public UndoableEdit setGuard(String timeInteval) {
	
		String oldTimeInterval = this.timeInterval;
		this.timeInterval = timeInteval;

		//hacks - I use the weight to display the TimeInterval
		updateWeightLabel();
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval, this.timeInterval);
	}
	//hacks - I use the weight to display the TimeInterval
	public void updateWeightLabel(){   
		weightLabel.setText(timeInterval);

		this.setWeightLabelPosition();
	}
	
	
	public TimedArc copy(){
		NormalArc copy = new NormalArc(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedArc timedCopy = new TimedArc(copy.copy(), this.timeInterval);
		return timedCopy;
	}
	
	public TimedArc paste(double despX, double despY, boolean toAnotherView){
		NormalArc copy = new NormalArc(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedArc timedCopy = new TimedArc(copy.paste(despX, despY, toAnotherView), this.timeInterval);
		return timedCopy;
	}

	public void showTimeIntervalEditor() {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), "TAPAAL 0.3", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new GuardDialogue(guiDialog.getRootPane(), this) );

		guiDialog.setResizable(true);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

	}

	public boolean satisfiesGuard(BigDecimal token) {
		boolean satisfies = true;
		String[] partedTimeInteval = timeInterval.split(",");
		if ((""+partedTimeInteval[0].charAt(0)).contains("[") ){
			if (token.compareTo(BigDecimal.valueOf(Long.parseLong( partedTimeInteval[0].substring(1) ))) < 0){
				return false;
			}
		}else {
			if ( token.compareTo(BigDecimal.valueOf(Long.parseLong( partedTimeInteval[0].substring(1) )))  <= 0){
				return false;
			}
		}
		int guardMaxValue = 0;
		
		int lastIndexOfNumber = partedTimeInteval[1].length()-1;
		if ( partedTimeInteval[1].substring(0, lastIndexOfNumber).contains("inf") ){
			guardMaxValue = Integer.MAX_VALUE;
		} else {
			guardMaxValue = Integer.parseInt( partedTimeInteval[1].substring(0, lastIndexOfNumber) );
		}
		
		if ((""+partedTimeInteval[1].charAt(lastIndexOfNumber)).contains("]") ){
			if ( token.compareTo(BigDecimal.valueOf((Long.parseLong(""+guardMaxValue)))) > 0 ){
				return false;
			}
		}else {
			if ( token.compareTo(BigDecimal.valueOf((Long.parseLong(""+guardMaxValue)))) >= 0){
				return false;
			}
		}
		
		return satisfies;
	}
	
	public void setWeightLabelPosition() {
		weightLabel.setPosition(
				(int)(myPath.midPoint.x) + weightLabel.getWidth()/2 - 4, 
				(int)(myPath.midPoint.y) - ((zoom/55)*(zoom/55)) );
	}
}