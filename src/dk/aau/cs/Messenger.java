package dk.aau.cs;

public interface Messenger {
	void displayInfoMessage(String message);

	void displayInfoMessage(String message, String title);

	void displayErrorMessage(String message);

	void displayErrorMessage(String message, String title);

	void displayWrappedErrorMessage(String message, String title);
}
