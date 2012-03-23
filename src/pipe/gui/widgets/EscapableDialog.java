/*
 * EscapableDialog.java
 */

package pipe.gui.widgets;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * 
 * @author Pere Bonet
 */
public class EscapableDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7859292606172447650L;

	/** Creates a new instance of EscapableDialog */
	public EscapableDialog(Frame frame, String string, boolean modal) {
		super(frame, string, modal);
	}
	
	public EscapableDialog(JDialog dialog, String string, boolean modal) {
		super(dialog, string, modal);
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		Action actionListener = new AbstractAction() {
			/**
		 * 
		 */
			private static final long serialVersionUID = -7649362860682585358L;

			public void actionPerformed(ActionEvent actionEvent) {
				setVisible(false);
			}
		};
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", actionListener);
		return rootPane;
	}

}
