package pipe.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/* Status Bar to let users know what to do*/
public class StatusBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8253246293753481390L;
	/* Provides the appropriate text for the mode that the user is in */
	public String textforNoNet = "Open a net to start editing";

	public String textforDrawing = "Drawing Mode: Click on a button to start adding components to the "
			+ "Editor";
	public String textforPlace = "Place Mode: Right click on a place to see menu options "
			+ "";
	public String textforTAPNPlace = "Place Mode: Right click on a place to see menu options "
			+ "";
	public String textforTrans = "Transition Mode: Right click on a transition to see menu "
			+ "options [Mouse wheel -> rotate]";
	public String textforTimedTrans = "Timed Transition Mode: Right click on a transition to see menu "
			+ "options [Mouse wheel -> rotate]";
	public String textforAddtoken = "Add Token Mode: Click on a place to add a token";
	public String textforDeltoken = "Delete Token Mode: Click on a place to delete a token ";
	public String textforAnimation = "Animation Mode: Red transitions are enabled, click a transition to "
			+ "fire it";
	public String textforArc = "Arc Mode: Right click on an arc to see menu options "
			+ "";
	public String textforTransportArc = "Transport Arc Mode: Right click on an arc to see menu options "
			+ "";
	public String textforInhibArc = "Inhibitor Mode: Right click on an arc to see menu options "
			+ "";
	public String textforMove = "Select Mode: Click/drag to select objects; drag to move them";
	public String textforAnnotation = "Annotation Mode: Right click on an annotation to see menu options; "
			+ "double click to edit";

	public String textforDrag = "Drag Mode";

	public String textforMarking = "Add a marking parameter";

	public String textforRate = "Add a rate parameter";

	private JLabel label;

	public StatusBar() {
		super();
		label = new JLabel(textforDrawing); // got to put something in there
		this.setLayout(new BorderLayout(0, 0));
		this.add(label);
	}

	public void changeText(String newText) {
		label.setText(newText);
	}

	public void changeText(Pipe.ElementType type) {
		switch (type) {
		case PLACE:
			changeText(textforPlace);
			break;
		case TAPNPLACE:
			changeText(textforTAPNPlace);
			break;
		case IMMTRANS:
			changeText(textforTrans);
			break;

		case TIMEDTRANS:
			changeText(textforTimedTrans);
			break;
		case TAPNTRANS:
			changeText(textforTrans);
			break;
		case ARC:
			changeText(textforArc);
			break;
		case TAPNARC:
			changeText(textforArc);
			break;
		case TRANSPORTARC:
			changeText(textforTransportArc);
			break;
		case TAPNINHIBITOR_ARC:
		case INHIBARC:
			changeText(textforInhibArc);
			break;

		case ADDTOKEN:
			changeText(textforAddtoken);
			break;

		case DELTOKEN:
			changeText(textforDeltoken);
			break;

		case SELECT:
			changeText(textforMove);
			break;

		case DRAW:
			changeText(textforDrawing);
			break;

		case ANNOTATION:
			changeText(textforAnnotation);
			break;

		case DRAG:
			changeText(textforDrag);
			break;

		default:
			changeText("To-do (textfor" + type);
			break;
		}
	}

}