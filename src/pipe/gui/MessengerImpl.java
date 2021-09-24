package pipe.gui;

import java.awt.*;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import dk.aau.cs.Messenger;

public class MessengerImpl implements Messenger {

	public void displayInfoMessage(String message) {
		displayInfoMessage(message, "Message");
	}

	public void displayInfoMessage(String message, String title) {
		showMessageBox(getTextArea(message), title, JOptionPane.INFORMATION_MESSAGE);
	}

	public void displayErrorMessage(String message) {
		displayErrorMessage(message, "Error");
	}

	public void displayErrorMessage(String message, String title) {
        showMessageBox(getTextArea(message), title, JOptionPane.ERROR_MESSAGE);
	}

	private void showMessageBox(Object message, String title, int messageType) {
		JOptionPane.showMessageDialog(CreateGui.getApp(), message, title,
				messageType);
	}

	public void displayWrappedErrorMessage(String message, String title) {
		JTextArea textArea = new JTextArea(message);
		textArea.setEditable(false);
		textArea.setEnabled(true);
		textArea.setDisabledTextColor(Color.BLACK);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setMargin(new Insets(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 100));
		scrollPane.getViewport().setView(textArea);

		showMessageBox(scrollPane, title, JOptionPane.ERROR_MESSAGE);
	}

	private JTextArea getTextArea(String message) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setEnabled(true);
        textArea.setLineWrap(false);
        textArea.setDisabledTextColor(Color.BLACK);
        textArea.setBackground(Color.getColor("FFe0d0"));

        return textArea;
    }
}
