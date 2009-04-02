package pipe.exception;

public class IlligalNameException extends Exception {

	String name="";
	
	public IlligalNameException(String nameInput) {
		name = nameInput;
	}

}
