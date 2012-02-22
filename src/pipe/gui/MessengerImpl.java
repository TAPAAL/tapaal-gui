package pipe.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import dk.aau.cs.Messenger;

public class MessengerImpl implements Messenger {

	public void displayInfoMessage(String message) {
		displayInfoMessage(message, "Message");
	}

	public void displayInfoMessage(String message, String title) {
		showMessageBox(message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public void displayErrorMessage(String message) {
		displayErrorMessage(message, "Error");
	}

	public void displayErrorMessage(String message, String title) {
		showMessageBox(message, title, JOptionPane.ERROR_MESSAGE);
	}

	private void showMessageBox(Object message, String title, int messageType) {
		JOptionPane.showMessageDialog(CreateGui.getApp(), message, title,
				messageType);
	}

	public void displayWrappedErrorMessage(String message, String title) {
		JTextArea textArea = new JTextArea(message);
		textArea.setEditable(false);
		textArea.setEnabled(false);
		textArea.setDisabledTextColor(Color.BLACK);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setMargin(new Insets(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 100));
		scrollPane.getViewport().setView(textArea);

		showMessageBox(scrollPane, title, JOptionPane.ERROR_MESSAGE);
	}
}
