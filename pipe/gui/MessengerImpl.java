package pipe.gui;

import javax.swing.JOptionPane;

import dk.aau.cs.verification.Messenger;

public class MessengerImpl implements Messenger {

	public void displayInfoMessage(String message) {
		displayInfoMessage(message, Pipe.getProgramName());
	}

	public void displayInfoMessage(String message, String title) {
		showMessageBox(message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public void displayErrorMessage(String message) {
		displayErrorMessage(message, Pipe.getProgramName());
	}

	public void displayErrorMessage(String message, String title) {
		showMessageBox(message, title, JOptionPane.ERROR_MESSAGE);
	}

	
	private void showMessageBox(String message, String title, int messageType) {
		JOptionPane.showMessageDialog(CreateGui.getApp(), message, title, messageType);
	}
}
