package pipe.gui;

public interface Messenger {
	void displayInfoMessage(String message);
	void displayInfoMessage(String message, String title);
	
	void displayErrorMessage(String message);
	void displayErrorMessage(String message, String title);
}
