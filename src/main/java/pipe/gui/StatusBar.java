package pipe.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/* Status Bar to let users know what to do*/
public class StatusBar extends JPanel {

	private final JLabel label;

	public StatusBar() {
		super();
		label = new JLabel("");
		this.setLayout(new BorderLayout(0, 0));
		this.add(label);
	}

	public void changeText(String newText) {
		label.setText(newText);
	}



}
