package dk.aau.cs.verification;

public interface Messenger {
	void displayInfoMessage(String message);
	void displayInfoMessage(String message, String title);
	
	void displayErrorMessage(String message);
	void displayErrorMessage(String message, String title);
}
